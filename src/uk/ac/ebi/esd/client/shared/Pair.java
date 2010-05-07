package uk.ac.ebi.esd.client.shared;

public class Pair<T1,T2>
{
 private T1 first;
 private T2 second;
 
 public Pair(T1 f, T2 s)
 {
  first = f;
  second = s;
 }

 public T1 getFirst()
 {
  return first;
 }

 public void setFirst(T1 first)
 {
  this.first = first;
 }

 public T2 getSecond()
 {
  return second;
 }

 public void setSecond(T2 second)
 {
  this.second = second;
 }
 
}
