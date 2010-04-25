package uk.ac.ebi.esd.server.service;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.admin.server.mng.SemanticUploader;
import uk.ac.ebi.age.admin.server.mng.UploadManager;
import uk.ac.ebi.age.admin.server.user.impl.SessionPoolImpl;
import uk.ac.ebi.age.admin.server.user.impl.TestUserDataBase;
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
 private AgeStorageAdm storage;
 private ESDServiceImpl service;
 
 public Init()
 {
 }

 /**
  * @see ServletContextListener#contextInitialized(ServletContextEvent)
  */
 public void contextInitialized(ServletContextEvent arg0)
 {
  ESDConfigManager cfg = ESDConfigManager.instance();
  
  try
  {
   storage = AgeStorageManager.createInstance( DB_TYPE.AgeDB, cfg.getDBPath() );
   
   service = new ESDServiceImpl(storage);
  }
  catch(StorageInstantiationException e)
  {
   e.printStackTrace();
   return;
  }
  
  Configuration conf = Configuration.getDefaultConfiguration();
  
  conf.setTmpDir( new File(cfg.getTmpPath()) );
  conf.setSessionPool(new SessionPoolImpl() );
  conf.setUserDatabase( new TestUserDataBase() );
  conf.setUploadManager( new UploadManager() );

  conf.getUploadManager().addUploadCommandListener("SetModel", new SemanticUploader(storage));
 }

 /**
  * @see ServletContextListener#contextDestroyed(ServletContextEvent)
  */
 public void contextDestroyed(ServletContextEvent arg0)
 {
  if( service != null )
   service.shutdown();

  if( storage != null )
   storage.shutdown();
  
  Configuration.getDefaultConfiguration().getSessionPool().shutdown();

 }


 
}
