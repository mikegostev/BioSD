package uk.ac.ebi.esd.server.service;

import uk.ac.ebi.esd.client.query.Report;

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

 public abstract Report selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, int offset, int count);
 public abstract Report getAllGroups(int offset, int count);

 public abstract void shutdown();

 public abstract Report getSamplesByGroup(String grpID, int offset, int count);

 public abstract Report getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offset, int count);
}
