package uk.ac.ebi.esd.client.ui.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.ObjectReport;
import uk.ac.ebi.esd.client.shared.AttributeReport;
import uk.ac.ebi.esd.client.ui.AttributeFieldInfo;
import uk.ac.ebi.esd.client.ui.ResultRenderer;
import uk.ac.ebi.esd.client.ui.SampleListGrid;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.ExpansionMode;
import com.smartgwt.client.types.Overflow;
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

public class ResultPane extends ListGrid implements ResultRenderer
{
 private String query;
 private boolean searchSamples;
 private boolean searchGroups;
 private boolean searchAtNames;
 private boolean searchAtValues;
 
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
  
  setBodyOverflow(Overflow.VISIBLE);
  setOverflow(Overflow.VISIBLE);
  
  setStyleName("reportGrid");
  
//  setBaseStyle("reportGrid");
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  ds.addField(new DataSourceTextField("id", "ID"));
  ds.addField(new DataSourceTextField("sampCnt", "Samples", 100));
  ds.addField(new DataSourceTextField("desc", "Description", 2000));
  ds.addField(new DataSourceIntegerField("prop", "Property", 111));
  
//  setDataSource(ds);
  
  setCanExpandRecords(true); 
  
  ListGridField idField = new ListGridField("id","ID", 100);  
  ListGridField sCntField = new ListGridField("sampCnt","Samples",100);
  ListGridField descField = new ListGridField("desc","Description");
//  ListGridField propField = new ListGridField("prop","AdditionalProp");
  
//  propField.setHidden(true);
  
  idField.setWidth(200);
     
  setFields(idField,sCntField, descField );
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
 
 public void showResult( List<ObjectReport> res, String qry, boolean sSmp, boolean sGrp, boolean sAtrNm, boolean sAtrVl )
 {
  query = qry;
  searchSamples=sSmp;
  searchGroups=sGrp;
  searchAtNames=sAtrNm;
  searchAtValues=sAtrVl;
  
  selectAllRecords();
  removeSelectedData();
  
  for( ObjectReport sgr : res )
  {
   ListGridRecord rec = new ListGridRecord();
   
   rec.setAttribute("id", sgr.getId());
   rec.setAttribute("sampCnt", sgr.getRefCount());
   rec.setAttribute("desc", sgr.getDescription());

   Record det = new ListGridRecord();
   
   for( AttributeReport ar : sgr.getAttributes() )
   {
    System.out.println("Attr: "+ar.getName()+" "+ar.getValue());
    det.setAttribute( ar.getName(), ar.getValue() );
   }
  
   det.setAttribute("Total samples", sgr.getRefCount());
   det.setAttribute("Selected samples", sgr.getMatchedSamples()==null?0:sgr.getMatchedSamples().size());
  
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
  
  lnkform.setNumCols(6);
  
  LinkItem li = new LinkItem("allsamples");
  li.setTitle("Show");
  li.setLinkTitle("all samples");
 
  li.addClickHandler( new ClickHandler()
  {
   @Override
   public void onClick(ClickEvent event)
   {
    QueryService.Util.getInstance().getSamplesByGroup(record.getAttributeAsString("id"),new AsyncCallback<List<ObjectReport>>(){

     @Override
     public void onFailure(Throwable arg0)
     {
      arg0.printStackTrace();
     }

     @Override
     public void onSuccess(List<ObjectReport> arg0)
     {
      renderSampleList(lay,arg0);
     }
    });
   }
  });
  
  LinkItem li2 = new LinkItem("selsamples");
  li2.setTitle("Show");
  li2.setLinkTitle("selected samples");
  
  li2.addClickHandler( new ClickHandler()
  {
   @Override
   public void onClick(ClickEvent event)
   {
    QueryService.Util.getInstance().getSamplesByGroupAndQuery(record.getAttributeAsString("id"), query, searchAtNames, searchAtValues,
     
    new AsyncCallback<List<ObjectReport>>(){

     @Override
     public void onFailure(Throwable arg0)
     {
      arg0.printStackTrace();
     }

     @Override
     public void onSuccess(List<ObjectReport> arg0)
     {
      renderSampleList(lay,arg0);
     }
    });
   }
  });

  LinkItem li3 = new LinkItem("hidesamples");
  li3.setTitle("Hide");
  li3.setLinkTitle("samples");
  
  li3.addClickHandler( new ClickHandler()
  {
   @Override
   public void onClick(ClickEvent event)
   {
    Canvas[] membs = lay.getMembers();
    
    if( membs[membs.length-1] instanceof ListGrid )
     lay.removeMember(membs[membs.length-1]);
   }
  });

  lnkform.setFields( li, li2, li3 );
  
  lay.addMember(lnkform);
  
  return lay;
  
 }
 
 
 private void renderSampleList(final VLayout lay, List<ObjectReport> smpls)
 {
  
  DataSource ds = new DataSource();
  ds.setClientOnly(true);

  ListGridRecord[] records = new ListGridRecord[smpls.size()];

  int rc = 0;

  Map<String, AttributeFieldInfo> hdr = new TreeMap<String, AttributeFieldInfo>();

  Set<String> localHdr = new TreeSet<String>();

  for(ObjectReport o : smpls)
  {
   localHdr.clear();

   ListGridRecord rec = new ListGridRecord();

   for(AttributeReport at : o.getAttributes())
   {
    String fname = null;
    int i = 0;
    while(true)
    {
     fname = at.getName() + "#" + (at.isCustom() ? "C" : "D") + "#" + i;

     if(!localHdr.contains(fname))
      break;
    }

    localHdr.add(fname);

    AttributeFieldInfo h = hdr.get(fname);

    if(h == null)
     hdr.put(fname, new AttributeFieldInfo(at.getName(), fname, at.getOrder()));
    else
     h.add(at.getOrder());

    rec.setAttribute(fname, at.getValue());
   }

   records[rc++] = rec;
  }

  ArrayList<AttributeFieldInfo> hlist = new ArrayList<AttributeFieldInfo>(hdr.size());
  hlist.addAll(hdr.values());
  Collections.sort(hlist);

  ListGrid attrList = new SampleListGrid();
  
  attrList.setShowAllRecords(true);
  attrList.setShowRowNumbers(true); 
  
  attrList.setHeight(1);
  attrList.setBodyOverflow(Overflow.VISIBLE);
  attrList.setOverflow(Overflow.VISIBLE);
  attrList.setLeaveScrollbarGap(false);

  ListGridField[] lfl = new ListGridField[hlist.size()];

  for(int i = 0; i < lfl.length; i++)
   lfl[i] = new ListGridField(hlist.get(i).getField(), hlist.get(i).getTitle());

  if( lfl.length > 0 )
  {
   attrList.setFields(lfl);
   attrList.setData(records);
  }
  else
   attrList.setFields(new ListGridField("ID","ID"));
  
  Canvas[] membs = lay.getMembers();
  
  if( membs[membs.length-1] instanceof ListGrid )
   lay.removeMember(membs[membs.length-1]);
  
  lay.addMember(attrList);

 }
 
 
}
