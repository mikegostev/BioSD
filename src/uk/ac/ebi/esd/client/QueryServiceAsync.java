package uk.ac.ebi.esd.client;

import uk.ac.ebi.esd.client.query.Report;
import uk.ac.ebi.esd.client.query.SampleList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface QueryServiceAsync
{
 void selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, int offs, int cnt, AsyncCallback<Report> acb);

 void getSamplesByGroup(String grpID, int offs, int count, AsyncCallback<SampleList> asyncCallback);

 void getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offs, int count, AsyncCallback<SampleList> asyncCallback);

 void getAllGroups(int offs, int count, AsyncCallback<Report> asyncCallback);
}
