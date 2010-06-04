package uk.ac.ebi.esd.client.ui.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.ebi.esd.client.LinkClickListener;
import uk.ac.ebi.esd.client.LinkManager;
import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.ObjectReport;
import uk.ac.ebi.esd.client.query.Report;
import uk.ac.ebi.esd.client.shared.AttributeReport;
import uk.ac.ebi.esd.client.ui.AttributeFieldInfo;
import uk.ac.ebi.esd.client.ui.SampleListGrid;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;

public class GroupDetailsPanel extends VLayout
{
 private boolean allSamples;
 private String groupID;
 
 private String query;
 private boolean searchAtNames;
 private boolean searchAtValues;
 
 private PagingRuler pager;
 
 public GroupDetailsPanel(final Record r, final String query, final boolean searchAtNames, final boolean searchAtValues)
 {
  this.query=query;
  this.searchAtNames=searchAtNames;
  this.searchAtValues=searchAtValues;
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  ListGridRecord rec = new ListGridRecord();

  for( String s : r.getAttributes() )
  {
   if( s.equals("__ref") )
    continue;
   
   ds.addField(new DataSourceTextField(s, s));
   
   if("AE Link".equals(s))
    rec.setAttribute(s, "<a target='_blank' border=0 href='"+r.getAttributeAsString(s)+"'><img border=0 src='images/ae.png'></a>");
   else
    rec.setAttribute(s, r.getAttributeAsString(s));

   //   System.out.println("At: "+s+" "+r.getAttributeAsString(s));
  }
   
  groupID = r.getAttributeAsString("ID");
  
  ds.addData(rec);
 
  DetailViewer dv = new DetailViewer();
  dv.setWidth("90%");
  dv.setDataSource(ds);
  dv.setStyleName("groupDetails");
  
  dv.setAutoFetchData(true);
  
  Canvas spc = new Canvas();
  spc.setHeight(15);

  addMember(spc);
  
  addMember(dv);
  
  
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
    showAllSamples(1);
   }
  });
  
  LinkItem li2 = new LinkItem("selsamples");
  li2.setTitle("Show");
  li2.setLinkTitle("matched samples");
  
  li2.addClickHandler( new ClickHandler()
  {
   @Override
   public void onClick(ClickEvent event)
   {
    showMatchedSamples( 1 );
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
    Canvas[] membs = getMembers();
    
    if( membs[membs.length-1] instanceof ListGrid )
     removeMember(membs[membs.length-1]);
    
    pager.setVisible(false);
   }
  });

  lnkform.setFields( li, li2, li3 );
  
  addMember(lnkform);
  
  spc = new Canvas();
  spc.setHeight(15);
  addMember(spc);

  pager = new PagingRuler(groupID);
  pager.setWidth("99%");
  pager.setVisible(false);
  addMember(pager);
  
  
  LinkManager.getInstance().addLinkClickListener(groupID, new LinkClickListener()
  {
   @Override
   public void linkClicked(String param)
   {
    int pNum = 1;
    
    try
    {
     pNum=Integer.parseInt(param);
    }
    catch(Exception e)
    {
    }
    
    if( allSamples )
     showAllSamples(pNum);
    else
     showMatchedSamples(pNum);
   }
  });
 }
 
 private void showAllSamples( final int pNum )
 {
  allSamples=true;
  
  WaitWindow.showWait();
  
  QueryService.Util.getInstance().getSamplesByGroup(groupID,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
   new AsyncCallback<Report>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    WaitWindow.hideWait();
    arg0.printStackTrace();
   }

   @Override
   public void onSuccess(final Report arg0)
   {
    DeferredCommand.addCommand(new Command()
    {
     @Override
     public void execute()
     {
      renderSampleList(GroupDetailsPanel.this,arg0,pNum);
      WaitWindow.hideWait();
     }
    });
   }
  });
 }
 
 private void showMatchedSamples( final int pNum )
 {
  allSamples=false;
  
  WaitWindow.showWait();
  QueryService.Util.getInstance().getSamplesByGroupAndQuery(groupID, query, searchAtNames, searchAtValues,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
   
  new AsyncCallback<Report>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    arg0.printStackTrace();
    WaitWindow.hideWait();
   }

   @Override
   public void onSuccess( final Report rep )
   {
    renderSampleList(GroupDetailsPanel.this,rep, pNum);
    WaitWindow.hideWait();
   }
  });
 }

 
 private void renderSampleList(final VLayout lay, Report smpls, int pg)
 {
  
  DataSource ds = new DataSource();
  ds.setClientOnly(true);

  ListGridRecord[] records = new ListGridRecord[smpls.getObjects().size()];

  int rc = 0;

  Map<String, AttributeFieldInfo> hdr = new TreeMap<String, AttributeFieldInfo>();

  Set<String> localHdr = new TreeSet<String>();

  for(ObjectReport o : smpls.getObjects())
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
     
     i++;
    }

    localHdr.add(fname);

    AttributeFieldInfo h = hdr.get(fname);

    if(h == null)
    {
     hdr.put(fname, h = new AttributeFieldInfo(at.isCustom()?at.getName():"<b>"+at.getName()+"</b>", fname, at.getOrder()));
     h.add(at.getOrder());
    }
     
//    else
//     h.add(at.getOrder());

    rec.setAttribute(fname, at.getValue());
   }

   records[rc++] = rec;
  }

  ArrayList<AttributeFieldInfo> hlist = new ArrayList<AttributeFieldInfo>(hdr.size());
  hlist.addAll(hdr.values());
  Collections.sort(hlist);

  if( smpls.getTotalRecords() > ResultPane.MAX_SAMPLES_PER_PAGE )
  {
   pager.setPagination(pg, smpls.getTotalRecords(),  ResultPane.MAX_SAMPLES_PER_PAGE );
   pager.setVisible(true);
  }
  else
   pager.setVisible(false);
  
  ListGrid attrList = new SampleListGrid();
  
  attrList.setShowAllRecords(true);
  attrList.setShowRowNumbers(true); 
  
  attrList.setWidth("99%");
  attrList.setHeight(1);
  attrList.setBodyOverflow(Overflow.VISIBLE);
  attrList.setOverflow(Overflow.VISIBLE);
  attrList.setLeaveScrollbarGap(false);
  attrList.addStyleName("sampleGrid");
  attrList.setMargin(10);
  attrList.setHoverWidth(200);
  attrList.setShowEdges(true);

  final ListGridField[] lfl = new ListGridField[hlist.size()];

  for(int i = 0; i < lfl.length; i++)
  {
   lfl[i] = new ListGridField(hlist.get(i).getField(), hlist.get(i).getTitle());
   lfl[i].setShowHover(true);
  }
  
  attrList.setHoverCustomizer(new HoverCustomizer()
  {
   @Override
   public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
   {
    if( colNum == 0)
     return null;
    
    return record.getAttributeAsString(lfl[colNum-1].getName());
   }
  });
  
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
