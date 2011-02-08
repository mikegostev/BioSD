package uk.ac.ebi.biosd.client.ui.test;

import uk.ac.ebi.biosd.client.query.AttributedObject;

import com.smartgwt.client.widgets.HTMLFlow;

public class ObjectViewPanelHTML extends HTMLFlow
{
 public ObjectViewPanelHTML( AttributedObject obj )
 {
  String html="<table border=1 width=100% height=100%>";
  
  
  for( AttributedObject at : obj.getAttributes() )
  {
   html += "<tr>";
   
   
   html += "<td>"+at.getName()+":&nbsp</td>";
   html += "<td>"+at.getStringValue()+"</td>";
   
   html += "</tr>";
   
   if( at.getObjectValue() != null )
   {
    html += "<tr><td colspan='2'><table>";
    
    for( AttributedObject obat : at.getObjectValue().getAttributes() )
    {
     html += "<td>"+obat.getName()+":&nbsp</td>";
     html += "<td>"+obat.getStringValue()+"</td>";
    }
    
    html += "</table></td></tr>";
   }
  }
  
  setContents(html);
 }
}
