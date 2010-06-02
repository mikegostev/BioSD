package uk.ac.ebi.esd.client.query;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Report implements IsSerializable
{
 private List<ObjectReport> objects;
 private int totalRecords;
 
 public List<ObjectReport> getObjects()
 {
  return objects;
 }
 
 public void setObjects(List<ObjectReport> objects)
 {
  this.objects = objects;
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
