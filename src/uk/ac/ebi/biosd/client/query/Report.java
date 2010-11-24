package uk.ac.ebi.biosd.client.query;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Report implements IsSerializable
{
 private List<GroupImprint> objects;
 private int totalRecords;
 
 public List<GroupImprint> getObjects()
 {
  return objects;
 }
 
 public void setObjects(List<GroupImprint> objects)
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
