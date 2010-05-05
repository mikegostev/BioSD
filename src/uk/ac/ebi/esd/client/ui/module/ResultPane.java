package uk.ac.ebi.esd.client.ui.module;

import java.util.List;
import java.util.Map;

import uk.ac.ebi.esd.client.query.SampleGroupReport;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.ExpansionMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;

public class ResultPane extends ListGrid
{
 public ResultPane()
 {
  setHeight("100%");
  
  setShowAllRecords(true);  
  setWrapCells(true);
  setCellHeight(56);
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  ds.addField(new DataSourceTextField("id", "ID"));
  ds.addField(new DataSourceTextField("desc", "Description", 2000));
  ds.addField(new DataSourceIntegerField("prop", "Property", 111));
  
//  setDataSource(ds);
  
  setCanExpandRecords(true); 
  
  ListGridField idField = new ListGridField("id","ID", 100);  
  ListGridField descField = new ListGridField("desc","Description");
//  ListGridField propField = new ListGridField("prop","AdditionalProp");
  
//  propField.setHidden(true);
  
  idField.setWidth(100);
     
  setFields(idField, descField );
  setExpansionMode(ExpansionMode.DETAIL_FIELD);
  
  ListGridRecord rec = new ListGridRecord();
  
  rec.setAttribute("id", "SBM00000X");
  rec.setAttribute("desc", "This is my first submission description\nline 2 This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2");
  rec.setAttribute("prop", 100);
  
  addData(rec);
  
//  rec = new ListGridRecord();
//  
//  rec.setAttribute("id", "SBM00000X");
//  rec.setAttribute("desc", "This is my first submission description");
//  rec.setAttribute("prop", 100);
//
//  ds.addData(rec);
  
//  rec.setDetailDS(ds);
  
//  setAlign(Alignment.CENTER);
//  
//  Label lb = new Label("Query result is empty");
//  
//  lb.setAlign(Alignment.CENTER);
//  
//  addMember(lb);
 }
 
 public void showResult( List<SampleGroupReport> res )
 {
  for( SampleGroupReport sgr : res )
  {
   ListGridRecord rec = new ListGridRecord();
   
   rec.setAttribute("id", sgr.getId());
   rec.setAttribute("desc", sgr.getDescription());

   Record[] det = new Record[ sgr.getAttributes().size() ];
   
   int i=0;
   for( Map.Entry<String, String> me : sgr.getAttributes().entrySet() )
   {
    ListGridRecord dr = new ListGridRecord();
    
    rec.setAttribute(me.getKey(), me.getValue() );

    det[i++]=dr;
   }
   
   rec.setAttribute("details", det);

  }
 }

 protected Canvas getExpansionComponent(final ListGridRecord record)
 {
  VLayout lay =  new VLayout();
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  for( Record r : record.getAttributeAsRecordArray("details") )
   ds.addField(new DataSourceTextField("id", "ID"));
  
  ds.addField(new DataSourceTextField("id", "ID"));
  ds.addField(new DataSourceTextField("desc", "Description", 2000));
  ds.addField(new DataSourceIntegerField("prop", "Property", 111));

  ListGridRecord rec = new ListGridRecord();
  
  rec.setAttribute("id", "SBM00000X");
  rec.setAttribute("desc", "This is my first submission description\nline 2 This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2");
  rec.setAttribute("prop", 100);
  
  ds.addData(rec);
 
  DetailViewer dv = new DetailViewer();
  
  dv.setDataSource(ds);
  
  dv.setAutoFetchData(true);
  
  lay.addMember(dv);
  
  return lay;
  
 }
}
