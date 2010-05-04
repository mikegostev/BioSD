package uk.ac.ebi.esd.client.ui.module;

import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.SampleGroupReport;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryPanel extends VLayout
{
 private TextItem queryField;
 private ComboBoxItem where;
 private ComboBoxItem what;
 
 private AsyncCallback<List<SampleGroupReport>> resultCallback;
 
 public QueryPanel( AsyncCallback<List<SampleGroupReport>> cb )
 {
  resultCallback = cb;
  
//  setAlign(Alignment.CENTER);
  setPadding(8);
  
  DynamicForm f1 = new DynamicForm();
  
  f1.setColWidths("40%","1","1","1","*");
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
  where.setValue("samples");

  what = new ComboBoxItem();
  what.setTitle("Among");

  LinkedHashMap<String, String> st = new LinkedHashMap<String, String>();
  st.put("both", "attribute name & values");
  st.put("val", "attribute values");
  st.put("name", "attribute names");
  
  what.setValueMap(st);
  what.setValue("attribute values");

  SpacerItem sp = new SpacerItem();
  sp.setEndRow(true);
  
  f1.setFields(queryField, bt, sp, where, what);
  f1.setBorder("1px solid blue");
  
  addMember(f1);
 }
 
 
 private class QueryAction implements ClickHandler
 {

  public void onClick(ClickEvent event)
  {
   DeferredCommand.addCommand(new Command()
   {
    public void execute()
    {
     boolean searchSampleAttribNames=false;
     boolean searchSampleAttribValues=false;
     boolean searchGroupDescription=false;

     
     if( "val".equals(what.getValue()) )
      searchSampleAttribValues = true;
     else if( "name".equals(what.getValue()) )
      searchSampleAttribNames = true;
     else if( "both".equals(what.getValue()) )
     {
      searchSampleAttribValues = true;
      searchSampleAttribNames = true;
     }

     if( where.getValue().equals("sample") )
     {
      searchGroupDescription=false;
     }
     else if( where.getValue().equals("group") )
     {
      searchGroupDescription=true;
      searchSampleAttribValues = false;
      searchSampleAttribNames = false;
     }
     else if( where.getValue().equals("both") )
      searchGroupDescription=true;

     
     QueryService.Util.getInstance().selectSampleGroups((String) queryField.getValue(),
       searchGroupDescription, searchSampleAttribNames,searchSampleAttribValues,
       resultCallback  
     );

    }
   });
  }
 }

}
