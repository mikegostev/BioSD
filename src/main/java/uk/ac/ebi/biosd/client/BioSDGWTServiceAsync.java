package uk.ac.ebi.biosd.client;

import uk.ac.ebi.age.ui.shared.imprint.ObjectId;
import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;
import uk.ac.ebi.biosd.client.query.Report;
import uk.ac.ebi.biosd.client.query.SampleList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BioSDGWTServiceAsync
{
 void selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, boolean refOnly, int offs, int cnt, AsyncCallback<Report> acb);

// void getSamplesByGroup(String grpID, String query, boolean searchAtNames, boolean searchAtValues, int offs, int count, AsyncCallback<SampleList> asyncCallback);

 void getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offs, int count, AsyncCallback<SampleList> asyncCallback);
 
 void getObjectImprint( ObjectId id, AsyncCallback<ObjectImprint> cb );

// void getAllGroups(int offs, int count, boolean refOnly, AsyncCallback<Report> asyncCallback);
}
