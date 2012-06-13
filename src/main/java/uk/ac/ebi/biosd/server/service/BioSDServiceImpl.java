package uk.ac.ebi.biosd.server.service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;

import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.annotation.AnnotationManager;
import uk.ac.ebi.age.annotation.Topic;
import uk.ac.ebi.age.authz.BuiltInUsers;
import uk.ac.ebi.age.authz.PermissionManager;
import uk.ac.ebi.age.authz.SecurityChangedListener;
import uk.ac.ebi.age.ext.annotation.AnnotationDBException;
import uk.ac.ebi.age.ext.authz.SystemAction;
import uk.ac.ebi.age.ext.authz.TagRef;
import uk.ac.ebi.age.ext.entity.Entity;
import uk.ac.ebi.age.model.AgeAttribute;
import uk.ac.ebi.age.model.AgeAttributeClass;
import uk.ac.ebi.age.model.AgeClass;
import uk.ac.ebi.age.model.AgeObject;
import uk.ac.ebi.age.model.AgeObjectAttribute;
import uk.ac.ebi.age.model.AgeRelation;
import uk.ac.ebi.age.model.AgeRelationClass;
import uk.ac.ebi.age.model.Attributed;
import uk.ac.ebi.age.model.DataType;
import uk.ac.ebi.age.query.AgeQuery;
import uk.ac.ebi.age.query.ClassNameExpression;
import uk.ac.ebi.age.query.ClassNameExpression.ClassType;
import uk.ac.ebi.age.storage.AgeStorage;
import uk.ac.ebi.age.storage.DataChangeListener;
import uk.ac.ebi.age.storage.MaintenanceModeListener;
import uk.ac.ebi.age.storage.exeption.IndexIOException;
import uk.ac.ebi.age.storage.index.KeyExtractor;
import uk.ac.ebi.age.storage.index.SortedTextIndex;
import uk.ac.ebi.age.storage.index.TextFieldExtractor;
import uk.ac.ebi.age.storage.index.TextIndex;
import uk.ac.ebi.age.storage.index.TextValueExtractor;
import uk.ac.ebi.age.ui.server.imprint.ImprintBuilder;
import uk.ac.ebi.age.ui.server.imprint.ImprintBuilder.StringProcessor;
import uk.ac.ebi.age.ui.server.imprint.ImprintingHint;
import uk.ac.ebi.age.ui.shared.imprint.AttributeImprint;
import uk.ac.ebi.age.ui.shared.imprint.ClassImprint;
import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;
import uk.ac.ebi.age.ui.shared.imprint.ObjectValue;
import uk.ac.ebi.age.ui.shared.imprint.Value;
import uk.ac.ebi.biosd.client.query.AttributedImprint;
import uk.ac.ebi.biosd.client.query.AttributedObject;
import uk.ac.ebi.biosd.client.query.GroupImprint;
import uk.ac.ebi.biosd.client.query.Report;
import uk.ac.ebi.biosd.client.query.SampleList;
import uk.ac.ebi.biosd.client.shared.MaintenanceModeException;
import uk.ac.ebi.biosd.server.stat.BioSDStat;
import uk.ac.ebi.mg.assertlog.Log;
import uk.ac.ebi.mg.assertlog.LogFactory;

import com.pri.util.ArrayObjectRecycler;
import com.pri.util.StringUtils;

public class BioSDServiceImpl extends BioSDService implements SecurityChangedListener
{
 private static Log log = LogFactory.getLog(BioSDServiceImpl.class);
 
 private AgeStorage storage;
 
 private static final String GROUP_INDEX_NAME="GROUPINDEX";
 private static final String SAMPLE_INDEX_NAME="SAMPLEINDEX";
 
 private SortedTextIndex<GroupKey> groupsIndex;
 private TextIndex samplesIndex;
 
 private AgeQuery groupSelectQuery;
// private List<AgeObject> groupList;
 
 private AgeClass sampleClass;
 private AgeClass groupClass;
 private AgeRelationClass sampleInGroupRelClass;
 private AgeRelationClass groupToSampleRelClass;
 
 private AgeRelationClass groupToPublicationRelClass;
 private AgeRelationClass groupToContactRelClass;
 
 private AgeAttributeClass desciptionAttributeClass;
 private AgeAttributeClass commentAttributeClass;
 private AgeAttributeClass dataSourceAttributeClass;
 private AgeAttributeClass referenceAttributeClass;
 
 private BioSDStat statistics;
 
 private volatile boolean  maintenanceMode = false;
 
 private WeakHashMap<String, UserCacheObject> userCache  = new WeakHashMap<String, UserCacheObject>();
 
 private Analyzer analizer = new StandardAnalyzer(Version.LUCENE_30);
 private QueryParser queryParser = new QueryParser( Version.LUCENE_30, BioSDConfigManager.GROUP_VALUE_FIELD_NAME, analizer );
 private SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span class='sHL'>","</span>");

 private ImprintingHint sampleConvHint;
 
 private static class GroupKey
 {
  String grpName;
  boolean refGroup;
 }
 
 private Comparator<GroupKey> groupComparator = new Comparator<GroupKey>()
 {
  @Override
  public int compare(GroupKey o1, GroupKey o2)
  {
 
   if( o1.refGroup == o2.refGroup )
    return StringUtils.naturalCompare(o1.grpName, o1.grpName);

   return o1.refGroup?-1:1;
   
  }
 };
 

 private ImprintBuilder.StringProcessor htmlEscProc = new StringProcessor()
 {
  @Override
  public String process(String str)
  {
   return StringUtils.htmlEscaped(str);
  }
 };
 
 public BioSDServiceImpl( AgeStorage stor ) throws BioSDInitException
 {
  long startTime=0;
  
  storage=stor;
  
  sampleClass = storage.getSemanticModel().getDefinedAgeClass( BioSDConfigManager.SAMPLE_CLASS_NAME );
  groupClass = storage.getSemanticModel().getDefinedAgeClass( BioSDConfigManager.SAMPLEGROUP_CLASS_NAME );
  sampleInGroupRelClass = storage.getSemanticModel().getDefinedAgeRelationClass( BioSDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME );
  desciptionAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.DESCRIPTION_ATTR_CLASS_NAME );
  commentAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.COMMENT_ATTR_CLASS_NAME );
  dataSourceAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.DATASOURCE_ATTR_CLASS_NAME );
  referenceAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.REFERENCE_ATTR_CLASS_NAME );
  
  groupToPublicationRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(BioSDConfigManager.HAS_PUBLICATION_REL_CLASS_NAME);
  groupToContactRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(BioSDConfigManager.CONTACT_OF_REL_CLASS_NAME).getInverseRelationClass();
  
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
   System.out.println("Can't find "+BioSDConfigManager.DESCRIPTION_ATTR_CLASS_NAME+" class");
   return;
  }
 
  if( sampleInGroupRelClass == null )
  {
   System.out.println("Can't find "+BioSDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME+" relation");
   return;
  }
  
  if( referenceAttributeClass == null )
  {
   System.out.println("Can't find "+BioSDConfigManager.REFERENCE_ATTR_CLASS_NAME+" attribute class");
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
  
  sampleConvHint = new ImprintingHint();
  sampleConvHint.setConvertRelations(false);
  sampleConvHint.setConvertAttributes(true);
  sampleConvHint.setQualifiersDepth(2);
  sampleConvHint.setResolveObjectAttributesTarget(false);
  
  ClassNameExpression clsExp = new ClassNameExpression();
  clsExp.setClassName( BioSDConfigManager.SAMPLEGROUP_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );

//  orExp.addExpression(clsExp);

  
  groupSelectQuery = AgeQuery.create(clsExp);
  
  ArrayList<TextFieldExtractor> extr = new ArrayList<TextFieldExtractor>(4);
  
  TagsExtractor tagExtr = new TagsExtractor();
  OwnerExtractor ownExtr = new OwnerExtractor();
  
  extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME, new SampleAttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME, new SampleAttrValuesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_REFERENCE_FIELD_NAME, new RefGroupExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.SECTAGS_FIELD_NAME, tagExtr ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.OWNER_FIELD_NAME, ownExtr ) );
  
  assert ( startTime = System.currentTimeMillis() ) != 0;

  long idxTime=0;

  try
  {
   assert ( idxTime = System.currentTimeMillis() ) != 0;

    groupsIndex = storage.createSortedTextIndex(GROUP_INDEX_NAME, groupSelectQuery, extr, new KeyExtractor<GroupKey>(){

    ArrayObjectRecycler<GroupKey> fact = new ArrayObjectRecycler<GroupKey>(4);
    
    @Override
    public GroupKey extractKey(AgeObject o1)
    {
     GroupKey k = fact.getObject();

     if(k == null)
      k = new GroupKey();

     AgeAttribute ref1 = o1.getAttribute(referenceAttributeClass);

     k.grpName = o1.getId();
     k.refGroup = ref1 != null ? ref1.getValueAsBoolean() : false;

     return k;
    }

    @Override
    public void recycleKey(GroupKey k)
    {
     fact.recycleObject(k);
    }},
    
    groupComparator);
   
   assert log.info("Group index building time: "+StringUtils.millisToString(System.currentTimeMillis()-idxTime));

  }
  catch(IndexIOException e)
  {
   throw new BioSDInitException("Init failed. Can't create group index",e);
  }

  
  clsExp = new ClassNameExpression();
  clsExp.setClassName( BioSDConfigManager.SAMPLE_CLASS_NAME );
  clsExp.setClassType( ClassType.DEFINED );

  AgeQuery q = AgeQuery.create(clsExp);
  
  extr = new ArrayList<TextFieldExtractor>(3);
  
  extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_ID_FIELD_NAME, new GroupIDExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.SECTAGS_FIELD_NAME, tagExtr ) );
  extr.add( new TextFieldExtractor(BioSDConfigManager.OWNER_FIELD_NAME, ownExtr ) );
  
  try
  {
   assert ( idxTime = System.currentTimeMillis() ) != 0;
   
   samplesIndex = storage.createTextIndex(SAMPLE_INDEX_NAME, q, extr);
   
   assert log.info("Sample index building time: "+StringUtils.millisToString(System.currentTimeMillis()-idxTime));

  }
  catch(IndexIOException e)
  {
   throw new BioSDInitException("Init failed. Can't create group index",e);
  }
  
  assert log.info("Indices building time: "+StringUtils.millisToString(System.currentTimeMillis()-startTime)+" Tag extraction: "+tagExtr.getTime()+"ms. Owner extraction: "+tagExtr.getTime()+"ms");

  
  
  
  storage.addDataChangeListener( new DataChangeListener() 
  {
   @Override
   public void dataChanged()
   {
    collectStats();
   }
  } );
  

  storage.addMaintenanceModeListener(new MaintenanceModeListener()
  {
   @Override
   public void exitMaintenanceMode()
   {
    maintenanceMode = false;
   }
   
   @Override
   public void enterMaintenanceMode()
   {
    maintenanceMode = true;
   }
  });

  assert ( startTime = System.currentTimeMillis() ) != 0;
  
  collectStats();

  assert log.info("Stats collecting time: "+(System.currentTimeMillis()-startTime)+"ms");

 }

 private void collectStats()
 {
  AgeAttributeClass pubsClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.PUBLICATIONS_ATTR_CLASS_NAME );
  AgeAttributeClass pubMedIdClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.PUBMEDID_ATTR_CLASS_NAME );
  AgeAttributeClass pubDOIClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.PUBDOI_ATTR_CLASS_NAME );
  
  Map<String,String> pubMedMap = new HashMap<String, String>();
  Map<String,String> doiMap = new HashMap<String, String>();
  
  statistics = new BioSDStat();
  
  List<? extends AgeObject> groupList = groupsIndex.getObjectList();
  
  statistics.setGroups( groupList.size() );
  
  int refGrp = 0;
  
  for( AgeObject grp : groupList )
  {
   Collection<? extends AgeAttribute> ref = grp.getAttributesByClass(referenceAttributeClass, false);

   boolean isRef = ref != null && ref.size() > 0 && ref.iterator().next().getValueAsBoolean();
   
   if( isRef )
    refGrp++;
   
   int samples = 0;
   for( AgeRelation rel : grp.getRelations() )
   {
    if( rel.getAgeElClass() == groupToSampleRelClass )
     samples++;
   }
   
//   Collection<? extends AgeRelation> smpRels = grp.getRelationsByClass(groupToSampleRelClass, true);
//   
//   
//   if( smpRels != null )
//    samples = smpRels.size();
   
   statistics.addSamples( samples );
   
   if( isRef )
    statistics.addRefSamples( samples );

   Object dsVal = grp.getAttributeValue(dataSourceAttributeClass);
   
   String ds = null;
   
   if( dsVal != null )
    ds = dsVal.toString();
   
   if( ds != null )
   {
    BioSDStat dsStat = statistics.getDataSourceStat( ds );
    dsStat.addGroups(1);
    dsStat.addSamples(samples);
    
    if( isRef )
     dsStat.addRefSamples( samples );
   }
   
   Collection<? extends AgeAttribute> pubs = grp.getAttributesByClass(pubsClass, false);
   
   if( pubs != null )
   {
    for( AgeAttribute pub : pubs )
    {
     AgeObject pubObj = ((AgeObjectAttribute)pub).getValue();
     
     Object val = pubObj.getAttributeValue(pubMedIdClass);
     
     String pmId = null;
     
     if( val == null )
      pmId = null;
     else
     {
      pmId = val.toString();
      
      if( pmId.length() == 0 )
       pmId=null;
     }
     
     val = pubObj.getAttributeValue(pubDOIClass);
     
     String doi = null;
     
     if( val == null )
      doi = null;
     else
     {
      doi = val.toString();
      
      if( doi.length() == 0 )
       doi=null;
     }
    
     if( pmId != null )
      pubMedMap.put(pmId, doi);

     if( doi != null && doiMap.get(doi) == null )
      doiMap.put(doi,pmId);
    }
   }
  }
  
  int doiCount = 0;
  for( Map.Entry<String, String> me : doiMap.entrySet() )
   if( me.getValue() == null )
    doiCount++;
  
  statistics.setRefGroups(refGrp);
  statistics.setPublications( pubMedMap.size() + doiCount );
 }
 
 @Override
 public Report selectSampleGroups(String query, boolean searchSmp, boolean searchGrp, boolean searchAttrNm,
   boolean searchAttrVl, boolean refOnly, int offset, int count) throws MaintenanceModeException
 {
  
  Highlighter highlighter = null;

  try
  {
   if( query != null )
   {
    highlighter = new Highlighter(htmlFormatter, new QueryScorer( queryParser.parse(query) ) );
    highlighter.setTextFragmenter(new NullFragmenter());
   }
  }
  catch(ParseException e)
  {
   Report rep = new Report();
   rep.setObjects(new ArrayList<GroupImprint>());
   rep.setTotalGroups(0);
   rep.setTotalSamples(0);
  
   return rep;
  }
  
  
  
  if( maintenanceMode )
   throw new MaintenanceModeException();
 
  String user = Configuration.getDefaultConfiguration().getSessionManager().getEffectiveUser();

  
  if( query == null )
   query = "";
  else
   query=query.trim();
  
//  if( query == null )
//   return getAllGroups(offset, count, refOnly);
  
//  if( query.length() == 0 )
//   return getAllGroups(offset, count, refOnly);
  
  StringBuilder sb = new StringBuilder();
  
  if( query.length() > 0  )
  {
   sb.append("( ");

   if(searchAttrNm)
   {
    if(searchGrp)
     sb.append(BioSDConfigManager.GROUP_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");

    if(searchSmp)
     sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");
   }

   if(searchAttrVl)
   {
    if(searchGrp)
     sb.append(BioSDConfigManager.GROUP_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");

    if(searchSmp)
     sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");
   }

   sb.setLength(sb.length() - 4);

   sb.append(" ) AND ");
  }
  

  if( ! BuiltInUsers.SUPERVISOR.getName().equals(user) )
  {
   UserCacheObject uco = getUserCacheobject(user);

   sb.append("(").append(BioSDConfigManager.SECTAGS_FIELD_NAME).append(":(").append(uco.getAllowTags()).append(") OR ")
   .append(BioSDConfigManager.OWNER_FIELD_NAME).append(":(").append(user).append("))").append(" AND ");

   if(uco.getDenyTags().length() > 0)
    sb.append("NOT ").append(BioSDConfigManager.SECTAGS_FIELD_NAME).append(":(").append(uco.getDenyTags()).append(") AND ");
  }
  
  int qLen = sb.length()-5;
 
  
  if( refOnly )
   sb.append(BioSDConfigManager.GROUP_REFERENCE_FIELD_NAME).append(":(true)").append(" AND ");

  sb.setLength(sb.length() - 5);
  
  String lucQuery = sb.toString(); 
  
  assert log.debug("Query: "+lucQuery);
  
  
  
  List<AgeObject> sel = groupsIndex.select( lucQuery );
  
  int nSmp = 0;
  
  for( AgeObject go : sel )
  {
   Collection<?> smps =  go.getRelationsByClass(groupToSampleRelClass, false);
   
   if( smps != null )
    nSmp+=smps.size();
  }
  
  List<GroupImprint> res = new ArrayList<GroupImprint>();
  
  int lim = offset+count;
  
  if( lim > sel.size() )
   lim=sel.size();
  
  for( int i=offset; i< lim; i++ )
  {
   GroupImprint gr = createGroupObject(sel.get(i), searchGrp? highlighter: null, searchAttrNm, searchAttrVl);
   
   if( searchSmp && query.length() > 0 )
   {
    sb.setLength(qLen);
   
    sb.append(" AND "+BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(gr.getId());
    gr.setMatchedCount( samplesIndex.count( sb.toString() ) );
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
  rep.setTotalGroups(sel.size());
  rep.setTotalSamples(nSmp);
  
  return rep;
 }
 
 private UserCacheObject getUserCacheobject(String user)
 {
  synchronized(userCache)
  {
   UserCacheObject uco = userCache.get(user);
   
   if( uco != null )
    return uco;
   
   uco = new UserCacheObject();
   uco.setUserName(user);

   StringBuilder sb = new StringBuilder(100);
   
   Collection<TagRef> tags = Configuration.getDefaultConfiguration().getPermissionManager().getAllowTags(SystemAction.READ, user);
   
   if( tags != null )
   {
    for( TagRef tr : tags )
     sb.append(tr.getClassiferName().length()).append(tr.getClassiferName()).append(tr.getTagName()).append(" ");
   }
   
   if( sb.length() > 0 )
    sb.setLength(sb.length()-1);
   else
    sb.append("XXX");
   
   uco.setAllowTags(sb.toString());
   
   sb.setLength(0);
   
   tags = Configuration.getDefaultConfiguration().getPermissionManager().getDenyTags(SystemAction.READ, user);

   if( tags != null )
   {
    for( TagRef tr : tags )
     sb.append(tr.getClassiferName().length()).append(tr.getClassiferName()).append(tr.getTagName()).append(" ");
   }
   
   if( sb.length() > 0 )
    sb.setLength(sb.length()-1);
   
   uco.setDenyTags(sb.toString());

   userCache.put(user, uco);
   
   return uco;
  }
 }

 private String highlight( Highlighter hlighter, Object str )
 {
  if( str == null )
   return null;
  
  String s = str.toString();

  if( hlighter == null )
   return s;
  
  try
  {
   String out = hlighter.getBestFragment(analizer, "", s);
   
   if( out == null )
    return s;
   
   return out;
  }
  catch(Exception e)
  {
  }
 
  return s;
 }
 
 private GroupImprint createGroupObject( AgeObject obj, Highlighter hlighter, boolean hlName, boolean hlValue )
 {
  GroupImprint sgRep = new GroupImprint();

  String strN = null;
  String strV = null;
  
  strV=obj.getId();
  
  sgRep.setId(strV );

//  if( hlValue )  
//   strV = highlight(hlighter, obj.getId());

//  sgRep.addAttribute("Submission ID", obj.getSubmission().getId(), true, 0);
  sgRep.addAttribute("__ID", strV, true, 0);
  
  Object descVal = obj.getAttributeValue(desciptionAttributeClass);
  
  if( descVal != null  )
  {
   strV = StringUtils.htmlEscaped( descVal.toString() );
   
   if( hlValue )  
    strV = highlight(hlighter, strV);
    
   sgRep.setDescription( strV );
  }
  
 
  for( AgeAttribute atr : obj.getAttributes() )
  {
   AgeAttributeClass atCls = atr.getAgeElClass();
   
   if( atCls.isClassOrSubclass( commentAttributeClass ) )
   {
    strN = StringUtils.htmlEscaped( atCls.getName() );
    
    if( hlName )  
     strN = highlight(hlighter, strN);

    strV = atr.getValue()!=null? StringUtils.htmlEscaped(atr.getValue().toString()):null;
    
    if( hlValue )  
     strV = highlight(hlighter, strV );
    
    sgRep.addOtherInfo( strN, strV );
   }
   else if( atCls.getDataType() == DataType.OBJECT )
   {
    strN = StringUtils.htmlEscaped( atCls.getName() );
    
    if( hlName )  
     strN = highlight(hlighter, strN);

    
    sgRep.attachObjects( strN, createAttributedObject( ((AgeObjectAttribute)atr).getValue(), hlighter, hlName, hlValue)  );
   } 
   else
   {
    strN = StringUtils.htmlEscaped( atCls.getName() );
    
    if( hlName )  
     strN = highlight(hlighter, strN);

    strV = atr.getValue()!=null? StringUtils.htmlEscaped(atr.getValue().toString()):null;
    
    if( hlValue )  
     strV = highlight(hlighter, strV );

     sgRep.addAttribute(strN, strV, atr.getAgeElClass().isCustom(),atr.getOrder());
   }
  }
  
  Collection<? extends AgeRelation> pubRels =  obj.getRelationsByClass(groupToPublicationRelClass, false);

  if( pubRels != null )
  {
   if( hlName )  
    strN = highlight(hlighter, "Publications");
   else
    strN = "Publications";

   for( AgeRelation pRel : pubRels )
    sgRep.attachObjects( strN, createAttributedObject(pRel.getTargetObject(), hlighter, hlName, hlValue ) );
  }

  Collection<? extends AgeRelation> persRels =  obj.getRelationsByClass(groupToContactRelClass, false);

  if( persRels != null )
  {
   if( hlName )  
    strN = highlight(hlighter, "Contacts");
   else
    strN = "Contacts";

   for( AgeRelation pRel : persRels )
    sgRep.attachObjects( "Contacts", createAttributedObject(pRel.getTargetObject(), hlighter, hlName, hlValue ) );
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

 private AttributedImprint createAttributedObject(AgeObject ageObj, Highlighter hlighter, boolean hlName, boolean hlValue )
 {
  String strN;
  String strV;
  
  AttributedImprint obj = new AttributedImprint();
  
  if( ageObj.getAttributes() != null )
  {
   for( AgeAttribute attr : ageObj.getAttributes() )
   {
    strN = StringUtils.htmlEscaped( attr.getAgeElClass().getName() );
    
    if( hlName )  
     strN = highlight(hlighter, strN);

    strV = attr.getValue()!=null? StringUtils.htmlEscaped(attr.getValue().toString()):null;
    
    if( hlValue )  
     strV = highlight( hlighter, strV );

    obj.addAttribute(strN, strV, attr.getAgeElClass().isCustom(), attr.getOrder());
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
 
 class TagsExtractor implements TextValueExtractor
 {
  private StringBuilder sb = new StringBuilder();
  private PermissionManager permMngr = Configuration.getDefaultConfiguration().getPermissionManager();

  private long execTime=0;
  
  @Override
  public String getValue(AgeObject ao)
  {
   long startTime=0;
   
   assert (startTime=System.currentTimeMillis()) > 0 || true ;
   
   Collection<TagRef> tags = permMngr.getEffectiveTags( ao );
   
   if( tags == null )
    return "";
   
   sb.setLength(0);
   
   for( TagRef tr : tags )
    sb.append(tr.getClassiferName().length()).append(tr.getClassiferName()).append(tr.getTagName()).append(" ");
    
   String val = sb.toString(); 
   
   assert ( execTime += (System.currentTimeMillis()-startTime)  ) > 0 || true ;
     
   return val;
  }
  
  public long getTime()
  {
   return execTime;
  }
 }
 
 class OwnerExtractor implements TextValueExtractor
 {
  private long execTime=0;

  private AnnotationManager annorMngr = Configuration.getDefaultConfiguration().getAnnotationManager();

  @Override
  public String getValue(AgeObject ao)
  {
   long startTime=0;
   
   assert (startTime=System.currentTimeMillis()) > 0 || true ;

   Entity entId = ao;
   
   String own = null;
   
   while( entId != null )
   {
    try
    {
     own = (String)annorMngr.getAnnotation(Topic.OWNER, entId, true);
    }
    catch(AnnotationDBException e)
    {
     e.printStackTrace();
     return "";
    }
    
    if( own != null )
     break;
    
    entId = entId.getParentEntity();
   }
   
   if( own == null )
   {
    own = Configuration.getDefaultConfiguration().getSessionManager().getEffectiveUser();
    
    if( own == null )
     own = "";
   }
   assert ( execTime += (System.currentTimeMillis()-startTime)  ) > 0 || true ;

   return own;
  }
  
  public long getTime()
  {
   return execTime;
  }

 }
 
 class AttrValuesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
  Set<String> tokSet = new HashSet<String>();
 
  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);
   
   for( AgeAttribute attr : gobj.getAttributes() )
   {
    Object val = attr.getValue();
    
    if( attr.getAgeElClass().getDataType().isTextual() )
     tokSet.add( val.toString() );
    
    if( attr.getAttributes() != null )
    {
     for( AgeAttribute qual : attr.getAttributes() )
     {
      Object qval = qual.getValue();
      
      if( qual.getAgeElClass().getDataType().isTextual() )
       tokSet.add( qval.toString() );
     }
    }

   }
    
   for( String tk : tokSet )
    sb.append( tk ).append(' ');
    
   tokSet.clear();  
     
   return sb.toString();
  }
 }
 
 class SampleAttrValuesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
  Set<String> tokSet = new HashSet<String>();
 
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
      if( ! attr.getAgeElClass().getDataType().isTextual() )
       continue;
      
      tokSet.add( attr.getValue().toString() ); 
       

      if(attr.getAttributes() != null)
      {
       for(AgeAttribute qual : attr.getAttributes() )
       {
        if( ! qual.getAgeElClass().getDataType().isTextual() )
         continue;

        tokSet.add( qual.getValue().toString() );
       }
      }
     }
    }
   }
   
   for( String tk : tokSet )
    sb.append( tk ).append(' ');

   tokSet.clear();
   return sb.toString();
  }
 }
 
 class RefGroupExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();

  public String getValue(AgeObject gobj)
  {
   for( AgeAttribute attr : gobj.getAttributes() )
   {
    if( attr.getAgeElClass() == referenceAttributeClass && attr.getValueAsBoolean() )
     return "true";
   }
   
   return "";
  }
 }
 
 class SampleAttrNamesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
  Set<String> tokSet = new HashSet<String>();

  public String getValue(AgeObject gobj)
  {

   Collection< ? extends AgeRelation> rels = gobj.getRelations();

   if(rels == null)
    return "";

   sb.setLength(0);
   
   for(AgeRelation rel : rels)
   {
    if(rel.getAgeElClass() == groupToSampleRelClass)
    {
     AgeObject obj = rel.getTargetObject();

     for(AgeAttribute attr : obj.getAttributes())
     {
      tokSet.add(attr.getAgeElClass().getName());

      if(attr.getAttributes() != null)
      {
       for(AgeAttribute qual : attr.getAttributes())
        tokSet.add(qual.getAgeElClass().getName());
      }
     }
    }
   }
   
   for( String tk : tokSet )
    sb.append( tk ).append(' ');

   tokSet.clear();

   return sb.toString();
  }
 }

 class AttrNamesExtractor implements TextValueExtractor
 {
  StringBuilder sb = new StringBuilder();
  Set<String> tokSet = new HashSet<String>();

  public String getValue(AgeObject gobj)
  {
   sb.setLength(0);
   
//   if( gobj.getId().equals("SAME316677") )
//    System.out.println("Hello");
   
   for( AgeAttribute attr : gobj.getAttributes() )
   {
    tokSet.add(attr.getAgeElClass().getName());
//    sb.append( attr.getAgeElClass().getName() ).append(' ');
    
    if( attr.getAttributes() != null )
    {
     for( AgeAttribute qual : attr.getAttributes() )
      tokSet.add(qual.getAgeElClass().getName());
    }
   }
    
   for( String tk : tokSet )
    sb.append( tk ).append(' ');

   tokSet.clear();

   
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

 
 private SampleList createSampleReport(List<AgeObject> samples, Highlighter hlite, boolean hlNm, boolean hlVl )
 {
  SampleList sl = new SampleList();
  
  class LinkCount
  {
   ClassImprint imprint;
   int counter;
  }
  
  Map<ClassImprint, LinkCount > clsMap = new HashMap<ClassImprint, LinkCount>();
  
  
  
  ImprintBuilder iBld = new ImprintBuilder( htmlEscProc, htmlEscProc, null, null);
  
  for( AgeObject smpl : samples )
  {
   ObjectImprint imp = iBld.convert(smpl,sampleConvHint);
   
   sl.addSample( imp );
   
//   sl.addSample( convertAttributed(smpl, hlite, hlNm, hlVl) );

   int ord=0;
   
   for( AttributeImprint attr : imp.getAttributes() )
   {
    ord++;
    
    LinkCount cCnt = clsMap.get(attr.getClassImprint());
    
    if( cCnt == null )
    {
     cCnt = new LinkCount();
     cCnt.counter=5000-ord;
     cCnt.imprint=attr.getClassImprint();
     clsMap.put(cCnt.imprint, cCnt);
    }
    else
     cCnt.counter+=5000-ord;
    
//    AttributeClassReport atCls = new AttributeClassReport();
//     
//    atCls.setCustom( ageAtCls.isCustom() );
//     
//    String cName = ageAtCls.getName();
//     
//    if( hlNm )
//     cName = highlight(hlite, cName);
//     
//    atCls.setName( cName );
////     atCls.setId("AttrClass"+(id++));
//    atCls.setId((ageAtCls.isCustom()?"CC:":"DC:")+ageAtCls.getName()+"$"+cCnt.value);
//     
//    atCls.addValue(attrval);
    
   }
   
  }
  
  List<LinkCount> clsLst = new ArrayList<LinkCount>( clsMap.size() );
  clsLst.addAll(clsMap.values());
  
  Collections.sort(clsLst, new Comparator<LinkCount>()
  {
   @Override
   public int compare(LinkCount o1, LinkCount o2)
   {
    if( BioSDConfigManager.SAMPLE_ACCS_ATTR_CLASS_NAME.equals( o1.imprint.getName() ) )
    {
     if( BioSDConfigManager.SAMPLE_ACCS_ATTR_CLASS_NAME.equals( o2.imprint.getName() ) )
      return 0;
     else
      return -1;
    }
    else if( BioSDConfigManager.SAMPLE_ACCS_ATTR_CLASS_NAME.equals( o2.imprint.getName() ) )
     return 1;
    
    return o2.counter-o1.counter;
   }
  });
  
  for( LinkCount lc : clsLst )
  {
   sl.addHeader( lc.imprint );
  
   if( hlNm )
    lc.imprint.setName( highlight(hlite, lc.imprint.getName()) );
  }
  
  if( hlVl && hlite != null)
  {
   for( ObjectImprint obj : sl.getSamples() )
    highlightAttributedImprint(hlite, obj);
  }
  
  return sl;
 }
 
 private void highlightAttributedImprint( Highlighter hlite, uk.ac.ebi.age.ui.shared.imprint.AttributedImprint obj )
 {
  if( obj.getAttributes() == null )
   return;
  
  for( AttributeImprint atImp : obj.getAttributes() )
  {
   if( atImp.getClassImprint().getType() == uk.ac.ebi.age.ui.shared.imprint.ClassType.ATTR_STRING)
   {
    for( Value v : atImp.getValues() )
    {
     ((uk.ac.ebi.age.ui.shared.imprint.StringValue)v).setValue( highlight(hlite, v.getStringValue() ) );
     
     highlightAttributedImprint( hlite, v );
    }
   }
   else if( atImp.getClassImprint().getType() == uk.ac.ebi.age.ui.shared.imprint.ClassType.ATTR_OBJECT )
   {
    for( Value v : atImp.getValues() )
    {
     if( ((ObjectValue)v).getObjectImprint() != null )
      highlightAttributedImprint( hlite, ((ObjectValue)v).getObjectImprint() );
     
     highlightAttributedImprint( hlite, v );
    }
   }
  }
 }

 private AttributedObject convertAttributed( Attributed obj, Highlighter hlite, boolean hlNm, boolean hlVl  )
 {
  AttributedObject objAttr = new AttributedObject();

  String strV = null;
  
  if( obj instanceof AgeObject )
  {
   objAttr.setName( ((AgeObject) obj).getId() );
   
   if( obj.getAttributes() != null )
   {
    List<AttributedObject> attrs = new ArrayList<AttributedObject>();
    
    for( Attributed oa : obj.getAttributes() )
     attrs.add(convertAttributed(oa, hlite, hlNm, hlVl ));
    
    objAttr.setAttributes(attrs);
   }
  }
  else if( obj instanceof AgeAttribute )
  {
   AgeAttribute ageAt = (AgeAttribute)obj;
   
   objAttr.setName((ageAt.getAgeElClass().isCustom()?"CC:":"DC:")+ageAt.getAgeElClass().getName());
   
   Object val = ageAt.getValue();
   
   if( val instanceof Attributed )
    objAttr.setObjectValue( convertAttributed( (Attributed)val, hlite, hlNm, hlVl ) );
   else
   {
    strV = val.toString();
    
    if( hlVl )  
     strV = highlight(hlite, val);
    else
     strV = val!=null?val.toString():null;

    
    objAttr.setStringValue(strV);
   }
   
   if( ageAt.getAttributes() != null )
   {
    List<AttributedObject> attrs = new ArrayList<AttributedObject>();
    
    for( Attributed oa : ageAt.getAttributes() )
     attrs.add(convertAttributed(oa, hlite, hlNm, hlVl));
    
    objAttr.setAttributes(attrs);
   }

  }
  
  return objAttr;
 }
 
// private void collectQualifiers(AgeAttribute ageAttr, AttributedObject biosdAttr)
// {
//  List<AttributedObject> bioQl = new ArrayList<AttributedObject>( 3 );
//  
//  for( AgeAttribute q : ageAttr.getAttributes() )
//  {
//   AttributedObject bq = new AttributedObject( q.getAgeElClass().getName(), q.getValue().toString() );
//   
//   if( q.getAttributes() != null )
//    collectQualifiers(q,bq);
//   
//   bioQl.add(bq);
//  }
//  
//  biosdAttr.setQualifiers(bioQl);
// }
 
 
 @Override
 public SampleList getSamplesByGroup(String grpID, String query, boolean searchAtNames, boolean searchAtValues, int offset, int count) throws MaintenanceModeException
 {
  if( query != null && query.trim().length() == 0 )
   query = null;
  
  Highlighter highlighter = null;

  try
  {
   if( query != null )
   {
    highlighter = new Highlighter(htmlFormatter, new QueryScorer( queryParser.parse(query) ) );
    highlighter.setTextFragmenter(new NullFragmenter());
   }
  }
  catch(ParseException e)
  {
   SampleList lst = createSampleReport( new ArrayList<AgeObject>(1), null, false, false );
   lst.setTotalRecords(0);
 
   return lst;
  }

  if( maintenanceMode )
   throw new MaintenanceModeException();

  
  AgeObject grpObj = storage.getGlobalObject(grpID);
  
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
  
  SampleList sl = createSampleReport(res, highlighter, searchAtNames, searchAtValues);
  sl.setTotalRecords(total);
  
  return sl;
 }
 
 public Report getSamplesByGroupX(String grpID, int offset, int count)
 {
  AgeObject grpObj = storage.getGlobalObject(grpID);
  
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
  rep.setTotalGroups(total);
  
  return rep;
 }

 
 @Override
 public SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count) throws MaintenanceModeException
 {
  Highlighter highlighter = null;

  try
  {
   if( query != null )
   {
    highlighter = new Highlighter(htmlFormatter, new QueryScorer( queryParser.parse(query) ) );
    highlighter.setTextFragmenter(new NullFragmenter());
   }
  }
  catch(ParseException e)
  {
   SampleList lst = createSampleReport( new ArrayList<AgeObject>(1), null, false, false );
   lst.setTotalRecords(0);
 
   return lst;
  }
  

  
  if( maintenanceMode )
   throw new MaintenanceModeException();

  
  StringBuilder sb = new StringBuilder();
  
  sb.append("( ");
  
  if( searchAttrNm )
   sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');

  if( searchAttrVl )
  {
   if( searchAttrNm )
    sb.append(" OR ");
   
   sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
  }
  
  sb.append(" ) AND ").append(BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
  
  List<AgeObject> sel = samplesIndex.select( sb.toString() );
  
 
  if( offset > sel.size())
   return null;
  
  int end = (offset+count) > sel.size()? sel.size() : (offset+count); 
  
  SampleList lst = createSampleReport( sel.subList(offset, end), highlighter, searchAttrNm, searchAttrVl );
  lst.setTotalRecords(sel.size());
  
  return lst;
 }
 
 public Report getSamplesByGroupAndQueryX(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
 {
  StringBuilder sb = new StringBuilder();
  
  sb.append("( ");
  
  if( searchAttrNm )
   sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');

  if( searchAttrVl )
  {
   if( sb.length() > 0 )
    sb.append(" OR ");
   
   sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
  }
  
  sb.append(" ) AND ").append(BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
  
  List<AgeObject> sel = samplesIndex.select( sb.toString() );
  
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
  rep.setTotalGroups(total);
  
  return rep;
 }

 @Override
 public void exportData( PrintWriter out )
 {
  
  out.println("<Biosamples>");
  
  for( AgeObject ao : groupsIndex.getObjectList() )
  {
   String grpId = StringUtils.htmlEscaped(ao.getId());
   
   out.print("<SampleGroup id=\"");
   out.print(grpId);
   out.println("\">");
   
   exportAttributed( ao, out );
   
   for( AgeRelation rel : ao.getRelations() )
   {
    if( rel.getAgeElClass() == groupToSampleRelClass )
     exportSample( rel.getTargetObject(), grpId, out );
   }
   
   out.println("</SampleGroup>");
  }
  
  out.println("</Biosamples>");

 }
 
 private void exportAttributed( Attributed ao,PrintWriter out )
 {
  for( AgeAttributeClass aac : ao.getAttributeClasses() )
  {
   out.print("<attribute class=\"");
   out.print(StringUtils.htmlEscaped(aac.getName()));
   out.println("\" classDefined=\""+(aac.isCustom()?"false":"true")+"\" dataType=\""+aac.getDataType().name()+"\">");

   for( AgeAttribute attr : ao.getAttributesByClass(aac, false) )
   {
    out.println("<value>");
    
    exportAttributed( attr, out );

    if( aac.getDataType() != DataType.OBJECT )
     out.print(StringUtils.htmlEscaped(attr.getValue().toString()));
    else
    {
     AgeObject tgtObj = (AgeObject)attr.getValue();
     
     out.print("<object id=\""+StringUtils.htmlEscaped(tgtObj.getId())+"\" class=\"");
     out.print(StringUtils.htmlEscaped(tgtObj.getAgeElClass().getName()));
     out.println("\" classDefined=\""+(tgtObj.getAgeElClass().isCustom()?"false":"true")+"\">");
 
     exportAttributed( tgtObj, out );
    
     out.println("</object>");
    }

    out.println("</value>");
   }

   out.println("</attribute>");
  }
 }
 
 private void exportSample( Attributed ao, String grpId, PrintWriter out )
 {
  out.print("<Sample id=\"");
  out.print(StringUtils.htmlEscaped(ao.getId()));
  out.println("\" groupId=\"" +grpId +"\">");

  exportAttributed( ao, out );

  
  out.println("</Sample>");
 }

 
// @Override
// public Report getAllGroups(int offset, int count, boolean refOnly )
// {
//  
//  int lim = offset+count;
//  
//  List<? extends AgeObject> groupList = groupsIndex.getObjectList();
//  
//  int last = refOnly?getStatistics().getRefGroups():groupList.size();
//  
//  if( lim > last )
//   lim=last;
//  
//  List<GroupImprint> res = new ArrayList<GroupImprint>(count);
//  
//  String user = Configuration.getDefaultConfiguration().getSessionManager().getEffectiveUser();
//
//  
//  if( ! BuiltInUsers.SUPERVISOR.getName().equals(user) )
//  {
//   UserCacheObject uco = getUserCacheobject(user);
//
//   sb.append(" AND (").append(BioSDConfigManager.SECTAGS_FIELD_NAME).append(":(").append(uco.getAllowTags()).append(") OR ")
//   .append(BioSDConfigManager.OWNER_FIELD_NAME).append(":(").append(user).append("))");
//
//   if(uco.getDenyTags().length() > 0)
//    sb.append(" NOT ").append(BioSDConfigManager.SECTAGS_FIELD_NAME).append(":(").append(uco.getDenyTags()).append(")");
//  }
//  else
//  {
//   for( ; offset < lim; offset++)
//    res.add( createGroupObject(groupList.get(offset)) );
//  }
//
//  
//  
//  Report rep = new Report();
//  rep.setObjects(res);
//  rep.setTotalGroups(refOnly?getStatistics().getRefGroups():getStatistics().getGroups());
//  rep.setTotalSamples(refOnly?getStatistics().getRefSamples():getStatistics().getSamples());
//  
//  return rep;
// }

 @Override
 public BioSDStat getStatistics()
 {
  return statistics;
 }

 @Override
 public void securityChanged()
 {
  synchronized(userCache)
  {
   userCache.clear();
  }
 }

 @Override
 public AgeObject getSample(String sampleId) throws MaintenanceModeException
 {
  if( maintenanceMode )
   throw new MaintenanceModeException();

  
  AgeObject smpObj = storage.getGlobalObject(sampleId);
  
  if( smpObj == null || ! smpObj.getAgeElClass().isClassOrSubclass(sampleClass) )
   return null;
  
  return smpObj;
 }

 @Override
 public AgeObject getGroup(String groupId) throws MaintenanceModeException
 {
  if( maintenanceMode )
   throw new MaintenanceModeException();

  
  AgeObject smpObj = storage.getGlobalObject(groupId);
  
  if( smpObj == null || ! smpObj.getAgeElClass().isClassOrSubclass(groupClass) )
   return null;
  
  return smpObj;
 }

 private static class Int
 {
  int value;
 }
}
