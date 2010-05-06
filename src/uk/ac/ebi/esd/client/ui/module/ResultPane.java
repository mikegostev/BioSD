package uk.ac.ebi.esd.client.ui.module;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.ObjectReport;
import uk.ac.ebi.esd.client.shared.Pair;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.ExpansionMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
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
  
  setMargin(5);
  
  setShowEdges(true);
  setEdgeSize(6);
  setEdgeImage("gnframe.gif");
  setEdgeMarginSize(10);
  
  setShowAllRecords(true);  
  setWrapCells(true);
  setFixedRecordHeights(false);
  
  setStyleName("reportGrid");
  
//  setBaseStyle("reportGrid");
  
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
  
  idField.setWidth(150);
     
  setFields(idField, descField );
  setExpansionMode(ExpansionMode.DETAIL_FIELD);
  
//  ListGridRecord rec = new ListGridRecord();
//  
//  rec.setAttribute("id", "SBM00000X");
//  rec.setAttribute("desc", "This is my first submission description\nline 2 This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2");
//  rec.setAttribute("prop", 100);
//  
//  addData(rec);
  
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
 
 public void showResult( List<ObjectReport> res )
 {
  selectAllRecords();
  removeSelectedData();
  
  for( ObjectReport sgr : res )
  {
   ListGridRecord rec = new ListGridRecord();
   
   rec.setAttribute("id", sgr.getId());
   rec.setAttribute("desc", sgr.getDescription());

   Record det = new ListGridRecord();
   
   for( Pair<String, String> me : sgr.getAttributes() )
   {
    System.out.println("Attr: "+me.getFirst()+" "+me.getSecond());
    det.setAttribute(me.getFirst(), me.getSecond() );
   }
   
   rec.setAttribute("details", new Record[]{det});
   
   addData(rec);
  }
 }

 protected Canvas getExpansionComponent(final ListGridRecord record)
 {
  final VLayout lay =  new VLayout();
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  Record r = record.getAttributeAsRecordArray("details")[0];
  
  ListGridRecord rec = new ListGridRecord();

  for( String s : r.getAttributes() )
  {
   if( s.equals("__ref") )
    continue;
   
   ds.addField(new DataSourceTextField(s, s));
   System.out.println("At: "+s+" "+r.getAttributeAsString(s));
   rec.setAttribute(s, r.getAttributeAsString(s));
  }
   
  
//  ds.addField(new DataSourceTextField("id", "ID"));
//  ds.addField(new DataSourceTextField("desc", "Description", 2000));
//  ds.addField(new DataSourceIntegerField("prop", "Property", 111));

  
//  rec.setAttribute("id", "SBM00000X");
//  rec.setAttribute("desc", "This is my first submission description\nline 2 This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2");
//  rec.setAttribute("prop", 100);
  
  ds.addData(rec);
 
  DetailViewer dv = new DetailViewer();
  
  dv.setDataSource(ds);
  
  dv.setAutoFetchData(true);
  
  lay.addMember(dv);
  
  
  DynamicForm lnkform = new DynamicForm();
  lnkform.setWidth(500);
  
  lnkform.setNumCols(4);
  
  LinkItem li = new LinkItem("allsamples");
  li.setTitle("Show");
  li.setLinkTitle("all samples");
 
  li.addClickHandler( new ClickHandler()
  {
   @Override
   public void onClick(ClickEvent event)
   {
    showSamples(lay,record.getAttributeAsString("id"));
   }


  });
  
  LinkItem li2 = new LinkItem("selsamples");
  li2.setTitle("Show");
  li2.setLinkTitle("selected samples");
  
  
  
  lnkform.setFields( li, li2 );
  
  lay.addMember(lnkform);
  
  return lay;
  
 }
 
 private void showSamples(VLayout lay, String grpID)
 {
  QueryService.Util.getInstance().getSamplesByGroup(grpID,new AsyncCallback<List<ObjectReport>>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    arg0.printStackTrace();
   }

   @Override
   public void onSuccess(List<ObjectReport> smpls)
   {
    Set<String> hdr = new TreeSet<String>();

    for( ObjectReport o : smpls )
    {
     for( Pair<String, String> at :  o.getAttributes() )
     {
      hdr.add(at.getFirst());
     }
    }
   
   }});
 }
}
