package uk.ac.ebi.biosd.client.ui.test;

import uk.ac.ebi.biosd.client.query.AttributedObject;

import com.smartgwt.client.widgets.Window;

public class ObjectViewer extends Window
{
 public ObjectViewer( AttributedObject obj )
 {
//  setTitle("Sample "+rec.getAttributeAsString("__id") );  
  setWidth(750);  
  setHeight(600); 
//  setLeft(offsetLeft);  
  setCanDragReposition(true);  
  setCanDragResize(true);  
  setShowMinimizeButton(false);
  setIsModal(true);
  setShowModalMask(true);
  centerInPage();

  addItem( new ObjectViewPanel(obj) );
 }
}
