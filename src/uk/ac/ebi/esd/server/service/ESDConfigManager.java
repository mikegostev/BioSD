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
 public static final String COMMENT_ATTR_CLASS_NAME = "Comment";
 public static final String HAS_PUBLICATION_REL_CLASS_NAME = "hasPublication";
 public static final String CONTACT_OF_REL_CLASS_NAME = "contactOf";
 public static final String DATASOURCE_ATTR_CLASS_NAME = "Data Source";

 private static ESDConfigManager instance = new ESDConfigManager();
 
 public static ESDConfigManager instance()
 {
  return instance;
 }

 public String getBasePath()
 {
  return "var/biosd";
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
