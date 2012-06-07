package uk.ac.ebi.biosd.client.query;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.age.ui.shared.imprint.ClassImprint;
import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleList implements IsSerializable
{
 private List< ObjectImprint > samples = new ArrayList<ObjectImprint>(25);
 private List<ClassImprint> header = new ArrayList<ClassImprint>();
 private int totalRecords;

 public SampleList()
 {}
 
 public List< ObjectImprint > getSamples()
 {
  return samples;
 }

 public List<ClassImprint> getHeaders()
 {
  return header;
 }

 public void addSample(ObjectImprint smp)
 {
  samples.add(smp);
 }

 public void addHeader(ClassImprint cls)
 {
  header.add(cls);
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
