package uk.ac.ebi.biosd.server.service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;


public class BioSDConfigManager
{
 public static final String SAMPLE_CLASS_NAME = "Sample";
 public static final String SAMPLEGROUP_CLASS_NAME = "Group";
 
 public static final String SAMPLE_NAME_FIELD_NAME = "sname";
 public static final String SAMPLE_VALUE_FIELD_NAME = "svalue";
 public static final String GROUP_NAME_FIELD_NAME = "gname";
 public static final String GROUP_VALUE_FIELD_NAME = "gvalue";
 public static final String GROUP_ID_FIELD_NAME = "gid";

 public static final String SAMPLEINGROUP_REL_CLASS_NAME = "belongsTo";
 
 public static final String DESCRIPTION_ATTR_CLASS_NAME = "Description";
 public static final String COMMENT_ATTR_CLASS_NAME = "Comment";
 public static final String HAS_PUBLICATION_REL_CLASS_NAME = "hasPublication";
 public static final String CONTACT_OF_REL_CLASS_NAME = "contactOf";
 public static final String DATASOURCE_ATTR_CLASS_NAME = "Data Source";

 public static final String BASE_PATH_PARAM="basePath";
 public static final String DB_PATH_PARAM="dbPath";
 public static final String TMP_PATH_PARAM="tmpPath";
 public static final String SERVICES_PATH_PARAM="servicesPath";
 
 @SuppressWarnings("serial")
 private Map<String,String> configMap = new HashMap<String,String>(){{
  put(BASE_PATH_PARAM,      "var/biosd/");
  put(DB_PATH_PARAM,        "agedb");
  put(TMP_PATH_PARAM,       "tmp");
  put(SERVICES_PATH_PARAM,  "services");
 }};
 
 private static BioSDConfigManager instance = null;
 
 public BioSDConfigManager(ServletContext servletContext)
 {
  Enumeration<?> pNames = servletContext.getInitParameterNames();
  
  while( pNames.hasMoreElements() )
  {
   String key = pNames.nextElement().toString();
   configMap.put(key, servletContext.getInitParameter(key) );
  }
 }
 
 public static void setInstance( BioSDConfigManager inst )
 {
  instance=inst;
 }

 
 public static BioSDConfigManager instance()
 {
  return instance;
 }

 public String getBasePath()
 {
  return getConfigParameter(BASE_PATH_PARAM);
 }

 
 public String getDBPath()
 {
  return getPathParam(DB_PATH_PARAM);
 }

 public String getTmpPath()
 {
  return getPathParam(TMP_PATH_PARAM);
 }

 public String getServicesPath()
 {
  return getPathParam(SERVICES_PATH_PARAM);
 }

 public String getPathParam( String key )
 {
  String basePath = getBasePath();
  String path = getConfigParameter(key);
  
  if( path == null )
   path=key;
  
  if( basePath.endsWith("/") || path.startsWith("/") )
   return basePath+path;

  return basePath+"/"+path;
 }

 
 public String getConfigParameter( String key )
 {
  return configMap.get(key);
 }

 public String setConfigParameter( String key, String value )
 {
  String old = configMap.get(key);
  configMap.put(key, value);
  return old;
 }


}
