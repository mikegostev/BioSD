package uk.ac.ebi.esd.client.ui.module;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.Report;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
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

//  Label banPan = new Label("<div class=\"headerdiv\" id=\"headerdiv\" style=\"z-index: 1;\">"
//        +"<iframe src=\"/inc/head.html\" name=\"head\" id=\"head\" frameborder=\"0\" marginwidth=\"0px\" marginheight=\"0px\" scrolling=\"no\"  width=\"100%\" style=\"z-index: 1; height: 57px;\">Your browser does not support inline frames or is currently configured not to display inline frames. Content can be viewed at actual source page: http://www.ebi.ac.uk/inc/head.html</iframe>"
//    +"</div>");

//  Label banPan = new Label("<div class=\"headerdiv\" id=\"headerdiv\"\r\n" + 
//  		"    style=\"position: absolute; z-index: 1;\"><iframe\r\n" + 
//  		"    src=\"/inc/head.html\" name=\"head\" id=\"head\" frameborder=\"0\"\r\n" + 
//  		"    marginwidth=\"0px\" marginheight=\"0px\" scrolling=\"no\" width=\"100%\"\r\n" + 
//  		"    style=\"position: absolute; z-index: 1; height: 57px;\"></iframe></div>");
  
  Label banPan = new Label();
 
  banPan.setWidth100();
  banPan.setHeight(57);
  
  addMember(banPan);
  addMember(qp);
  addMember(resultPane);

  Label footPan = new Label("<iframe src=\"/inc/foot.html\" name=\"foot\"\r\n" + 
  		"    frameborder=\"0\" marginwidth=\"0px\" marginheight=\"0px\" scrolling=\"no\"\r\n" + 
  		"    height=\"22px\" width=\"100%\" style=\"z-index: 2\"></iframe>");
  
  footPan.setWidth100();
  footPan.setHeight(22);

  addMember( footPan );
  
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
