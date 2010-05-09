package uk.ac.ebi.esd.client.ui.module;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryFace extends VLayout
{
 private ResultPane resultPane;
 
 public QueryFace()
 {
  setWidth100();
  setHeight100();
  
  resultPane =  new ResultPane();
  
  Canvas qp = new QueryPanel( resultPane );
  
//  qp.setHeight(200);
  qp.setWidth("100%");

  resultPane.setWidth100();
  resultPane.setHeight100();
  
  addMember( qp );
  addMember( resultPane );
  
 }
}
