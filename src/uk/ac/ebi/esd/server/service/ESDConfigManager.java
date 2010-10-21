package uk.ac.ebi.esd.server.service;


public class ESDConfigManager
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

 public String getServicesPath()
 {
  return getBasePath()+"/services";
 }
}
