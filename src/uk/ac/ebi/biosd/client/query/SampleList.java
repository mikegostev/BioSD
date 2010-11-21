package uk.ac.ebi.biosd.client.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.biosd.client.shared.AttributeClassReport;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleList implements IsSerializable
{
 private List< Map<String, String> > samples = new ArrayList<Map<String,String>>(25);
 private List<AttributeClassReport> header;
 private int totalRecords;

 
 public List<Map<String, String>> getSamples()
 {
  return samples;
 }

 public List<AttributeClassReport> getHeader()
 {
  return header;
 }

 public void addSample(Map<String, String> attrMap)
 {
  samples.add(attrMap);
 }

 public void setHeader(List<AttributeClassReport> clsLst)
 {
  header=clsLst;
 }

 public int getTotalRecords()
 {
  return totalRecords;
 }

 public void setTotalRecords(int totalRecords)
 {
  this.totalRecords = totalRecords;
 }

}
