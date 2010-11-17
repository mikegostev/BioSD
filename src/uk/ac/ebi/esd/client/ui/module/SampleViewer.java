package uk.ac.ebi.esd.client.ui.module;

import java.util.List;

import uk.ac.ebi.esd.client.shared.AttributeClassReport;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.viewer.DetailViewer;

public class SampleViewer  extends Window
{
 public SampleViewer(List<AttributeClassReport> header, ListGridRecord rec )
 {
  int height = header.size()*30;
  
  if( height > 600 )
   height = 600;
  
//  setAutoSize(true);  
  setTitle("Sample "+rec.getAttributeAsString("__id") );  
  setWidth(750);  
  setHeight(height); 
//  setLeft(offsetLeft);  
  setCanDragReposition(true);  
  setCanDragResize(true);  
  setShowMinimizeButton(false);
  setIsModal(true);
  setShowModalMask(true);
  centerInPage();
  
  DataSource ds = new DataSource();
  ds.setClientOnly(true);

  for( AttributeClassReport cls : header )
  {
   ds.addField(new DataSourceTextField(cls.getId(), cls.isCustom()?cls.getName():("<b>"+cls.getName()+"</b>") ));
  }
  
  ds.addData(rec);
  
  DetailViewer dv = new DetailViewer();
  dv.setWidth("100%");
  dv.setHeight("100%");
  dv.setCanSelectText(true);
  dv.setDataSource(ds);
  dv.setStyleName("groupDetails");
  dv.setAutoFetchData(true);

  addItem(dv);
 }

}
