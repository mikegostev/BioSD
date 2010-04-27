package uk.ac.ebi.esd.client;

import uk.ac.ebi.esd.client.ui.module.QueryFace;

import com.google.gwt.core.client.EntryPoint;

public class ESDEntryPoint implements EntryPoint
{

 public void onModuleLoad()
 {
  new QueryFace().draw();

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