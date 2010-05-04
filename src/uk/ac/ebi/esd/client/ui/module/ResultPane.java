package uk.ac.ebi.esd.client.ui.module;

import java.util.List;

import uk.ac.ebi.esd.client.query.SampleGroupReport;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.VLayout;

public class ResultPane extends VLayout
{
 public ResultPane()
 {
  setAlign(Alignment.CENTER);
  
  Label lb = new Label("Query result is empty");
  
  lb.setAlign(Alignment.CENTER);
  
  addMember(lb);
 }
 
 public void showResult( List<SampleGroupReport> res )
 {
  
 }

}
