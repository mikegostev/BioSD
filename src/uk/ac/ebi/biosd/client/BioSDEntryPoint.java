package uk.ac.ebi.biosd.client;

import uk.ac.ebi.biosd.client.ui.module.QueryFace;

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.widgets.layout.VLayout;

public class BioSDEntryPoint implements EntryPoint
{

 public void onModuleLoad()
 {
  VLayout c =  new QueryFace();

//  RootPanel.get("draw_syuda").add(c);
  c.draw();
//
//  DataManager.getInstance().init( new AsyncCallback<Void>()
//  {
//   
//   public void onSuccess(Void arg0)
//   {
//    new QueryFace().draw();
//   }
//   
//   public void onFailure(Throwable arg0)
//   {
//    // TODO Auto-generated method stub
//    
//   }
//  });
  
 }

}