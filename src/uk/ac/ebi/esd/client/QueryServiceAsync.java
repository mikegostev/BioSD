package uk.ac.ebi.esd.client;

import java.util.List;

import uk.ac.ebi.esd.client.query.SampleGroupReport;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface QueryServiceAsync
{
 void selectSampleGroups(String value, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl, AsyncCallback<List<SampleGroupReport>> acb);
}
