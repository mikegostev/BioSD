package uk.ac.ebi.biosd.client.ui.test;

import uk.ac.ebi.biosd.client.query.AttributedObject;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class ObjectViewPanel extends ListGrid
{
 public ObjectViewPanel( AttributedObject obj )
 {
  DataSource ds = new DataSource();
  ds.setClientOnly(true);

  ds.addField(new DataSourceTextField("Name", "Name"));
  ds.addField(new DataSourceTextField("Value", "Value"));

  setWidth("100%");
  setHeight("100%");
  setCanSelectText(true);
  setDataSource(ds);
  setStyleName("objectView");
  setAutoFetchData(true);
  
  setCanExpandRecords(true);
  setCanExpandRecordProperty("hasDetails");
  
  ListGridField nameField = new ListGridField("Name","Name");  
  ListGridField valField = new ListGridField("Value","Value");

  setFields(nameField,valField);
//  setExpansionMode(ExpansionMode.DETAIL_RELATED);

//  ds.addField(new DataSourceBooleanField("hasDetails", "hasDetails"));
//  ds.addField(new DataSource("hasDetails", "hasDetails"));

  
  for( AttributedObject at : obj.getAttributes() )
  {
   ListGridRecord rec = new ListGridRecord();
   
   rec.setAttribute( "Name", at.getName() );
   rec.setAttribute( "Value", at.getStringValue() );
   rec.setAttribute( "__attr", at );
   
   rec.setAttribute( "hasDetails", at.getAttributes() != null || ! ( at.getStringValue() instanceof String ) );

   ds.addData(rec);
  }
 }
}
