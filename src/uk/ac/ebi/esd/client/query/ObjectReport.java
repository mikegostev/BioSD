package uk.ac.ebi.esd.client.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ebi.esd.client.shared.Pair;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ObjectReport implements IsSerializable
{
 private String id;
 private String descr;
 
 private Collection<String> matchedSamples = new ArrayList<String>(50);
 private Collection<String> allSamples = new ArrayList<String>(50);
 private List<Pair<String,String>> attr = new ArrayList<Pair<String,String>>();
 
 
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
  attr.add(new Pair<String, String>(nm, val));
 }
 
 public List<Pair<String, String>> getAttributes()
 {
  return attr;
 }
}
