package uk.ac.ebi.biosd.server.service;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ebi.age.admin.server.mng.AgeAdmin;
import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.admin.server.mng.RemoteRequestListener;
import uk.ac.ebi.age.admin.server.service.ServiceRequest;
import uk.ac.ebi.age.annotation.AnnotationManager;
import uk.ac.ebi.age.annotation.Topic;
import uk.ac.ebi.age.authz.ACR.Permit;
import uk.ac.ebi.age.authz.AuthDB;
import uk.ac.ebi.age.authz.BuiltInUsers;
import uk.ac.ebi.age.authz.Session;
import uk.ac.ebi.age.authz.exception.TagException;
import uk.ac.ebi.age.ext.annotation.AnnotationDBException;
import uk.ac.ebi.age.ext.authz.SystemAction;
import uk.ac.ebi.age.ext.authz.TagRef;
import uk.ac.ebi.age.ext.entity.ClusterEntity;
import uk.ac.ebi.age.ext.submission.SubmissionDBException;
import uk.ac.ebi.age.model.AgeClass;
import uk.ac.ebi.age.model.AgeObject;
import uk.ac.ebi.age.transaction.ReadLock;
import uk.ac.ebi.age.transaction.Transaction;
import uk.ac.ebi.age.transaction.TransactionException;
import uk.ac.ebi.age.util.StringUtil;
import uk.ac.ebi.biosd.shared.TagControlConstants;

public class BioSDTagController implements RemoteRequestListener
{
 private final Configuration config;
 private final AgeAdmin admin;

 public BioSDTagController(AgeAdmin adm)
 {
  admin = adm;
  
  config = adm.getConfiguration();
 }

 @Override
 public boolean processRequest(ServiceRequest upReq, final PrintWriter out)
 {
  Session sess = config.getSessionManager().getSession();

  String userName = sess != null ? sess.getUser() : BuiltInUsers.ANONYMOUS.getName();

  String[] sbmId=null;
  String[] sampleId=null;
  String[] groupId=null;
  
  String val = upReq.getParams().get(TagControlConstants.SUBMISSON_ID);

  if(val != null)
  {
   sbmId = val.split(",");
   
   for(int i = 0; i < sbmId.length; i++)
    sbmId[i] = sbmId[i].trim();
  }

  val = upReq.getParams().get(TagControlConstants.SAMPLE_ID);

  if(val != null)
  {
   sampleId = val.split(",");
   
   for(int i = 0; i < sampleId.length; i++)
    sampleId[i] = sampleId[i].trim();
  }

  val = upReq.getParams().get(TagControlConstants.GROUP_ID);

  if(val != null)
  {
   groupId = val.split(",");
   
   for(int i = 0; i < groupId.length; i++)
    groupId[i] = groupId[i].trim();
  }

  
  if(config.getPermissionManager().checkSystemPermission(SystemAction.CTRLSUBMTAGS, userName) != Permit.ALLOW)
  {
   out.println("ERROR: user '" + userName + "' is not allowed to manage submission tags");
   return false;
  }


  if( sbmId != null )
  {
   for( String id : sbmId )
   {
    try
    {
     if( ! config.getSubmissionDB().hasSubmission(id) )
     {
      out.println("ERROR: submission doesn't exist: "+id);
      return false;
     }
    }
    catch(SubmissionDBException e)
    {
     out.println("ERROR: submission DB error: " + e.getMessage() + " (" + e.getClass().getName() + ")");
    }
   }
  }
  
  
  AuthDB authDB = config.getAuthDB();

  String tagsStr = upReq.getParams().get(TagControlConstants.TAGS_TO_ADD);

  List<TagRef> addTags = null;

  if(tagsStr != null)
  {
   addTags = parseTags(tagsStr);

   if(addTags == null)
   {
    out.println("ERROR: Invalid tag string: " + tagsStr);
    return false;
   }

   if( ! checkTags(authDB, addTags, out))
    return false;
  }

  tagsStr = upReq.getParams().get(TagControlConstants.TAGS_TO_REMOVE);

  List<TagRef> delTags = null;

  if(tagsStr != null)
  {
   delTags = parseTags(tagsStr);

   if(delTags == null)
   {
    out.println("ERROR: Invalid tag string: " + tagsStr);
    return false;
   }
  }

  AnnotationManager annotationManager = config.getAnnotationManager();

  ClusterEntity cEnt = new ClusterEntity();

  List<TagRef> auxList = new ArrayList<TagRef>();


  boolean ok = false;
  
  Transaction trn = null;
  
  if(  addTags != null ||  delTags != null )
  {
   try
   {
    trn = annotationManager.startTransaction();

    if(sampleId != null)
     if( !updateObjectTags(trn, sampleId, addTags, delTags, auxList, BioSDConfigManager.SAMPLE_CLASS_NAME, out) )
      return false;

    if(groupId != null)
     if( !updateObjectTags(trn, groupId, addTags, delTags, auxList, BioSDConfigManager.SAMPLEGROUP_CLASS_NAME, out) )
      return false;


    if(sbmId != null)
    {
     for(String id : sbmId)
     {
      cEnt.setEntityID(id);

      @SuppressWarnings("unchecked")
      List<TagRef> tags = (List<TagRef>) annotationManager.getAnnotation(trn, Topic.TAG, cEnt, false);

      tags = prepareTags(tags, addTags, delTags, auxList);

      if(tags == null)
       continue;

      if(tags.size() == 0)
       config.getAnnotationManager().removeAnnotation(trn, Topic.TAG, cEnt, false);
      else
       config.getAnnotationManager().addAnnotation(trn, Topic.TAG, cEnt, (Serializable) tags);
     }
    }

    annotationManager.commitTransaction(trn);
    ok = true;

    out.println("OK");

   }
   catch(Exception e)
   {
    out.println("ERROR: problem with tag storing: " + e.getMessage() + " (" + e.getClass().getName() + ")");
   }
   finally
   {
    if(!ok)
    {

     try
     {
      annotationManager.rollbackTransaction(trn);
     }
     catch(TransactionException e1)
     {
      out.println("ERROR: transaction rollback error");
      e1.printStackTrace();

     }

     return false;
    }
   }
  }
  
  ReadLock rLock = null;
  
  if( "on".equalsIgnoreCase(upReq.getParams().get(TagControlConstants.LIST_TAGS)) )
  {
   try
   {
    rLock = annotationManager.getReadLock();
    
    if(sampleId != null)
     if( !printObjectTags(rLock, sampleId, BioSDConfigManager.SAMPLE_CLASS_NAME, out) )
      return false;
    
    if( groupId != null )
     if( !printObjectTags(rLock, groupId, BioSDConfigManager.SAMPLEGROUP_CLASS_NAME, out) )
      return false;
     
    if( sbmId != null )
    {

     for(String id : sbmId)
     {
      cEnt.setEntityID(id);

      @SuppressWarnings("unchecked")
      List<TagRef> tags = (List<TagRef>) annotationManager.getAnnotation(rLock, Topic.TAG, cEnt, false);

      if(tags == null)
       return true;

      out.print("Submission " + id + ": ");

      int i = 0;
      for(TagRef tr : tags)
      {
       if(i++ != 0)
        out.print(';');

       out.print(tr.getClassiferName());
       out.print(':');
       out.print(tr.getTagName());

       if(tr.getTagValue() != null)
       {
        out.print('=');
        out.print(tr.getTagValue());
       }
      }

      out.print('\n');
     }
    }
   }
   catch(AnnotationDBException e)
   {
    out.println("ERROR: " + e.getMessage() + " (" + e.getClass().getName() + ")");
    return false;
   }
   finally
   {
    annotationManager.releaseLock(rLock);
   }
  }
  
  return true;

 }
 
 private List<TagRef> parseTags(String tagsStr)
 {
  tagsStr = tagsStr.trim();

  List<TagRef> tags = new ArrayList<TagRef>();
  
  for( String ts : StringUtil.splitString(tagsStr, ",") )
  {
   List<String> parts = StringUtil.splitString(ts, ":");
   
   if( parts.size() != 2 )
   {
    return null;
   }
   
   TagRef tr = new TagRef();
   tr.setClassiferName(parts.get(0).trim());
   
   String p2 = parts.get(1);
   
   int colPos = p2.indexOf('=');
   
   if( colPos != -1 )
   {
    tr.setTagName(p2.substring(0,colPos).trim() );
    tr.setTagValue(p2.substring(colPos+1).trim() );
   }
   else
    tr.setTagName(p2);
   
   tags.add(tr);
  }
  
  return tags;
 }
 
 private boolean checkTags( AuthDB authDB, List<TagRef> addTags, final PrintWriter out )
 {
   ReadLock authLock = authDB.getReadLock();

   try
   {
    boolean error = false;

    for(TagRef tr : addTags)
    {
     if(authDB.getTag(authLock, tr.getClassiferName(), tr.getTagName()) == null)
     {
      out.println("ERROR: invalid tag: " + tr.getClassiferName() + ":" + tr.getTagName());
      error = true;
     }
    }

    if(error)
     return false;
   }
   catch(TagException e)
   {
    out.println("ERROR: getTag method error: " + e.getMessage() + " (" + e.getClass().getName() + ")");
    return false;
   }
   finally
   {
    authDB.releaseLock(authLock);
   }

   return true;
  }
 
 private List<TagRef> prepareTags( List<TagRef> origTags,  List<TagRef> addTags,  List<TagRef> delTags,  List<TagRef> auxList )
 {
  boolean changed = false;
  
  if(delTags != null && origTags != null && origTags.size() > 0 )
  {
   for(TagRef tr : delTags)
   {
    int pos = Collections.binarySearch(origTags, tr);

    if(pos >= 0)
    {
     origTags.remove(pos);
     changed = true;
    }
   }
  }

  if(addTags != null)
  {
   if(origTags == null)
    origTags = new ArrayList<TagRef>();

   auxList.clear();

   for(TagRef tr : addTags)
   {
    int pos = Collections.binarySearch(origTags, tr);
    
    if(pos < 0)
     auxList.add(tr);
    else if( tr.getTagValue() != null || origTags.get(pos).getTagValue() != null )
    {
     origTags.set(pos, tr);
     changed = true;
    }
   }

   if(auxList.size() > 0)
   {
    changed = true;
    origTags.addAll(auxList);
   }
  }

  if(changed)
  {
   Collections.sort(origTags);
   return origTags;
  }
  
  if( origTags == null )
   return com.pri.util.collection.Collections.emptyList();
 
  return null;
 }
 
 private boolean updateObjectTags( Transaction trn, String[] ids, List<TagRef> addTags, List<TagRef> delTags, List<TagRef> auxList, String className, PrintWriter out) throws AnnotationDBException
 {
  AnnotationManager annotationManager = config.getAnnotationManager();

  AgeClass sampleClass = admin.getStorageAdmin().getSemanticModel().getDefinedAgeClass(className);

  for(String id : ids)
  {
   AgeObject sample = admin.getStorageAdmin().getGlobalObject(id);

   if(sample.getAgeElClass() != sampleClass)
   {
    out.println("Object with ID=" + id + " is not of class " + className);
    return false;
   }

   @SuppressWarnings("unchecked")
   List<TagRef> tags = (List<TagRef>) annotationManager.getAnnotation(trn, Topic.TAG, sample, false);

   tags = prepareTags(tags, addTags, delTags, auxList);

   if(tags == null)
    continue;

   if(tags.size() == 0)
    annotationManager.removeAnnotation(trn, Topic.TAG, sample, false);
   else
    annotationManager.addAnnotation(trn, Topic.TAG, sample, (Serializable) tags);
   
  }

  return true;
 }
 
 private boolean printObjectTags( ReadLock lck, String[] ids, String className, PrintWriter out) throws AnnotationDBException
 {
  AnnotationManager annotationManager = config.getAnnotationManager();

  AgeClass sampleClass = admin.getStorageAdmin().getSemanticModel().getDefinedAgeClass(className);

  for(String id : ids)
  {
   AgeObject sample = admin.getStorageAdmin().getGlobalObject(id);

   if(sample.getAgeElClass() != sampleClass)
   {
    out.println("Object with ID=" + id + " is not of class " + className);
    return false;
   }

   @SuppressWarnings("unchecked")
   List<TagRef> tags = (List<TagRef>) annotationManager.getAnnotation(lck, Topic.TAG, sample, false);

   if( tags == null )
    return true;

   out.print(className +" "+id+": ");
   
   int i=0;
   for( TagRef tr : tags )
   {
    if( i++ != 0)
     out.print(';');
    
    out.print(tr.getClassiferName());
    out.print(':');
    out.print(tr.getTagName());
    
    if( tr.getTagValue() != null )
    {
     out.print('=');
     out.print(tr.getTagValue());
    }
   }
   
   out.print('\n');
  }

  return true;
 }
}
