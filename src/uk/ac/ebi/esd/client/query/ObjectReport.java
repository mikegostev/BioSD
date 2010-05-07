package uk.ac.ebi.esd.client.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ebi.esd.client.shared.AttributeReport;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ObjectReport implements IsSerializable
{
 private String id;
 private String descr;
 
 private Collection<String> matchedSamples = new ArrayList<String>(50);
 private Collection<String> allSamples = new ArrayList<String>(50);
 private List<AttributeReport> attr = new ArrayList<AttributeReport>();
 
 
 public void addMatchedSample(String obj)
 {
  matchedSamples.add( obj );
 }

 public void addSample( String obj )
 {
  allSamples.add( obj );
 }
 
 public void setId(String id)
 {
  this.id = id;
 }

 public void setDescription(String string)
 {
  descr = string;
 }

 public String getDescription()
 {
  return descr;
 }

 public String getId()
 {
  return id;
 }

 public Collection<String> getMatchedSamples()
 {
  return matchedSamples;
 }

 public Collection<String> getAllSamples()
 {
  return allSamples;
 }

 public void addAttribute(String nm, String val, boolean cust, int ord)
 {
  AttributeReport atr = new AttributeReport();
  
  atr.setName(nm);
  atr.setValue(val);
  atr.setOrder(ord);
  atr.setCustom(cust);
  
  attr.add(atr);
 }
 
 public List<AttributeReport> getAttributes()
 {
  return attr;
 }
}
