package uk.ac.ebi.esd.server.service;

import java.util.ArrayList;
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
import uk.ac.ebi.age.query.OrExpression;
import uk.ac.ebi.age.query.ClassNameExpression.ClassType;
import uk.ac.ebi.age.storage.AgeStorage;
import uk.ac.ebi.age.storage.index.AgeIndex;
import uk.ac.ebi.age.storage.index.TextFieldExtractor;
import uk.ac.ebi.age.storage.index.TextValueExtractor;
import uk.ac.ebi.esd.client.query.ObjectReport;

public class ESDServiceImpl extends ESDService
{
 private AgeStorage storage;
 
 private AgeIndex attrTextIndex;
 
 private AgeClass sampleClass;
 private AgeClass groupClass;
 private AgeRelationClass sampleInGroupRelClass;
 private AgeRelationClass groupToSampleRelClass;
 
 private AgeAttributeClass desciptionAttributeClass;
 
 public ESDServiceImpl( AgeStorage stor )
 {
  storage=stor;
  
  sampleClass = storage.getSemanticModel().getDefinedAgeClass( ESDConfigManager.SAMPLE_CLASS_NAME );
  groupClass = storage.getSemanticModel().getDefinedAgeClass( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
  sampleInGroupRelClass = storage.getSemanticModel().getDefinedAgeRelationClass( ESDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME );
  desciptionAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( ESDConfigManager.DESCRIPTION_ATTR_CLASS_NAME );
  
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
  
  groupToSampleRelClass = sampleInGroupRelClass.getInverseClass();
  
  OrExpression orExp = new OrExpression();
  
  ClassNameExpression clsExp = new ClassNameExpression();
  clsExp.setClassName( ESDConfigManager.SAMPLE_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );
  
  orExp.addExpression(clsExp);
  
  clsExp = new ClassNameExpression();
  clsExp.setClassName( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );

  orExp.addExpression(clsExp);

  
  AgeQuery q = AgeQuery.create(orExp);
  
  ArrayList<TextFieldExtractor> extr = new ArrayList<TextFieldExtractor>(2);
  
  extr.add( new TextFieldExtractor(ESDConfigManager.NAME_FIELD_NAME, new AttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(ESDConfigManager.VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
  
  attrTextIndex = storage.createTextIndex(q, extr);
 }

 @Override
 public List<ObjectReport> selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl)
 {
  StringBuilder sb = new StringBuilder();
  
  if( searchAttrNm )
   sb.append(ESDConfigManager.NAME_FIELD_NAME).append(":(").append(value).append(')');

  if( searchAttrVl )
  {
   if( sb.length() > 0 )
    sb.append(" OR ");
   
   sb.append(ESDConfigManager.VALUE_FIELD_NAME).append(":(").append(value).append(')');
  }
  
  List<AgeObject> sel = storage.queryTextIndex(attrTextIndex, sb.toString() );
  
  List<ObjectReport> res = new ArrayList<ObjectReport>();
  
  Map<AgeObject,ObjectReport> repMap = new HashMap<AgeObject, ObjectReport>();
  
  for( AgeObject obj : sel )
  {
   if( obj.getAgeElClass() == sampleClass && searchSmp )
   {
    AgeObject grpObj = getGroupForSample(obj);
    
    if( grpObj == null )
     continue;
    
    ObjectReport sgRep = repMap.get(grpObj);
    
    if( sgRep == null )
    {
     sgRep = new ObjectReport();

     sgRep.setId( grpObj.getId() );

     sgRep.addAttribute("Submission ID", grpObj.getSubmission().getId(), true, 0);
     sgRep.addAttribute("ID", grpObj.getId(), true, 0);
     
     Object descVal = grpObj.getAttributeValue(desciptionAttributeClass);
     sgRep.setDescription( descVal!=null?descVal.toString():null );
     
     repMap.put(grpObj, sgRep);
    
     for( AgeAttribute atr : grpObj.getAttributes() )
      sgRep.addAttribute(atr.getAgeElClass().getName(), atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
     
     res.add(sgRep);
    }
    
    sgRep.addMatchedSample( obj.getId() );
   }
   else if( obj.getAgeElClass() == groupClass && searchGrp )
   {
    ObjectReport sgRep = repMap.get(obj);
    
    if( sgRep == null )
    {
     sgRep = new ObjectReport();

     sgRep.setId( obj.getId() );

     sgRep.addAttribute("Submission ID", obj.getSubmission().getId(), true, 0);
     sgRep.addAttribute("ID", obj.getId(), true, 0);
     
     Object descVal = obj.getAttributeValue(desciptionAttributeClass);
     sgRep.setDescription( descVal!=null?descVal.toString():null );
     
     repMap.put(obj, sgRep);
    
     res.add(sgRep);
    }
   }
  }
  
  
  return res;
 }
 
 private AgeObject getGroupForSample(AgeObject obj)
 {
  for(AgeRelation rel : obj.getRelations() )
  {
   if( rel.getAgeElClass() == sampleInGroupRelClass && rel.getTargetObject().getAgeElClass() == groupClass )
    return rel.getTargetObject();
  }
  
  return null;
 }


 public void shutdown()
 {
  storage.shutdown();
 }
 
 class AttrValuesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
 
  public String getValue(AgeObject obj)
  {
   sb.setLength(0);
   
   for( AgeAttribute attr : obj.getAttributes() )
   {
    Object val = attr.getValue();
    
    if( val instanceof String )
     sb.append( val ).append(' ');
    
    if( attr.getQualifiers() != null )
    {
     for( AgeAttribute qual : attr.getQualifiers() )
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
 
 class AttrNamesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
 
  public String getValue(AgeObject obj)
  {
   sb.setLength(0);
   
   for( AgeAttribute attr : obj.getAttributes() )
   {
    sb.append( attr.getAgeElClass().getName() ).append(' ');
    
    if( attr.getQualifiers() != null )
    {
     for( AgeAttribute qual : attr.getQualifiers() )
     {
      sb.append( qual.getAgeElClass().getName() ).append(' ');
     }
    }
   }
    
   return sb.toString();
  }
 }

 @Override
 public List<ObjectReport> getSamplesByGroup(String grpID)
 {
  AgeObject grpObj = storage.getObjectById(grpID);
  
  if( grpObj == null )
   return null;
  
  List<ObjectReport> res = new ArrayList<ObjectReport>(30);
  
  for( AgeRelation rel : grpObj.getRelations() )
  {
   if( rel.getAgeElClass() == groupToSampleRelClass )
   {
    AgeObject sample = rel.getTargetObject();
    
    ObjectReport rp = new ObjectReport();
    
    rp.setId(sample.getId());
    
    rp.addAttribute( "ID", sample.getId(), true, 0);
    
    for( AgeAttribute attr : sample.getAttributes() )
    {
     String attrname = attr.getAgeElClass().getName();
     
     rp.addAttribute( attrname, attr.getValue().toString(), attr.getAgeElClass().isCustom(),attr.getOrder());
     
     if( attr.getQualifiers() != null )
     {
      for( AgeAttribute qlf  : attr.getQualifiers() )
       rp.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), attr.getAgeElClass().isCustom(), qlf.getOrder());
     }
    }
    
    res.add( rp );
   }
  }
  
  return res;
 }

 @Override
 public List<ObjectReport> getSamplesByGroupAndQuery(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl)
 {
  StringBuilder sb = new StringBuilder();
  
  if( searchAttrNm )
   sb.append(ESDConfigManager.NAME_FIELD_NAME).append(":(").append(query).append(')');

  if( searchAttrVl )
  {
   if( sb.length() > 0 )
    sb.append(" OR ");
   
   sb.append(ESDConfigManager.VALUE_FIELD_NAME).append(":(").append(query).append(')');
  }
  
  List<AgeObject> sel = storage.queryTextIndex(attrTextIndex, sb.toString() );
  
  List<ObjectReport> res = new ArrayList<ObjectReport>();
  
  
  for( AgeObject obj : sel )
  {
   if( obj.getAgeElClass() == sampleClass )
   {
    AgeObject grpObj = getGroupForSample(obj);
    
    if( grpObj == null || ! grpObj.getId().equals(grpId)  )
     continue;
    
    ObjectReport sampRep = new ObjectReport();

     sampRep.setId( obj.getId() );

     sampRep.addAttribute("ID", obj.getId(), true, 0);
     
     Object descVal = obj.getAttributeValue(desciptionAttributeClass);
     sampRep.setDescription( descVal!=null?descVal.toString():null );
     
     for( AgeAttribute atr : obj.getAttributes() )
     {
      String attrname = atr.getAgeElClass().getName();

      sampRep.addAttribute(attrname, atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());

      if( atr.getQualifiers() != null )
      {
       for( AgeAttribute qlf  : atr.getQualifiers() )
        sampRep.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), atr.getAgeElClass().isCustom(), qlf.getOrder());
      }

     }
     res.add(sampRep);
   }
  }
  
  
  return res;
 }


}
