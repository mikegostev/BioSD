package uk.ac.ebi.biosd.server.service;

import java.io.PrintWriter;
import java.util.Set;

import uk.ac.ebi.age.ext.user.exception.NotAuthorizedException;
import uk.ac.ebi.age.model.AgeAttributeClass;
import uk.ac.ebi.age.model.AgeObject;
import uk.ac.ebi.age.model.Attributed;
import uk.ac.ebi.age.ui.shared.imprint.ObjectId;
import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;
import uk.ac.ebi.biosd.client.query.Report;
import uk.ac.ebi.biosd.client.query.SampleList;
import uk.ac.ebi.biosd.client.shared.MaintenanceModeException;
import uk.ac.ebi.biosd.server.stat.BioSDStat;

public abstract class BioSDService
{
 private static BioSDService service;
 
 public static BioSDService getInstance()
 {
  return service;
 }
 
 public static void setDefaultInstance( BioSDService srv )
 {
  service=srv;
 }

 public abstract void shutdown();

 
 public abstract Report selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, boolean refOnly, int offset, int count)
   throws MaintenanceModeException;

// public abstract SampleList getSamplesByGroup(String grpID, String query, boolean searchAtNames, boolean searchAtValues, int offset, int count)
//   throws MaintenanceModeException;

 public abstract SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offset, int count)
   throws MaintenanceModeException;


 public abstract BioSDStat getStatistics();

 public abstract void exportData(PrintWriter out, String[] grpLst);

 public abstract AgeObject getSample(String sampleId) throws MaintenanceModeException;

 public abstract AgeObject getGroup(String groupId) throws MaintenanceModeException;

 public abstract void exportSample(Attributed ao, String grpId, PrintWriter out, Set<AgeAttributeClass> atset);

 public abstract ObjectImprint getObjectImprint(ObjectId id) throws MaintenanceModeException, NotAuthorizedException;

 public abstract void exportGroup(Attributed ao, PrintWriter out);

 public abstract void exportData(PrintWriter out, long since);

}
