package uk.ac.ebi.esd.client.ui;

import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class SampleListGrid extends ListGrid
{
 private static final String AELinkFieldName = "temp";
 
 public SampleListGrid()
 {
  setShowRecordComponents(true);          
  setShowRecordComponentsByCell(true);  
 }
 
 @Override
 protected Canvas createRecordComponent(final ListGridRecord record, final Integer colNum) 
 {
  String title = getField(colNum).getTitle();
  
  if(title.equals(AELinkFieldName))
  {
   ImgButton aeImg = new ImgButton();
   aeImg.setShowDown(false);
   aeImg.setShowRollOver(false);
   aeImg.setAlign(Alignment.CENTER);
   aeImg.setSrc("ae.png");
   aeImg.setPrompt("View Chart");
   aeImg.setHeight(16);
   aeImg.setWidth(16);
   aeImg.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
   {

    @Override
    public void onClick(com.smartgwt.client.widgets.events.ClickEvent event)
    {
     Window.open(record.getAttribute(getFieldName(colNum)), "_blank", "");
//     SC.say("Chart Icon Clicked for country : " + record.getAttribute(getFieldName(colNum)));
    }
   });
   
   return aeImg;
  }

  return null;
 }
}
