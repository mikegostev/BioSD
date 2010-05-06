package uk.ac.ebi.esd.client.ui.module;

import java.util.List;

import uk.ac.ebi.esd.client.query.ObjectReport;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryFace extends VLayout
{
 private ResultPane resultPane;
 
 public QueryFace()
 {
  setWidth100();
  setHeight100();
  
  Canvas qp = new QueryPanel( new AsyncCallback<List<ObjectReport>>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    arg0.printStackTrace();
   }

   @Override
   public void onSuccess(List<ObjectReport> arg0)
   {
    resultPane.showResult(arg0);
   }});
  
//  qp.setHeight(200);
  qp.setWidth("100%");

  resultPane =  new ResultPane();
  resultPane.setWidth100();
  resultPane.setHeight100();
  
  addMember( qp );
  addMember( resultPane );
  
 }
}
