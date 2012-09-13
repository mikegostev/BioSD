package uk.ac.ebi.biosd.server.service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uk.ac.ebi.age.admin.server.mng.AgeAdmin;
import uk.ac.ebi.age.admin.server.mng.AgeAdminConfigManager;
import uk.ac.ebi.age.admin.server.mng.AgeAdminException;
import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.model.IdScope;
import uk.ac.ebi.age.parser.SyntaxProfile;
import uk.ac.ebi.age.parser.impl.ClassSpecificSyntaxProfileDefinitionImpl;
import uk.ac.ebi.age.parser.impl.SyntaxProfileDefinitionImpl;
import uk.ac.ebi.age.service.id.IdGenerator;
import uk.ac.ebi.age.service.id.impl.SeqIdGeneratorImpl;
import uk.ac.ebi.age.storage.AgeStorageAdm;
import uk.ac.ebi.age.storage.impl.ser.SerializedStorage;
import uk.ac.ebi.age.storage.impl.ser.SerializedStorageConfiguration;
import uk.ac.ebi.biosd.shared.Constants;
import uk.ac.ebi.mg.assertlog.Log;
import uk.ac.ebi.mg.assertlog.LogFactory;
import uk.ac.ebi.mg.executor.DefaultExecutorService;

import com.pri.util.StringUtils;

/**
 * Application Lifecycle Listener implementation class Init
 * 
 */
public class Init implements ServletContextListener
{
 private static Log log = LogFactory.getLog(Init.class);
 
 private AgeAdmin adm;
 
 public Init()
 {
 }

 /**
  * @see ServletContextListener#contextInitialized(ServletContextEvent)
  */
 @Override
 public void contextInitialized(ServletContextEvent arg0)
 {
  long startTime=0;
  
  assert ( startTime = System.currentTimeMillis() ) != 0;

  DefaultExecutorService.init();
  
  BioSDConfigManager cfg = new BioSDConfigManager( arg0.getServletContext() );

  BioSDConfigManager.setInstance(cfg);
  
  assert log.info("BioSD base path: "+cfg.getBasePath());

  AgeStorageAdm storage;
  
  try
  {
   IdGenerator.setInstance( new SeqIdGeneratorImpl(cfg.getIDGenPath()) );
   
   SerializedStorageConfiguration serConf = new SerializedStorageConfiguration();
   
   serConf.setStorageBaseDir( new File( cfg.getAgeDBPath() ) );
   serConf.setMaintenanceModeTimeout(cfg.getMaintenanceModeTimeout());
   serConf.setAutoMModeTimeout(cfg.getAutoMModeTimeout());
   serConf.setMaster(cfg.isMaster());
   serConf.setIndexDir(cfg.getIndexDir());
   
   storage = new SerializedStorage(serConf);
  }
  catch(Exception e)
  {
   e.printStackTrace();
   return;
  }
  
  Configuration conf = Configuration.getDefaultConfiguration();
  
  conf.setBaseDir( new File(cfg.getBasePath()) );
  conf.setTmpDir( new File(cfg.getTmpPath()) );

  
  SyntaxProfile sp = new SyntaxProfile();
  SyntaxProfileDefinitionImpl commSP = new SyntaxProfileDefinitionImpl();
  commSP.setClusterIdPrefix("$C:");
  commSP.setModuleIdPrefix("$M:");
  commSP.setGlobalIdPrefix("$G:");
  commSP.setDefaultIdScope(IdScope.GLOBAL);
  commSP.setPrototypeObjectId("<<ALL>>");
  commSP.setResetPrototype(true);
 
  ClassSpecificSyntaxProfileDefinitionImpl vertClasses = new ClassSpecificSyntaxProfileDefinitionImpl(commSP);
  vertClasses.setHorizontalBlockDefault(false);

  ClassSpecificSyntaxProfileDefinitionImpl vertModClasses = new ClassSpecificSyntaxProfileDefinitionImpl(commSP);
  vertModClasses.setHorizontalBlockDefault(false);
  vertModClasses.setDefaultIdScope(IdScope.MODULE);
  
  sp.setCommonSyntaxProfile(commSP);
  
  sp.addClassSpecificSyntaxProfile("Submission", vertClasses);
  sp.addClassSpecificSyntaxProfile("Group", vertClasses);

  sp.addClassSpecificSyntaxProfile("Term Source", vertModClasses);
  sp.addClassSpecificSyntaxProfile("Person", vertModClasses);
  sp.addClassSpecificSyntaxProfile("Publication", vertModClasses);
  sp.addClassSpecificSyntaxProfile("Organization", vertModClasses);
  sp.addClassSpecificSyntaxProfile("Database", vertModClasses);
  
  conf.setSyntaxProfile(sp);
  
  String prm = cfg.getConfigParameter(AgeAdminConfigManager.STARTUP_ONLINE_MODE_PARAM);
  
  conf.setOnlineMode( prm == null || ( ! "off".equalsIgnoreCase(prm) && ! "false".equalsIgnoreCase(prm) ));
  
  prm = cfg.getConfigParameter(AgeAdminConfigManager.INSTANCE_NAME_PARAM);
  
  if( prm == null )
  {
   try
   {
    prm = InetAddress.getLocalHost().getHostName();
   }
   catch(UnknownHostException e)
   {
    prm = "INST"+System.currentTimeMillis();
   }
  }
  
  conf.setInstanceName(prm);
  
  
  try
  {
   adm = new AgeAdmin(conf, storage);
  }
  catch(AgeAdminException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
   return;
  }
  
  BioSDServiceImpl biosd=null;
  try
  {
   biosd = new BioSDServiceImpl( storage );
  }
  catch(BioSDInitException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
   return;
  }
  
  conf.getRemoteRequestManager().addRemoteRequestListener(Constants.BIOSD_TAG_CONTROL_COMMAND, new BioSDTagController(adm));
  
  Configuration.getDefaultConfiguration().getAuthDB().addSecurityChangedListener(biosd);
  
  BioSDService.setDefaultInstance( biosd );

  assert log.info("System startup time: "+StringUtils.millisToString(System.currentTimeMillis()-startTime) );

 }

 /**
  * @see ServletContextListener#contextDestroyed(ServletContextEvent)
  */
 @Override
 public void contextDestroyed(ServletContextEvent arg0)
 {
  BioSDService service = BioSDService.getInstance();

  
  if( service != null )
   service.shutdown();
  
  if( adm != null )
   adm.shutdown();

  if( BioSDConfigManager.instance().isMaster() )
   IdGenerator.getInstance().shutdown();
  
  DefaultExecutorService.shutdown();

 }


 
}
