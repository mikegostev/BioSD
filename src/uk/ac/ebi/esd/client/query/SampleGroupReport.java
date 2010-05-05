package uk.ac.ebi.esd.client.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleGroupReport implements IsSerializable
{
 private String id;
 private String descr;
 
 private Collection<String> matchedSamples = new ArrayList<String>(50);
 private Collection<String> allSamples = new ArrayList<String>(50);
 private Map<String,String> attr = new TreeMap<String,String>();
 
 
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

 public void addAttribute(String nm, String val)
 {
  attr.put(nm,val);
 }
 
 public Map<String,String> getAttributes()
 {
  return attr;
 }
}
