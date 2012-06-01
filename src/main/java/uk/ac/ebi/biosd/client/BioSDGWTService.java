package uk.ac.ebi.biosd.client;

import uk.ac.ebi.biosd.client.query.Report;
import uk.ac.ebi.biosd.client.query.SampleList;
import uk.ac.ebi.biosd.client.shared.MaintenanceModeException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ageQueryGWT")
public interface BioSDGWTService extends RemoteService
{
 public static class Util
 {
  private static BioSDGWTServiceAsync instance;
  
  public static BioSDGWTServiceAsync getInstance()
  {
   if( instance != null )
    return instance;
   
   
   instance = (BioSDGWTServiceAsync) GWT.create(BioSDGWTService.class);
   return instance;
  }
 }
 
 
 Report selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, boolean refOnly, int offs, int cnt)
   throws MaintenanceModeException;


 SampleList getSamplesByGroup(String grpID, String query, boolean searchAtNames, boolean searchAtValues, int offs, int count) throws MaintenanceModeException;


 SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offs, int count)
  throws MaintenanceModeException;



}
