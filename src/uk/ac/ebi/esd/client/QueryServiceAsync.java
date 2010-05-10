package uk.ac.ebi.esd.client;

import java.util.List;

import uk.ac.ebi.esd.client.query.ObjectReport;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface QueryServiceAsync
{
 void selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, AsyncCallback<List<ObjectReport>> acb);

 void getSamplesByGroup(String grpID, AsyncCallback<List<ObjectReport>> asyncCallback);

 void getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, AsyncCallback<List<ObjectReport>> asyncCallback);
}
