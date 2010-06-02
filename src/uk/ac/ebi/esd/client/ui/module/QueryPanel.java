package uk.ac.ebi.esd.client.ui.module;

import java.util.LinkedHashMap;

import uk.ac.ebi.esd.client.LinkClickListener;
import uk.ac.ebi.esd.client.LinkManager;
import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.Report;
import uk.ac.ebi.esd.client.ui.ResultRenderer;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryPanel extends VLayout implements LinkClickListener
{
 private TextItem queryField;
 private ComboBoxItem where;
 private ComboBoxItem what;
 
 private ResultRenderer resultCallback;
 
 private boolean  searchAttribNames;
 private boolean searchAttribValues;
 private boolean searchGroup;
 private boolean searchSample;
 private String query;

 
 public QueryPanel( ResultRenderer cb )
 {
  resultCallback = cb;
  
//  setAlign(Alignment.CENTER);
  setPadding(3);
  
  HLayout hstrip = new HLayout();
  hstrip.setWidth100();
  hstrip.setHeight(125);
  
  Canvas left = new Canvas();
  left.setWidth(300);
  left.setHeight100();
  left.setStyleName("leftBanner");
  
//  Canvas cenleft = new Canvas();
//  left.setWidth(550);
//  left.setHeight100();
//  left.setStyleName("centerleftBanner");
  
  Canvas cenright = new Canvas();
  cenright.setWidth100();
  cenright.setHeight100();
  cenright.setStyleName("centerrightBanner");
  
  
  Canvas right = new Canvas();
  right.setWidth(229);
  left.setHeight100();
  right.setStyleName("rightBanner");
  
  DynamicForm f1 = new DynamicForm();
  f1.setStyleName("reqForm");
  f1.setWidth(550);
  f1.setTitleSuffix("");
  
  f1.setColWidths("130","100","40","70","*","50");
  f1.setNumCols(6);

//  f1.setCellBorder(1);
  
  QueryAction act = new QueryAction();
  
  PickerIcon searchPicker = new PickerIcon(PickerIcon.SEARCH, act);
  
  queryField = new TextItem("refreshPicker","Query");
  queryField.setColSpan(5);
  queryField.setWidth(410);
  queryField.setTitleStyle("queryFieldTitle");
  queryField.setShowTitle(false);
  queryField.setIcons(searchPicker);
  queryField.addKeyPressHandler(act);
  
//  ButtonItem bt = new ButtonItem("Button","Search");
//  bt.setStartRow(false);
//  bt.setEndRow(true);
//  bt.addClickHandler( new QueryAction() );

 
  where = new ComboBoxItem();
  where.setTitle("");
  where.setTitleStyle("whereTitle");
  where.setWidth(120);
  
  LinkedHashMap<String, String> vm = new LinkedHashMap<String, String>();
  vm.put("both", "samples & groups");
  vm.put("sample", "samples");
  vm.put("group", "groups");
  
  where.setValueMap(vm);
  where.setDefaultToFirstOption(true);

  what = new ComboBoxItem();
  what.setTitle("");
  what.setTitleStyle("whatTitle");

  LinkedHashMap<String, String> st = new LinkedHashMap<String, String>();
  st.put("both", "attribute name & values");
  st.put("val", "attribute values");
  st.put("name", "attribute names");
  
  what.setValueMap(st);
  what.setDefaultToFirstOption(true);
//  what.setValue("attribute name & values");

  SpacerItem sp0 = new SpacerItem();
  sp0.setHeight(30);
  sp0.setEndRow(true);

  SpacerItem sp1 = new SpacerItem();
  
  SpacerItem sp2 = new SpacerItem();
  sp2.setEndRow(true);

  SpacerItem sp3 = new SpacerItem();
  
  f1.setFields(sp0, sp1,queryField, sp2, sp3, where, what);
//  f1.setBorder("1px solid blue");
  
  hstrip.addMember(left);
  hstrip.addMember(f1);
  hstrip.addMember(cenright);
  hstrip.addMember(right);
  
  addMember(hstrip);
 
 
  LinkManager.getInstance().addLinkClickListener("groupPage", this);
 }
 
 
 private class QueryAction implements  FormItemClickHandler, KeyPressHandler, Command // ClickHandler,
 {

  public void onKeyPress(KeyPressEvent event)
  {
   if("Enter".equals(event.getKeyName()))
    DeferredCommand.addCommand(this);
  }

  public void onFormItemClick(FormItemIconClickEvent event)
  {
   DeferredCommand.addCommand(this);
  }

  public void execute()
  {
   if("val".equals(what.getValue()))
   {
    searchAttribNames = false;
    searchAttribValues = true;
   }
   else if("name".equals(what.getValue()))
   {
    searchAttribNames = true;
    searchAttribValues = false;
   }
   else if("both".equals(what.getValue()))
   {
    searchAttribValues = true;
    searchAttribNames = true;
   }

   if(where.getValue().equals("sample"))
   {
    searchSample = true;
    searchGroup = false;
   }
   else if(where.getValue().equals("group"))
   {
    searchSample = false;
    searchGroup = true;
   }
   else if(where.getValue().equals("both"))
   {
    searchSample = true;
    searchGroup = true;
   }

   query = (String) queryField.getValue();

   QueryService.Util.getInstance().selectSampleGroups(query, searchSample, searchGroup,
     searchAttribNames, searchAttribValues, 0, ResultPane.MAX_GROUPS_PER_PAGE, new AsyncCallback<Report>()
     {

      @Override
      public void onFailure(Throwable arg0)
      {
       arg0.printStackTrace();
       SC.say("Query error: " + arg0.getMessage());
      }

      @Override
      public void onSuccess(Report resLst)
      {
       resultCallback.showResult(resLst, (String) queryField.getValue(), searchSample, searchGroup, searchAttribNames, searchAttribValues,1);
      }
     });

  }

 }


 @Override
 public void linkClicked(String param)
 {
  int pNum=1;
  
  try
  {
   pNum = Integer.parseInt(param);
  }
  catch(Exception e)
  {
  }
  
  QueryService.Util.getInstance().selectSampleGroups(query, searchSample, searchGroup,
    searchAttribNames, searchAttribValues, (pNum-1)*ResultPane.MAX_GROUPS_PER_PAGE, ResultPane.MAX_GROUPS_PER_PAGE, new AsyncCallback<Report>()
    {

     @Override
     public void onFailure(Throwable arg0)
     {
      arg0.printStackTrace();
      SC.say("Query error: " + arg0.getMessage());
     }

     @Override
     public void onSuccess(Report resLst)
     {
      resultCallback.showResult(resLst, (String) queryField.getValue(), searchSample, searchGroup, searchAttribNames, searchAttribValues,1);
     }
    }); }

}
