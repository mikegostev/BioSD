package uk.ac.ebi.esd.server.service;

import java.util.List;

import uk.ac.ebi.esd.client.query.ObjectReport;

public abstract class ESDService
{
 private static ESDService service;
 
 public static ESDService getInstance()
 {
  return service;
 }
 
 public static void setDefaultInstance( ESDService srv )
 {
  service=srv;
 }

 public abstract List<ObjectReport> selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl);

 public abstract void shutdown();

 public abstract List<ObjectReport> getSamplesByGroup(String grpID);
}
