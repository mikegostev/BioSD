package uk.ac.ebi.biosd.client.query;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AttributedObject implements IsSerializable
{
 private String                    name;
 private String                    value;
 private AttributedObject          objectValue;
 /**
  * @gwt.typeArgs <uk.ac.ebi.biosd.client.query.AttributedObject>
  */

 private List<AttributedObject> qualifiers;

 public AttributedObject()
 {
 }

// public AttributedObject(String nm, Object v)
// {
//  name=nm;
//  value=v;
// }

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }

 public String getStringValue()
 {
  return value!= null?value:objectValue.toString();
 }

 public void setStringValue(String value)
 {
  this.value = value;
 }
 
 public AttributedObject getObjectValue()
 {
  return objectValue;
 }

 public void setObjectValue(AttributedObject value)
 {
  this.objectValue = value;
 }


 public List<AttributedObject> getAttributes()
 {
  return qualifiers;
 }

 public void setAttributes(List<AttributedObject> qualifiers)
 {
  this.qualifiers = qualifiers;
 }

 public String toString()
 {
  return name;
 }
}
