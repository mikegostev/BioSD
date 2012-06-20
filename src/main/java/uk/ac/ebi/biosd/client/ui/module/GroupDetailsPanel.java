package uk.ac.ebi.biosd.client.ui.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.age.ui.client.LinkClickListener;
import uk.ac.ebi.age.ui.client.LinkManager;
import uk.ac.ebi.age.ui.client.module.ObjectImprintViewerWindow;
import uk.ac.ebi.age.ui.client.module.PagingRuler;
import uk.ac.ebi.age.ui.shared.imprint.AttributeImprint;
import uk.ac.ebi.age.ui.shared.imprint.ClassImprint;
import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;
import uk.ac.ebi.age.ui.shared.imprint.Value;
import uk.ac.ebi.biosd.client.BioSDGWTService;
import uk.ac.ebi.biosd.client.query.AttributedImprint;
import uk.ac.ebi.biosd.client.query.SampleList;
import uk.ac.ebi.biosd.client.shared.AttributeReport;
import uk.ac.ebi.biosd.client.shared.MaintenanceModeException;
import uk.ac.ebi.biosd.client.shared.Pair;
import uk.ac.ebi.biosd.client.ui.SampleListGrid;
import uk.ac.ebi.biosd.client.ui.SourceIconBundle;
import uk.ac.ebi.biosd.client.ui.test.ObjectViewer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;

public class GroupDetailsPanel extends VLayout
{
 private static final int BRIEF_LEN=200;
 
 private boolean allSamples;
 private String groupID;
 
 private String query;
 private boolean searchAtNames;
 private boolean searchAtValues;
 
 private List< Pair<String, String> > otherInfoList;
 private Map<String, List<AttributedImprint> > attMap = new HashMap<String, List<AttributedImprint>>();
// private List< AttributedImprint > publList;
// private List< AttributedImprint > contList;
 
 private PagingRuler pager;
 
 @SuppressWarnings("unchecked")
 public GroupDetailsPanel(final Record r, final String query, final boolean searchAtNames, final boolean searchAtValues)
 {
  this.query=query;
  this.searchAtNames=searchAtNames;
  this.searchAtValues=searchAtValues;
  
  DataSource ds = new DataSource();
  
  ds.setClientOnly(true);
  
  ListGridRecord rec = new ListGridRecord();

  groupID = r.getAttributeAsString("__ID");

  String[] attrs = r.getAttributes();

  for( String s : attrs )
  {
   if( s.startsWith("__")  )
    continue;
   
   ds.addField(new DataSourceTextField(s, s));
   
   String val = r.getAttributeAsString(s);
   
   if("Link".equals(s) )
   {
    String dsAttr = r.getAttributeAsString("Data Source");
    
    if( dsAttr != null )
    {
     String icon = SourceIconBundle.getIcon(dsAttr);
     
     if( icon != null )
      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='"+icon+"'></a>");
     else
      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
//     
//     if("Array Express".equals(dsAttr))
//      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='images/ae.png'></a>");
//     else if("Pride".equals(dsAttr))
//      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='images/pride.jpg'></a>");
//     else
//      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
    }
    else
     rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
    
   }
   else if("Submission PubMed ID".equals(s))
    rec.setAttribute(s, "<a target='_blank' border=0 href='http://www.ncbi.nlm.nih.gov/pubmed/"+val+"'>"+val+"</a>");
   else if("Submission DOI".equals(s))
    rec.setAttribute(s, "<a target='_blank' border=0 href='http://dx.doi.org/"+val+"'>"+val+"</a>");
   else if( val.length() > 8 && val.substring(0, 7).equalsIgnoreCase("http://") )
    rec.setAttribute(s, "<a target=\"_blank\" href=\"" + val + "\">" + val + "</a>");
   else
    rec.setAttribute(s, val);

   //   System.out.println("At: "+s+" "+r.getAttributeAsString(s));
  }

  for( String attr : attrs )
  {
   if( attr.startsWith("__$att$") )
   {
    List< AttributedImprint > list = (List< AttributedImprint >) r.getAttributeAsObject(attr);
    String title = attr.substring(7);
    
    String repstr = makeRepresentationString2(list, title);

    ds.addField(new DataSourceTextField(title, title) );
    
    rec.setAttribute(title, repstr);
    
    attMap.put(title, list);
   }
  }
  
  
  otherInfoList = (List< Pair<String, String> >) r.getAttributeAsObject("__other");
  
  if( otherInfoList != null && otherInfoList.size() > 0 )
  {
   String repstr = "<div style='float: left' class='briefObjectRepString'>";
   
   int cCount = 0;
   
   for( Pair<String, String> fstEl : otherInfoList )
   {
    if( cCount > BRIEF_LEN )
     break;
    
    repstr += "<b>"+fstEl.getFirst()+"</b>"; 
    
    repstr += ": "+fstEl.getSecond()+"; ";

    cCount+=fstEl.getFirst().length()+fstEl.getSecond().length();
   }

   repstr += "</div><div><a class='el' href='javascript:linkClicked(&quot;"+groupID+"&quot;,&quot;other&quot;)'>more</a></div>";

   
   ds.addField(new DataSourceTextField("Other", "Other") );
   
   rec.setAttribute("Other", repstr);
  }
   
  int total = r.getAttributeAsInt("__total");
  int matched = r.getAttributeAsInt("__matched");
 
  ds.addField(new DataSourceTextField("__smm", "Total/matched samples"));

  rec.setAttribute("__smm", String.valueOf(total+"/"+matched));
  
  ds.addData(rec);
  
  
  DetailViewer dv = new DetailViewer();
  dv.setWidth("90%");
  dv.setDataSource(ds);
  dv.setStyleName("groupDetails");
  dv.setCanSelectText(true);
  
  dv.setAutoFetchData(true);
  
  Canvas spc = new Canvas();
  spc.setHeight(15);

  addMember(spc);
  
  addMember(dv);
  
  spc = new Canvas();
  spc.setHeight(5);
  addMember(spc);
  
  if( matched > 0 )
  {

   DynamicForm lnkform = new DynamicForm();
   lnkform.setWidth(500);
   lnkform.setStyleName("sampleListLink");

   lnkform.setNumCols(6);

   LinkItem li = new LinkItem("allsamples");
   li.setTitle("Show");
   li.setLinkTitle("all samples");

   li.addClickHandler(new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     showAllSamples(1);
    }
   });

   LinkItem li2 = new LinkItem("selsamples");
   li2.setTitle("Show");
   li2.setLinkTitle("samples matching the query");

   li2.addClickHandler(new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     showMatchedSamples(1);
    }
   });

   LinkItem li3 = new LinkItem("hidesamples");
   li3.setTitle("Hide");
   li3.setLinkTitle("samples");

   li3.addClickHandler(new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     Canvas[] membs = getMembers();

     if(membs[membs.length - 1] instanceof ListGrid)
      removeMember(membs[membs.length - 1]);

     pager.setVisible(false);
    }
   });

   lnkform.setFields(li, li2, li3);

   addMember(lnkform);
  }
  
  spc = new Canvas();
  spc.setHeight(5);
  addMember(spc);

  pager = new PagingRuler(groupID);
  pager.setWidth("99%");
  pager.setVisible(false);
  addMember(pager);
  
  showAllSamples(1);
  
  LinkManager.getInstance().addLinkClickListener(groupID, new LinkClickListener()
  {
   @Override
   public void linkClicked(String param)
   {
    if( "other".equals(param) )
    {
     new NameValuePanel( otherInfoList ).show();
     return;
    }
    else if( attMap.containsKey(param) )
    {
     new AttributedListPanel( param, attMap.get(param) ).show();
     return;
    }
   
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
 
 private String makeRepresentationString( List< AttributedImprint > list, String theme )
 {
  AttributedImprint fstEl = list.get(0);
  String repstr = "";
  
  int lastBold = -1;
  
  for( AttributeReport attr : fstEl.getAttributes() )
  {
   if( repstr.length() > BRIEF_LEN )
    break;
   
   repstr += "<b>"+attr.getName()+"</b>"; 
   
   lastBold=repstr.length();
   
   repstr += ": "+attr.getValue()+"; ";
  }

  if( repstr.length() > BRIEF_LEN )
   repstr=repstr.substring(0,BRIEF_LEN);
  
  if( BRIEF_LEN < lastBold )
   repstr+="</b>";
 
  repstr += "... <a class='el' href='javascript:linkClicked(&quot;"+groupID+"&quot;,&quot;"+theme+"&quot;)'>more</a>";
  
  return repstr;
 }
 
 private String makeRepresentationString2( List< AttributedImprint > list, String theme )
 {
  AttributedImprint fstEl = list.get(0);
  String repstr = "<div style='float: left' class='briefObjectRepString'>";
  
  int cCount = 0;
  
  for( AttributeReport attr : fstEl.getAttributes() )
  {
   if( cCount > BRIEF_LEN )
    break;
   
   repstr += "<b>"+attr.getName()+"</b>"; 
   
   repstr += ": "+attr.getValue()+"; ";

   cCount+=attr.getName().length()+attr.getValue().length();
  }

  repstr += "</div><div><a class='el' href='javascript:linkClicked(&quot;"+groupID+"&quot;,&quot;"+theme+"&quot;)'>more</a></div>";
  
  return repstr;
 }
 
 private void showAllSamples( final int pNum )
 {
  allSamples=true;
  
//  WaitWindow.showWait();
  
  BioSDGWTService.Util.getInstance().getSamplesByGroup(groupID, query, searchAtNames, searchAtValues,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
   new AsyncCallback<SampleList>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    WaitWindow.hideWait();
    
    if( arg0 instanceof MaintenanceModeException )
    {
     SC.say("Sorry. The system is being maintained right now. Please repeate your action later");
     return;
    }

    arg0.printStackTrace();
   }

   @Override
   public void onSuccess(final SampleList arg0)
   {
    Scheduler.get().scheduleDeferred(new ScheduledCommand()
    {
     @Override
     public void execute()
     {
      renderSampleList2(GroupDetailsPanel.this,arg0,pNum);
//      WaitWindow.hideWait();
     }
    });
    
//    DeferredCommand.addCommand(new Command()
//    {
//     @Override
//     public void execute()
//     {
//      renderSampleList(GroupDetailsPanel.this,arg0,pNum);
//      WaitWindow.hideWait();
//     }
//    });
   }
  });
 }
 
 private void showMatchedSamples( final int pNum )
 {
  allSamples=false;
  
  WaitWindow.showWait();
  BioSDGWTService.Util.getInstance().getSamplesByGroupAndQuery(groupID, query, searchAtNames, searchAtValues,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
   
  new AsyncCallback<SampleList>(){

   @Override
   public void onFailure(Throwable arg0)
   {
    if( arg0 instanceof MaintenanceModeException )
    {
     SC.say("Sorry. The system is being maintained right now. Please repeate your action later");
     return;
    }

    arg0.printStackTrace();
    WaitWindow.hideWait();
   }

   @Override
   public void onSuccess( final SampleList rep )
   {
    renderSampleList2(GroupDetailsPanel.this,rep, pNum);
    WaitWindow.hideWait();
   }
  });
 }

 /*
 private void renderSampleList(final VLayout lay, Report smpls, int pg)
 {
  
  DataSource ds = new DataSource();
  ds.setClientOnly(true);

  ListGridRecord[] records = new ListGridRecord[smpls.getObjects().size()];

  int rc = 0;

  Map<String, AttributeFieldInfo> hdr = new TreeMap<String, AttributeFieldInfo>();

  Set<String> localHdr = new TreeSet<String>();

  for(GroupImprint o : smpls.getObjects())
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

  if( smpls.getTotalGroups() > ResultPane.MAX_SAMPLES_PER_PAGE )
  {
   pager.setContents(pg, smpls.getTotalGroups(),  ResultPane.MAX_SAMPLES_PER_PAGE, null, null );
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
  attrList.setStyleName("sampleGrid");
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

 */
 
 private void renderSampleList2(final VLayout lay, SampleList smpls, int pg)
 {
  ListGrid attrList = new SampleListGrid();
  
  attrList.setShowAllRecords(true);
//  attrList.setShowRowNumbers(true); 
  
  attrList.setWidth("99%");
  attrList.setHeight(1);
  attrList.setBodyOverflow(Overflow.VISIBLE);
  attrList.setOverflow(Overflow.VISIBLE);
  attrList.setLeaveScrollbarGap(false);
  attrList.setStyleName("sampleGrid");
  attrList.setMargin(5);
  attrList.setHoverWidth(200);
  attrList.setShowEdges(true);
  
//  DataSource ds = new DataSource();
//  ds.setClientOnly(true);
//
//  attrList.setDataSource(ds);
//  attrList.setShowHover(true);
  
//  attrList.setAutoFetchData(true);

//  for( AttributeClassReport cls : smpls.getHeader() )
//  {
//   DataSourceTextField dsf = new DataSourceTextField(cls.getId(), cls.isCustom()?cls.getName():("<b>"+cls.getName()+"</b>"));
//   
//   if( cls.getId().equals("__id") )
//    dsf.setPrimaryKey(true);
//   
//   ds.addField(dsf );
//  }

  final ListGridField[] fields = new ListGridField[smpls.getHeaders().size()+1];

  String prideKey = null;
  
  final List<ClassImprint> header = smpls.getHeaders();
  
  fields[0] = new ListGridField("__num", "N" );
  fields[0].setWidth(25);

  
  int i=1;
  for( ClassImprint cls : header )
  {
   fields[i] = new ListGridField(cls.getId(), cls.isCustom()?cls.getName():("<b>"+cls.getName()+"</b>") );
   fields[i].setShowHover(true);
   
   if( cls.getName().equals("Pride ID") )
    prideKey = cls.getId();
   
   i++;
  }

  int offset = (pg-1)*ResultPane.MAX_SAMPLES_PER_PAGE;
//  attrList.setDataSource(ds);
  ArrayList<ListGridRecord> records = new ArrayList<ListGridRecord>();

  ArrayList<ListGridRecord> recList = new ArrayList<ListGridRecord>();
  
  for( ObjectImprint sample : smpls.getSamples() )
  {
   offset++;
   
   ListGridRecord mainRec = new ListGridRecord();

   mainRec.setAttribute("__obj", sample);
   mainRec.setAttribute("__id", "<span class='idCell'>"+sample.getId()+"</span>");
   mainRec.setAttribute("__ord", 0);
   mainRec.setAttribute("__num", offset);
   
   recList.clear();
   
   recList.add(mainRec);
   
   int colNum=0;
   for( AttributeImprint at : sample.getAttributes() )
   {
    colNum++;
    
    String atId = at.getClassImprint().getId();
    
    int recNum = -1;
    for( Value v : at.getValues() )
    {
     recNum++;
     
     ListGridRecord cRec = null;
     
     if( recNum >= recList.size() )
     {
      recList.add( cRec = new ListGridRecord() );

      cRec.setAttribute("__obj", sample);
      cRec.setAttribute("__ord", recNum);
//      cRec.setAttribute("__num", offset);
     }
     else
      cRec = recList.get(recNum);
     
     if( atId.equals(prideKey) )
     {
      cRec.setAttribute(atId, "<a target='_blank' href='http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber="+v.getStringValue()+"'>"+v.getStringValue()+"</a>");
     }
     else if( v.getAttributes() != null && v.getAttributes().size() > 0 )
      cRec.setAttribute(atId, "<span class='qualifiedCell'>"+v.getStringValue()+"</span>");
     else
     {
      String val = v.getStringValue();
      
      if( val.length() > 8 && val.substring(0, 7).equalsIgnoreCase("http://") )
       cRec.setAttribute(atId, "<a target=\"_blank\" href=\"" + val + "\">" + val + "</a>");
      else if( at.getClassImprint() == header.get(0)  )
       mainRec.setAttribute(atId, "<span class='idCell'>"+val+"</span>");
      else
       cRec.setAttribute(atId, val);
     }     
    }
    

   }
   
   for( ListGridRecord lr : recList )
    records.add(lr);
  }

//  ListGridRecord rec = new ListGridRecord();
//  rec.setAttribute("__id", "XYN");
//  ds.addData(rec);

  if( smpls.getTotalRecords() > ResultPane.MAX_SAMPLES_PER_PAGE )
  {
   pager.setContents(pg, smpls.getTotalRecords(),  ResultPane.MAX_SAMPLES_PER_PAGE, null, null );
   pager.setVisible(true);
  }
  else
   pager.setVisible(false);

  
  attrList.setFields(fields);
  
  ListGridRecord[] data = new ListGridRecord[records.size()];
  
  attrList.setData( records.toArray( data ) );
  
  attrList.setHoverCustomizer(new HoverCustomizer()
  {
   @Override
   public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
   {
    if( colNum == 0)
     return null;
  
//    return value.toString();
    return record.getAttributeAsString(fields[colNum].getName());
   }
  });
  
  attrList.addCellClickHandler( new CellClickHandler()
  {
   @Override
   public void onCellClick(CellClickEvent event)
   {
    String attrId = header.get(event.getColNum()-1).getId();
    
    ObjectImprint sample = (ObjectImprint) event.getRecord().getAttributeAsObject("__obj");
    
    int valOrd = event.getRecord().getAttributeAsInt("__ord");
    
    Value clickedVal = null;
    
    for( AttributeImprint at : sample.getAttributes() )
    {
     if(attrId.equals(at.getClassImprint().getId()))
     {
      if( at.getValueCount() <= valOrd )
       return;
      
      clickedVal = at.getValues().get(valOrd);
      break;
     }
    }
    
//    System.out.println("Clicked: "+attrId+" -> "+clickedObj.getStringValue());
    
    if( event.getColNum() != 1  )
    {
     if( clickedVal == null || clickedVal.getAttributes() == null || clickedVal.getAttributes().size() == 0 )
      return;

     new ObjectViewer(clickedVal).show();
     return;
    }
    
    new ObjectImprintViewerWindow( sample ).show();
//    new SampleViewer(header, event.getRecord()).show();
   }
  });

  Canvas[] membs = lay.getMembers();
  
  if( membs[membs.length-1] instanceof ListGrid )
   lay.removeMember(membs[membs.length-1]);

  
  lay.addMember(attrList);
//  attrList.fetchData();
  
 }

}
