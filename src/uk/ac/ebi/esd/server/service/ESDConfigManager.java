package uk.ac.ebi.esd.server.service;


public class ESDConfigManager
{
 public static final String SAMPLE_CLASS_NAME = "sample";
 public static final String SAMPLEGROUP_CLASS_NAME = "group";
 
 public static final String NAME_FIELD_NAME = "name";
 public static final String VALUE_FIELD_NAME = "value";
 public static final String SAMPLEINGROUP_REL_CLASS_NAME = "belongsTo";
 
 public static final String DESCRIPTION_ATTR_CLASS_NAME = "description";
 
 private static ESDConfigManager instance = new ESDConfigManager();
 
 public static ESDConfigManager instance()
 {
  return instance;
 }

 public String getBasePath()
 {
  return "var/esd";
 }

 
 public String getDBPath()
 {
  return getBasePath()+"/agedb";
 }

 public String getTmpPath()
 {
  return getBasePath()+"/tmp";
 }

}
