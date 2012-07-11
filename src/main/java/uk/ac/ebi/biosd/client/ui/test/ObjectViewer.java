package uk.ac.ebi.biosd.client.ui.test;

import uk.ac.ebi.age.ui.shared.imprint.AttributeImprint;
import uk.ac.ebi.age.ui.shared.imprint.ClassType;
import uk.ac.ebi.age.ui.shared.imprint.ObjectValue;
import uk.ac.ebi.age.ui.shared.imprint.Value;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Window;

public class ObjectViewer extends Window
{
 static final int MAX_HEIGHT = 700;
 
 public ObjectViewer( Value obj )
 {
  setTitle( "Value properties: "+obj.getStringValue() );  
  setWidth(750);  
 
  int height = 0;
  
  final int rowHeight = 22;
  
  for( AttributeImprint at : obj.getAttributes() )
  {
   if( at.getClassImprint().getType() != ClassType.ATTR_OBJECT )
    height+=rowHeight*at.getValueCount();
   else
   {
    for( Value v : at.getValues() )
    {
     if( ((ObjectValue)v).getObjectImprint() == null || ((ObjectValue)v).getObjectImprint().getAttributes() == null )
      height += rowHeight;
     else
     {
      for( AttributeImprint oat : ((ObjectValue)v).getObjectImprint().getAttributes() )
       height += rowHeight * oat.getValueCount();
     }
    }
    
   }
  }
  
  if( height > MAX_HEIGHT )
   height = MAX_HEIGHT;
  
  setHeight(height+25); 
  setCanDragReposition(true);  
  setCanDragResize(true);  
  setShowMinimizeButton(false);
  setIsModal(true);
  setShowModalMask(true);
  centerInPage();
  
  setAlign(VerticalAlignment.CENTER);

  addItem( new ObjectViewPanelHTML(obj) );
 }  
}
