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
import uk.ac.ebi.age.admin.shared.SubmissionConstants;
import uk.ac.ebi.age.annotation.AnnotationManager;
import uk.ac.ebi.age.annotation.Topic;
import uk.ac.ebi.age.authz.ACR.Permit;
import uk.ac.ebi.age.authz.AuthDB;
import uk.ac.ebi.age.authz.BuiltInUsers;
import uk.ac.ebi.age.authz.Session;
import uk.ac.ebi.age.authz.exception.TagException;
import uk.ac.ebi.age.ext.authz.SystemAction;
import uk.ac.ebi.age.ext.authz.TagRef;
import uk.ac.ebi.age.ext.entity.ClusterEntity;
import uk.ac.ebi.age.ext.submission.SubmissionDBException;
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
  
  AuthDB authDB = config.getAuthDB();

  String tagsStr = upReq.getParams().get(SubmissionConstants.SUBMISSON_TAGS);

  List<TagRef> addTags = null;

  if(tagsStr != null)
  {
   addTags = parseTags(tagsStr);

   if(addTags == null)
   {
    out.println("ERROR: Invalid tag string: " + tagsStr);
    return false;
   }

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

  }

  tagsStr = upReq.getParams().get(SubmissionConstants.SUBMISSON_TAGS_RM);

  List<TagRef> delTags = null;

  if(tagsStr != null)
  {
   delTags = parseTags(tagsStr);

   if(delTags == null)
   {
    out.println("ERROR: Invalid tag string: " + tagsStr);
    return false;
   }

   ReadLock authLock = authDB.getReadLock();

   try
   {
    boolean error = false;

    for(TagRef tr : delTags)
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

  }

  AnnotationManager annotationManager = config.getAnnotationManager();

  ClusterEntity cEnt = new ClusterEntity();

  List<TagRef> addTagsSel = new ArrayList<TagRef>();

  Transaction trn = annotationManager.startTransaction();

  try
  {

   for(String id : sbmId)
   {
    boolean changed = false;

    cEnt.setEntityID(id);

    @SuppressWarnings("unchecked")
    List<TagRef> tags = (List<TagRef>) annotationManager.getAnnotation(trn, Topic.TAG, cEnt, false);

    if(tags == null)
     tags = new ArrayList<TagRef>();

    if(delTags != null)
    {
     for(TagRef tr : delTags)
     {
      int pos = Collections.binarySearch(tags, tr);

      if(pos >= 0)
      {
       tags.remove(pos);
       changed = true;
      }
     }
    }

    if(addTags != null)
    {
     addTagsSel.clear();

     for(TagRef tr : addTags)
     {
      int pos = Collections.binarySearch(tags, tr);
      
      if(pos < 0)
       addTagsSel.add(tr);
      else if( tr.getTagValue() != null || tags.get(pos).getTagValue() != null )
      {
       tags.set(pos, tr);
       changed = true;
      }
     }

     if(addTagsSel.size() > 0)
     {
      changed = true;
      tags.addAll(addTagsSel);
     }
    }

    if(changed)
    {
     Collections.sort(tags);
     config.getAnnotationManager().addAnnotation(trn, Topic.TAG, cEnt, (Serializable) tags);
    }
   }
   
   annotationManager.commitTransaction(trn);

   out.println("OK");
 
  }
  catch(Exception e)
  {
   out.println("ERROR: problem with tag storing: " + e.getMessage() + " (" + e.getClass().getName() + ")");

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
}
