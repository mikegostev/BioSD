package uk.ac.ebi.biosd.client.ui.test;

import uk.ac.ebi.age.ui.shared.imprint.AttributeImprint;
import uk.ac.ebi.age.ui.shared.imprint.ClassType;
import uk.ac.ebi.age.ui.shared.imprint.ObjectValue;
import uk.ac.ebi.age.ui.shared.imprint.Value;

import com.smartgwt.client.widgets.HTMLFlow;

public class ObjectViewPanelHTML extends HTMLFlow
{
 public ObjectViewPanelHTML(Value obj)
 {
  String html = "<table class='qualifierTable'>";

  for(AttributeImprint at : obj.getAttributes())
  {
   for(Value atv : at.getValues())
   {
    html += "<tr>";

    String rowSpan = "";

    if(at.getClassImprint().getType() != ClassType.ATTR_OBJECT)
    {
     if(((ObjectValue) atv).getObjectImprint() != null && ((ObjectValue) atv).getObjectImprint().getAttributes() != null)
      rowSpan = " rowspan='" + (((ObjectValue) atv).getObjectImprint().getAttributes().size()) + "'";
    }

    html += "<td class='qualifierName'" + rowSpan + ">" + at.getClassImprint().getName() + ":&nbsp</td>";

    String val = atv.getStringValue();

    if(at.getClassImprint().getType() != ClassType.ATTR_OBJECT)
     html += "<td colspan='2' style='qualifierValue'>" + val + "</td>";
    else
    {
     if(((ObjectValue) atv).getObjectImprint() == null || ((ObjectValue) atv).getObjectImprint().getAttributes() == null)
      html += "<td colspan='2' style='qualifierValue'>" + ((ObjectValue) atv).getTargetObjectId() + "</td>";

     boolean first = true;

     for(AttributeImprint obat : ((ObjectValue) atv).getObjectImprint().getAttributes())
     {
      for(Value objatv : obat.getValues())
      {

       if(first)
        first = false;
       else
        html += "<tr>";

       html += "<td class='qualifierObjAttrName'>" + obat.getClassImprint().getName() + ":&nbsp</td>";

       val = objatv.getStringValue();

       if(val.startsWith("http://"))
        val = "<a target='_blank' href='" + val + "'>" + val + "</a>";

       html += "<td class='qualifierValue'>" + val + "</td></tr>";
      }
     }
    }

    html += "</tr>";
   }
  }

  html += "</table>";

  setContents(html);
 }
}
