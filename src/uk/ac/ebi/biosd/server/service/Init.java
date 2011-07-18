package uk.ac.ebi.biosd.server.service;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.age.admin.server.mng.AgeAdmin;
import uk.ac.ebi.age.admin.server.mng.AgeAdminException;
import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.mng.AgeStorageManager;
import uk.ac.ebi.age.mng.AgeStorageManager.DB_TYPE;
import uk.ac.ebi.age.service.id.IdGenerator;
import uk.ac.ebi.age.service.id.impl.SeqIdGeneratorImpl;
import uk.ac.ebi.age.storage.AgeStorageAdm;

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
  BioSDConfigManager cfg = new BioSDConfigManager( arg0.getServletContext() );

  BioSDConfigManager.setInstance(cfg);
  
  AgeStorageAdm storage;
  
  try
  {
   boolean master = cfg.isMaster();
   
   if( master )
    IdGenerator.setInstance( new SeqIdGeneratorImpl(cfg.getServicesPath()+"/SeqIdGen") );
   
   storage = AgeStorageManager.createInstance( DB_TYPE.AgeDB, cfg.getAgeDBPath(), master );

  }
  catch(Exception e)
  {
   e.printStackTrace();
   return;
  }
  
  Configuration conf = Configuration.getDefaultConfiguration();
  
  conf.setBaseDir( new File(cfg.getBasePath()) );
  conf.setTmpDir( new File(cfg.getTmpPath()) );

  try
  {
   adm = new AgeAdmin(conf, storage);
  }
  catch(AgeAdminException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  
  BioSDServiceImpl biosd = new BioSDServiceImpl( storage );
  
  Configuration.getDefaultConfiguration().getAuthDB().addSecurityChangedListener(biosd);
  
  BioSDService.setDefaultInstance( biosd );

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

  if( BioSDConfigManager.instance().isMaster() )
   IdGenerator.getInstance().shutdown();
 }


 
}
