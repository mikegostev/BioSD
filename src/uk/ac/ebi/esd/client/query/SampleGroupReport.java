package uk.ac.ebi.esd.client.query;

import java.util.ArrayList;
import java.util.Collection;

public class SampleGroupReport
{
 private String id;
 private String descr;
 
 Collection<String> matchedSamples = new ArrayList<String>(50);
 Collection<String> allSamples = new ArrayList<String>(50);
 
 
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

}
