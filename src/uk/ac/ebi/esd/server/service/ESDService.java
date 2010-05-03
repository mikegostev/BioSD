package uk.ac.ebi.esd.server.service;

import java.util.List;

import uk.ac.ebi.esd.client.query.SampleGroupReport;

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

 public abstract List<SampleGroupReport> selectSampleGroups(String value, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl);

 public abstract void shutdown();
}
