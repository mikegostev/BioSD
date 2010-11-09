package uk.ac.ebi.esd.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.age.model.AgeAttribute;
import uk.ac.ebi.age.model.AgeAttributeClass;
import uk.ac.ebi.age.model.AgeClass;
import uk.ac.ebi.age.model.AgeObject;
import uk.ac.ebi.age.model.AgeRelation;
import uk.ac.ebi.age.model.AgeRelationClass;
import uk.ac.ebi.age.query.AgeQuery;
import uk.ac.ebi.age.query.ClassNameExpression;
import uk.ac.ebi.age.query.ClassNameExpression.ClassType;
import uk.ac.ebi.age.storage.AgeStorage;
import uk.ac.ebi.age.storage.DataChangeListener;
import uk.ac.ebi.age.storage.index.AgeIndex;
import uk.ac.ebi.age.storage.index.TextFieldExtractor;
import uk.ac.ebi.age.storage.index.TextValueExtractor;
import uk.ac.ebi.esd.client.query.AttributedImprint;
import uk.ac.ebi.esd.client.query.GroupImprint;
import uk.ac.ebi.esd.client.query.Report;
import uk.ac.ebi.esd.client.query.SampleList;
import uk.ac.ebi.esd.client.shared.AttributeClassReport;

public class ESDServiceImpl extends ESDService
{
 private AgeStorage storage;
 
 private AgeIndex groupsIndex;
 private AgeIndex samplesIndex;
 
 private AgeQuery groupSelectQuery;
 private List<AgeObject> groupList;
 
 private AgeClass sampleClass;
 private AgeClass groupClass;
 private AgeRelationClass sampleInGroupRelClass;
 private AgeRelationClass groupToSampleRelClass;
 
 private AgeRelationClass groupToPublicationRelClass;
 private AgeRelationClass groupToContactRelClass;
 
 private AgeAttributeClass desciptionAttributeClass;
 private AgeAttributeClass commentAttributeClass;
 
 public ESDServiceImpl( AgeStorage stor )
 {
  storage=stor;
  
  sampleClass = storage.getSemanticModel().getDefinedAgeClass( ESDConfigManager.SAMPLE_CLASS_NAME );
  groupClass = storage.getSemanticModel().getDefinedAgeClass( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
  sampleInGroupRelClass = storage.getSemanticModel().getDefinedAgeRelationClass( ESDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME );
  desciptionAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( ESDConfigManager.DESCRIPTION_ATTR_CLASS_NAME );
  commentAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( ESDConfigManager.COMMENT_ATTR_CLASS_NAME );
  
  groupToPublicationRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(ESDConfigManager.HAS_PUBLICATION_REL_CLASS_NAME);
  groupToContactRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(ESDConfigManager.CONTACT_OF_REL_CLASS_NAME).getInverseRelationClass();
  
  if( sampleClass == null )
  {
   System.out.println("Can't find Sample class");
   return;
  }

  if( groupClass == null )
  {
   System.out.println("Can't find Group class");
   return;
  }
 
  if( desciptionAttributeClass == null )
  {
   System.out.println("Can't find "+ESDConfigManager.DESCRIPTION_ATTR_CLASS_NAME+" class");
   return;
  }
 
  if( sampleInGroupRelClass == null )
  {
   System.out.println("Can't find "+ESDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME+" relation");
   return;
  }
  
  groupToSampleRelClass = sampleInGroupRelClass.getInverseRelationClass();
  
//  OrExpression orExp = new OrExpression();
//  
//  ClassNameExpression clsExp = new ClassNameExpression();
//  clsExp.setClassName( ESDConfigManager.SAMPLE_CLASS_NAME );
//  clsExp.setClassType( ClassType.DEFINED );
//  
//  orExp.addExpression(clsExp);
  
  ClassNameExpression clsExp = new ClassNameExpression();
  clsExp.setClassName( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );

//  orExp.addExpression(clsExp);

  
  groupSelectQuery = AgeQuery.create(clsExp);
  
  ArrayList<TextFieldExtractor> extr = new ArrayList<TextFieldExtractor>(4);
  
  extr.add( new TextFieldExtractor(ESDConfigManager.SAMPLE_NAME_FIELD_NAME, new SampleAttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.SAMPLE_VALUE_FIELD_NAME, new SampleAttrValuesExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.GROUP_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.GROUP_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
  
  groupsIndex = storage.createTextIndex(groupSelectQuery, extr);

  Collection<AgeObject> grps = storage.executeQuery(groupSelectQuery);
  
  storage.addDataChangeListener( new DataChangeListener() 
  {
   @Override
   public void dataChanged()
   {
    Collection<AgeObject> grps = storage.executeQuery(groupSelectQuery);
    
    if( grps instanceof List<?> )
     groupList = (List<AgeObject>)grps;
    else
    {
     groupList = new ArrayList<AgeObject>( grps.size() );
     groupList.addAll(grps);
    }

   }
  } );
  
  if( grps instanceof List<?> )
   groupList = (List<AgeObject>)grps;
  else
  {
   groupList = new ArrayList<AgeObject>( grps.size() );
   groupList.addAll(grps);
  }
 
  clsExp = new ClassNameExpression();
  clsExp.setClassName( ESDConfigManager.SAMPLE_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );

  AgeQuery q = AgeQuery.create(clsExp);
  
  extr = new ArrayList<TextFieldExtractor>(3);
  
  extr.add( new TextFieldExtractor(ESDConfigManager.GROUP_ID_FIELD_NAME, new GroupIDExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.SAMPLE_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.SAMPLE_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
  
  samplesIndex = storage.createTextIndex(q, extr);
}

 @Override
 public Report selectSampleGroups(String query, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
 {
//  List<AgeObject> sel = null;
  
  if( query == null )
   return getAllGroups(offset, count);
  
  query=query.trim();
  
  if( query.length() == 0 )
   return getAllGroups(offset, count);
  
  StringBuilder sb = new StringBuilder();
  
//  sb.append("( ");
  
  if( searchAttrNm )
  {
   if( searchGrp )
    sb.append(ESDConfigManager.GROUP_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");

   if( searchSmp )
    sb.append(ESDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");
  }

  if( searchAttrVl )
  {
   if( searchGrp )
    sb.append(ESDConfigManager.GROUP_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");

   if( searchSmp )
    sb.append(ESDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");
  }
  
  sb.setLength(sb.length()-4);
//  sb.append(" )");
  
  String lucQuery = sb.toString();
  int qLen = lucQuery.length();
  
//  System.out.println("Query: "+lucQuery);
  
  List<AgeObject> sel = storage.queryTextIndex(groupsIndex, lucQuery );
  
  List<GroupImprint> res = new ArrayList<GroupImprint>();
  
  int lim = offset+count;
  
  if( lim > sel.size() )
   lim=sel.size();
  
  for( int i=offset; i< lim; i++ )
  {
   GroupImprint gr = createGroupObject(sel.get(i));
   
   if( searchSmp )
   {
    sb.setLength(qLen);
   
    sb.append(" AND "+ESDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(gr.getId());
    gr.setMatchedCount( storage.queryTextIndex(samplesIndex, sb.toString() ).size() );
   }
   
   res.add(gr);
  }
  
//  Map<AgeObject,ObjectReport> repMap = new HashMap<AgeObject, ObjectReport>();
//  
//  for( AgeObject obj : sel )
//  {
//   if( obj.getAgeElClass() == sampleClass && searchSmp )
//   {
//    AgeObject grpObj = getGroupForSample(obj);
//    
//    if( grpObj == null )
//     continue;
//    
//    ObjectReport sgRep = repMap.get(grpObj);
//    
//    if( sgRep == null )
//    {
//     sgRep = createGroupObject(grpObj);
//     
//     repMap.put(grpObj, sgRep);
//     res.add(sgRep);
//    }
//    
//    sgRep.addMatchedSample( obj.getId() );
//   }
//   else if( obj.getAgeElClass() == groupClass && searchGrp )
//   {
//    ObjectReport sgRep = repMap.get(obj);
//    
//    if( sgRep == null )
//    {
//     sgRep = createGroupObject(obj);
//
//     repMap.put(obj, sgRep);
//     res.add(sgRep);
//    }
//   }
//  }
  Report rep = new Report();
  rep.setObjects(res);
  rep.setTotalRecords(sel.size());
  
  return rep;
 }
 
 private GroupImprint createGroupObject( AgeObject obj )
 {
  GroupImprint sgRep = new GroupImprint();

  sgRep.setId( obj.getId() );

  sgRep.addAttribute("Submission ID", obj.getSubmission().getId(), true, 0);
  sgRep.addAttribute("ID", obj.getId(), true, 0);
  
  Object descVal = obj.getAttributeValue(desciptionAttributeClass);
  sgRep.setDescription( descVal!=null?descVal.toString():null );
  
 
  for( AgeAttribute atr : obj.getAttributes() )
  {
   AgeAttributeClass atCls = atr.getAgeElClass();
   
   if( atCls.isClassOrSubclass( commentAttributeClass ) )
   {
    sgRep.addOtherInfo( atCls.getName(), atr.getValue().toString() );
   }
   else
    sgRep.addAttribute(atCls.getName(), atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
  }
  
  Collection<? extends AgeRelation> pubRels =  obj.getRelationsByClass(groupToPublicationRelClass, false);

  if( pubRels != null )
  {
   for( AgeRelation pRel : pubRels )
    sgRep.addPublication( createAttributedObject(pRel.getTargetObject()) );
  }

  Collection<? extends AgeRelation> persRels =  obj.getRelationsByClass(groupToContactRelClass, false);

  if( persRels != null )
  {
   for( AgeRelation pRel : persRels )
    sgRep.addContact( createAttributedObject(pRel.getTargetObject()) );
  }

  
  Collection<? extends AgeRelation> rels =  obj.getRelationsByClass(groupToSampleRelClass, false);
  
  int sCount=0;
  if( rels != null )
  {
   for( AgeRelation rl : rels )
    if( rl.getTargetObject().getAgeElClass() == sampleClass )
     sCount++;
  }
  
  sgRep.setRefCount( sCount );
  
  return sgRep;
 }

 private AttributedImprint createAttributedObject(AgeObject ageObj)
 {
  AttributedImprint obj = new AttributedImprint();
  
  if( ageObj.getAttributes() != null )
  {
   for( AgeAttribute attr : ageObj.getAttributes() )
   {
    obj.addAttribute(attr.getAgeElClass().getName(), attr.getValue().toString(), attr.getAgeElClass().isCustom(), attr.getOrder());
   }
  }
  
  return obj;
 }
 
// private AgeObject getGroupForSample(AgeObject obj)
// {
//  for(AgeRelation rel : obj.getRelations() )
//  {
//   if( rel.getAgeElClass() == sampleInGroupRelClass && rel.getTargetObject().getAgeElClass() == groupClass )
//    return rel.getTargetObject();
//  }
//  
//  return null;
// }



 public void shutdown()
 {
  storage.shutdown();
 }
 
 class AttrValuesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
 
  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);
   
   for( AgeAttribute attr : gobj.getAttributes() )
   {
    Object val = attr.getValue();
    
    if( val instanceof String )
     sb.append( val ).append(' ');
    
    if( attr.getAttributes() != null )
    {
     for( AgeAttribute qual : attr.getAttributes() )
     {
      Object qval = qual.getValue();
      
      if( qval instanceof String )
       sb.append( qval ).append(' ');
     }
    }

   }
    
   return sb.toString();
  }
 }
 
 class SampleAttrValuesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
 
  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);

   for(AgeRelation rel : gobj.getRelations())
   {
    if(rel.getAgeElClass() == groupToSampleRelClass)
    {
     AgeObject obj = rel.getTargetObject();

     for(AgeAttribute attr : obj.getAttributes())
     {
      Object val = attr.getValue();
      
      if( val instanceof String )
       sb.append( val ).append(' ');

      if(attr.getAttributes() != null)
      {
       for(AgeAttribute qual : attr.getAttributes() )
       {
        Object qval = qual.getValue();
        
        if( qval instanceof String )
         sb.append( qval ).append(' ');
       }
      }
     }
    }
   }
   return sb.toString();
  }
 }
 
 class SampleAttrNamesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();

  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);

   for(AgeRelation rel : gobj.getRelations())
   {
    if(rel.getAgeElClass() == groupToSampleRelClass)
    {
     AgeObject obj = rel.getTargetObject();

     for(AgeAttribute attr : obj.getAttributes())
     {
      sb.append(attr.getAgeElClass().getName()).append(' ');

      if(attr.getAttributes() != null)
      {
       for(AgeAttribute qual : attr.getAttributes())
       {
        sb.append(qual.getAgeElClass().getName()).append(' ');
       }
      }
     }
    }
   }

   return sb.toString();
  }
 }

 class AttrNamesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
 
  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);
   
   for( AgeAttribute attr : gobj.getAttributes() )
   {
    sb.append( attr.getAgeElClass().getName() ).append(' ');
    
    if( attr.getAttributes() != null )
    {
     for( AgeAttribute qual : attr.getAttributes() )
     {
      sb.append( qual.getAgeElClass().getName() ).append(' ');
     }
    }
   }
    
   return sb.toString();
  }
 }
 
 class GroupIDExtractor implements TextValueExtractor
 {
 
  public String getValue(AgeObject gobj)
  {
   for(AgeRelation rel : gobj.getRelations())
   {
    if(rel.getAgeElClass() == sampleInGroupRelClass)
    {
     return rel.getTargetObject().getId();
    }
   }
   return "";
  }
 }

 
 private SampleList createSampleReport(List<AgeObject> samples)
 {
  int id=1;
  
  SampleList sl = new SampleList();
  
  Map<AgeAttributeClass, AttributeClassReport > valMap = new HashMap<AgeAttributeClass, AttributeClassReport>();
  
  for( AgeObject smpl : samples )
  {
   Map<String,String> attrMap = new HashMap<String, String>(); 
   
   attrMap.put("__id",smpl.getId());
   
   for( AgeAttribute attr : smpl.getAttributes() )
   {
    AgeAttributeClass ageAtCls = attr.getAgeElClass();
    String attrval = attr.getValue().toString();
    
    AttributeClassReport atCls = valMap.get(ageAtCls);
    
    if( atCls == null )
    {
     atCls = new AttributeClassReport();
     
     atCls.setCustom( ageAtCls.isCustom() );
     atCls.setName( ageAtCls.getName() );
     atCls.setId("AttrClass"+(id++));
     
     valMap.put(attr.getAgeElClass(), atCls);
    }
    
    atCls.addValue(attrval);
    
    attrMap.put(atCls.getId(), attrval);
   }
   
   sl.addSample( attrMap );
  }
  
  List<AttributeClassReport> clsLst = new ArrayList<AttributeClassReport>( valMap.size()+1 );
  clsLst.addAll(valMap.values());
  
  Collections.sort(clsLst, new Comparator<AttributeClassReport>()
  {
   @Override
   public int compare(AttributeClassReport arg0, AttributeClassReport arg1)
   {
    if( arg0.getName().equals("Name") )
    {
     if( arg1.getName().equals("Name") )
      return 0;
     else
      return -1;
    }
    else if( arg1.getName().equals("Name") )
     return 1;
    
    return arg1.getValues().size()-arg0.getValues().size();
   }
  });
  
  for( AttributeClassReport atr : clsLst )
   atr.clearValues();
  
  AttributeClassReport idCls = new AttributeClassReport();
  
  idCls.setCustom(false);
  idCls.setName("ID");
  idCls.setId("__id");
  
  clsLst.add(idCls);
  
  sl.setHeader( clsLst );
  
  return sl;
 }

 
 @Override
 public SampleList getSamplesByGroup(String grpID, int offset, int count)
 {
  AgeObject grpObj = storage.getObjectById(grpID);
  
  if( grpObj == null )
   return null;
  
  List<AgeObject> res = new ArrayList<AgeObject>(count);
  
  int total=0;
  
  for( AgeRelation rel : grpObj.getRelations() )
  {
   if( rel.getAgeElClass() == groupToSampleRelClass )
   {
    total++;
    
    if( offset > 0 )
    {
     offset--;
     continue;
    }
 
    if( count == 0 )
     continue;
   
    count--;
    
    res.add(rel.getTargetObject());
    
   }
  }
  
  SampleList sl = createSampleReport(res);
  sl.setTotalRecords(total);
  
  return sl;
 }
 
 public Report getSamplesByGroupX(String grpID, int offset, int count)
 {
  AgeObject grpObj = storage.getObjectById(grpID);
  
  if( grpObj == null )
   return null;
  
  List<GroupImprint> res = new ArrayList<GroupImprint>(30);
  
  int total=0;
  
  for( AgeRelation rel : grpObj.getRelations() )
  {
   if( rel.getAgeElClass() == groupToSampleRelClass )
   {
    total++;
    
    if( offset > 0 )
    {
     offset--;
     continue;
    }
 
    if( count == 0 )
     continue;
   
    count--;
    
    AgeObject sample = rel.getTargetObject();
    
    GroupImprint rp = new GroupImprint();
    
    rp.setId(sample.getId());
    
    
    for( AgeAttribute attr : sample.getAttributes() )
    {
     String attrname = attr.getAgeElClass().getName();
     
     rp.addAttribute( attrname, attr.getValue().toString(), attr.getAgeElClass().isCustom(),attr.getOrder());
     
//     if( attr.getAttributes() != null )
//     {
//      for( AgeAttribute qlf  : attr.getAttributes() )
//       rp.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), attr.getAgeElClass().isCustom(), qlf.getOrder());
//     }
    }

    rp.addAttribute( "ID", sample.getId(), true, 0);
    
    res.add( rp );
   }
  }
  
  Report rep = new Report();
  rep.setObjects(res);
  rep.setTotalRecords(total);
  
  return rep;
 }

 @Override
 public SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
 {
  StringBuilder sb = new StringBuilder();
  
  sb.append("( ");
  
  if( searchAttrNm )
   sb.append(ESDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');

  if( searchAttrVl )
  {
   if( sb.length() > 0 )
    sb.append(" OR ");
   
   sb.append(ESDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
  }
  
  sb.append(" ) AND ").append(ESDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
  
  List<AgeObject> sel = storage.queryTextIndex(samplesIndex, sb.toString() );
  
 
  if( offset > sel.size())
   return null;
  
  int end = (offset+count) > sel.size()? sel.size() : (offset+count); 
  
  SampleList lst = createSampleReport( sel.subList(offset, end) );
  lst.setTotalRecords(sel.size());
  
  return lst;
 }
 
 public Report getSamplesByGroupAndQueryX(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
 {
  StringBuilder sb = new StringBuilder();
  
  sb.append("( ");
  
  if( searchAttrNm )
   sb.append(ESDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');

  if( searchAttrVl )
  {
   if( sb.length() > 0 )
    sb.append(" OR ");
   
   sb.append(ESDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
  }
  
  sb.append(" ) AND ").append(ESDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
  
  List<AgeObject> sel = storage.queryTextIndex(samplesIndex, sb.toString() );
  
  List<GroupImprint> res = new ArrayList<GroupImprint>();
 
  int total = res.size();
  
  for( AgeObject obj : sel )
  {
   if( offset > 0 )
   {
    offset--;
    continue;
   }

   if( count == 0 )
    continue;
  
   count--;

   GroupImprint sampRep = new GroupImprint();

   sampRep.setId( obj.getId() );

   sampRep.addAttribute("ID", obj.getId(), true, 0);
   
   Object descVal = obj.getAttributeValue(desciptionAttributeClass);
   sampRep.setDescription( descVal!=null?descVal.toString():null );
   
   for( AgeAttribute atr : obj.getAttributes() )
   {
    String attrname = atr.getAgeElClass().getName();

    sampRep.addAttribute(attrname, atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());

    if( atr.getAttributes() != null )
    {
     for( AgeAttribute qlf  : atr.getAttributes() )
      sampRep.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), atr.getAgeElClass().isCustom(), qlf.getOrder());
    }

   }
   res.add(sampRep);
  }
  
//  for( AgeObject obj : sel )
//  {
//   if( obj.getAgeElClass() == sampleClass )
//   {
//    AgeObject grpObj = getGroupForSample(obj);
//    
//    if( grpObj == null || ! grpObj.getId().equals(grpId)  )
//     continue;
//    
//    ObjectReport sampRep = new ObjectReport();
//
//     sampRep.setId( obj.getId() );
//
//     sampRep.addAttribute("ID", obj.getId(), true, 0);
//     
//     Object descVal = obj.getAttributeValue(desciptionAttributeClass);
//     sampRep.setDescription( descVal!=null?descVal.toString():null );
//     
//     for( AgeAttribute atr : obj.getAttributes() )
//     {
//      String attrname = atr.getAgeElClass().getName();
//
//      sampRep.addAttribute(attrname, atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
//
//      if( atr.getQualifiers() != null )
//      {
//       for( AgeAttribute qlf  : atr.getQualifiers() )
//        sampRep.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), atr.getAgeElClass().isCustom(), qlf.getOrder());
//      }
//
//     }
//     res.add(sampRep);
//   }
//  }
  Report rep = new Report();
  rep.setObjects(res);
  rep.setTotalRecords(total);
  
  return rep;
 }

 @Override
 public Report getAllGroups(int offset, int count)
 {
  int lim = offset+count;
  
  if( lim > groupList.size() )
   lim=groupList.size();
  
  List<GroupImprint> res = new ArrayList<GroupImprint>(count);
  
  for( ; offset < lim; offset++)
   res.add( createGroupObject(groupList.get(offset)) );
  
  Report rep = new Report();
  rep.setObjects(res);
  rep.setTotalRecords(groupList.size());
  
  return rep;
 }


}
