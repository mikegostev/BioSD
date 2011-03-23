import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;


public class ViewSubmission
{

 /**
  * @param args
  */
 public static void main(String[] args)
 {
  try
  {
   FileInputStream inp = new FileInputStream("m:/workspaceHL/eclipse/ESD/war/var/esd/agedb/submission/SBM1062.ser");
   
   ObjectInputStream oimp = new ObjectInputStream( inp );
   
   Object obj = oimp.readObject();
   
   System.out.println( obj.toString() );
  }
  catch(FileNotFoundException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  catch(IOException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  catch(ClassNotFoundException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }

 }

}
