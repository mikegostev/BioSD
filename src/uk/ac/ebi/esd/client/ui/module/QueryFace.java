package uk.ac.ebi.esd.client.ui.module;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryFace extends VLayout
{
 public QueryFace()
 {
  setWidth100();
  setHeight100();
  
  Canvas qp = new QueryPanel();
//  qp.setHeight(200);
  qp.setWidth("100%");
  qp.setBorder("1px solid red");

  Canvas rp =  new ResultPane();
  rp.setWidth100();
  rp.setHeight100();
  rp.setBorder("1px dotted green");
  
  addMember( qp );
  addMember( rp );
  
 }
}
