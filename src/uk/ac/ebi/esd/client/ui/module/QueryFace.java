package uk.ac.ebi.esd.client.ui.module;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.Report;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;

public class QueryFace extends VLayout
{
 private ResultPane resultPane;
 
 public QueryFace()
 {
  setWidth100();
  setHeight100();

  resultPane = new ResultPane();

  Canvas qp = new QueryPanel(resultPane);

  // qp.setHeight(200);
  qp.setWidth("100%");

  resultPane.setWidth100();
  resultPane.setHeight100();

  addMember(qp);
  addMember(resultPane);

  DeferredCommand.addCommand(new Command()
  {
   @Override
   public void execute()
   {
    QueryService.Util.getInstance().getAllGroups(0, ResultPane.MAX_GROUPS_PER_PAGE, new AsyncCallback<Report>()
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
      resultPane.showResult(resLst, null, false, false, false, false,1);
     }
    });
   }
  });

 }
}
