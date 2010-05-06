package uk.ac.ebi.esd.client.ui.module;

import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.ObjectReport;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryPanel extends VLayout
{
 private TextItem queryField;
 private ComboBoxItem where;
 private ComboBoxItem what;
 
 private AsyncCallback<List<ObjectReport>> resultCallback;
 
 public QueryPanel( AsyncCallback<List<ObjectReport>> cb )
 {
  resultCallback = cb;
  
//  setAlign(Alignment.CENTER);
  setPadding(3);
  
  HLayout hstrip = new HLayout();
  hstrip.setWidth100();
  hstrip.setHeight(125);
  
  Canvas left = new Canvas();
  left.setWidth(456);
  left.setHeight100();
  left.setStyleName("leftBanner");
  
  
  Canvas right = new Canvas();
  right.setWidth(229);
  left.setHeight100();
  right.setStyleName("rightBanner");
  
  DynamicForm f1 = new DynamicForm();
  f1.setStyleName("reqForm");
  
  f1.setColWidths("200","1","1","1","*");
  f1.setNumCols(5);

  
//  f1.setCellBorder(1);
  
  queryField = new TextItem("refreshPicker", "Query");
  queryField.setColSpan(3);
  queryField.setWidth(400);
  
  ButtonItem bt = new ButtonItem("Button","Search");
  bt.setStartRow(false);
  bt.addClickHandler( new QueryAction() );

 
  where = new ComboBoxItem();
  where.setTitle("Search within");
  
  LinkedHashMap<String, String> vm = new LinkedHashMap<String, String>();
  vm.put("both", "samples & groups");
  vm.put("sample", "samples");
  vm.put("group", "groups");
  
  where.setValueMap(vm);
  where.setDefaultToFirstOption(true);

  what = new ComboBoxItem();
  what.setTitle("Among");

  LinkedHashMap<String, String> st = new LinkedHashMap<String, String>();
  st.put("both", "attribute name & values");
  st.put("val", "attribute values");
  st.put("name", "attribute names");
  
  what.setValueMap(st);
  what.setDefaultToFirstOption(true);
//  what.setValue("attribute name & values");

  SpacerItem sp = new SpacerItem();
  sp.setEndRow(true);
  
  f1.setFields(queryField, bt, sp, where, what);
//  f1.setBorder("1px solid blue");
  
  hstrip.addMember(left);
  hstrip.addMember(f1);
  hstrip.addMember(right);
  
  addMember(hstrip);
 }
 
 
 private class QueryAction implements ClickHandler
 {

  public void onClick(ClickEvent event)
  {
   DeferredCommand.addCommand(new Command()
   {
    public void execute()
    {
     boolean searchAttribNames=false;
     boolean searchAttribValues=false;
     boolean searchGroup=false;
     boolean searchSample=false;

     
     if( "val".equals(what.getValue()) )
      searchAttribValues = true;
     else if( "name".equals(what.getValue()) )
      searchAttribNames = true;
     else if( "both".equals(what.getValue()) )
     {
      searchAttribValues = true;
      searchAttribNames = true;
     }

     if( where.getValue().equals("sample") )
     {
      searchSample=true;
      searchGroup=false;
     }
     else if( where.getValue().equals("group") )
     {
      searchSample=false;
      searchGroup=true;
     }
     else if( where.getValue().equals("both") )
     {
      searchSample=true;
      searchGroup=true;
     }
     
     QueryService.Util.getInstance().selectSampleGroups((String) queryField.getValue(),
       searchSample,searchGroup, searchAttribNames,searchAttribValues,
       resultCallback  
     );

    }
   });
  }
 }

}
