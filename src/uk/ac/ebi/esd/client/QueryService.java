package uk.ac.ebi.esd.client;

import java.util.List;

import uk.ac.ebi.esd.client.query.ObjectReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ageQueryGWT")
public interface QueryService extends RemoteService
{
 public static class Util
 {
  private static QueryServiceAsync instance;
  
  public static QueryServiceAsync getInstance()
  {
   if( instance != null )
    return instance;
   
   
   instance = (QueryServiceAsync) GWT.create(QueryService.class);
   return instance;
  }
 }
 
 
 List<ObjectReport> selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl);


 List<ObjectReport> getSamplesByGroup(String grpID);

}
