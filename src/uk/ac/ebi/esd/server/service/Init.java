package uk.ac.ebi.esd.server.service;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.age.admin.server.mng.AgeAdmin;
import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.storage.AgeStorageAdm;
import uk.ac.ebi.age.storage.AgeStorageManager;
import uk.ac.ebi.age.storage.AgeStorageManager.DB_TYPE;
import uk.ac.ebi.age.storage.exeption.StorageInstantiationException;

/**
 * Application Lifecycle Listener implementation class Init
 * 
 */
public class Init implements ServletContextListener
{
 private AgeAdmin adm;
 
 public Init()
 {
 }

 /**
  * @see ServletContextListener#contextInitialized(ServletContextEvent)
  */
 public void contextInitialized(ServletContextEvent arg0)
 {
  ESDConfigManager cfg = ESDConfigManager.instance();
  AgeStorageAdm storage;
  
  try
  {
   storage = AgeStorageManager.createInstance( DB_TYPE.AgeDB, cfg.getDBPath() );
   
   ESDService.setDefaultInstance( new ESDServiceImpl( storage ) );
  }
  catch(StorageInstantiationException e)
  {
   e.printStackTrace();
   return;
  }
  
  Configuration conf = Configuration.getDefaultConfiguration();
  
  conf.setTmpDir( new File(cfg.getTmpPath()) );

  adm = new AgeAdmin(conf, storage);
  
//  conf.setSessionPool(new SessionPoolImpl() );
//  conf.setUserDatabase( new TestUserDataBase() );
//  conf.setUploadManager( new UploadManager() );
//
//  conf.getUploadManager().addUploadCommandListener("SetModel", new SemanticUploader(storage));
//  conf.getUploadManager().addUploadCommandListener("Submission", new SubmissionUploader(storage));
 }

 /**
  * @see ServletContextListener#contextDestroyed(ServletContextEvent)
  */
 public void contextDestroyed(ServletContextEvent arg0)
 {
  ESDService service = ESDService.getInstance();

  
  if( service != null )
   service.shutdown();
  
  if( adm != null )
   adm.shutdown();

 }


 
}
