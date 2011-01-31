package uk.ac.ebi.biosd.client.query;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Attribute implements IsSerializable
{
 private String          name;
 private String          value;
 private List<Attribute> qualifiers;

 public Attribute()
 {
 }

 public Attribute(String nm, String v)
 {
  name=nm;
  value=v;
 }

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }

 public String getValue()
 {
  return value;
 }

 public void setValue(String value)
 {
  this.value = value;
 }

 public List<Attribute> getQualifiers()
 {
  return qualifiers;
 }

 public void setQualifiers(List<Attribute> qualifiers)
 {
  this.qualifiers = qualifiers;
 }

}
