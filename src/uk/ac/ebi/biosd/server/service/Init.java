package uk.ac.ebi.biosd.server.service;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.age.admin.server.mng.AgeAdmin;
import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.mng.AgeStorageManager;
import uk.ac.ebi.age.mng.AgeStorageManager.DB_TYPE;
import uk.ac.ebi.age.service.IdGenerator;
import uk.ac.ebi.age.service.impl.SeqIdGeneratorImpl;
import uk.ac.ebi.age.storage.AgeStorageAdm;
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
  BioSDConfigManager cfg = BioSDConfigManager.instance();
  AgeStorageAdm storage;
  
  try
  {
   IdGenerator.setInstance( new SeqIdGeneratorImpl(cfg.getServicesPath()+"/SeqIdGen") );
   storage = AgeStorageManager.createInstance( DB_TYPE.AgeDB, cfg.getDBPath() );
   
   BioSDService.setDefaultInstance( new BioSDServiceImpl( storage ) );
  }
  catch(StorageInstantiationException e)
  {
   e.printStackTrace();
   return;
  }
  
  Configuration conf = Configuration.getDefaultConfiguration();
  
  conf.setBaseDir( new File(cfg.getBasePath()) );
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
  BioSDService service = BioSDService.getInstance();

  
  if( service != null )
   service.shutdown();
  
  if( adm != null )
   adm.shutdown();

  IdGenerator.getInstance().shutdown();
 }


 
}
