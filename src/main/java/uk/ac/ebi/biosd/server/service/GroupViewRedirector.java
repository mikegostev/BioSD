package uk.ac.ebi.biosd.server.service;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ebi.age.admin.server.mng.Configuration;
import uk.ac.ebi.age.admin.server.service.ServiceServlet;
import uk.ac.ebi.age.authz.ACR.Permit;
import uk.ac.ebi.age.authz.PermissionManager;
import uk.ac.ebi.age.authz.Session;
import uk.ac.ebi.age.ext.authz.SystemAction;
import uk.ac.ebi.age.model.AgeObject;
import uk.ac.ebi.age.ui.server.imprint.ImprintBuilder;
import uk.ac.ebi.age.ui.server.imprint.ImprintingHint;
import uk.ac.ebi.age.ui.shared.render.ImprintJSONRenderer;
import uk.ac.ebi.biosd.client.shared.MaintenanceModeException;

public class GroupViewRedirector extends ServiceServlet
{
 
 private static final ImprintingHint hint;

 static
 {
  hint = new ImprintingHint();
  hint.setConvertRelations(true);
  hint.setConvertAttributes(true);
  hint.setQualifiersDepth(2);
  hint.setResolveObjectAttributesTarget(true);
  hint.setResolveRelationsTarget(false);
  hint.setConvertImplicitRelations(false);
 }
 
 private static final long serialVersionUID = 1L;

 @Override
 protected void service(HttpServletRequest req, HttpServletResponse resp, Session sess) throws ServletException, IOException
 {
  BioSDService biosd = BioSDService.getInstance();
  
  if( biosd == null )
  {
   resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
   return;
  }
  
  String groupId = null;
  
  if( req.getPathInfo() != null)
   groupId=req.getPathInfo().substring(1);
  
  AgeObject group = null;
  
  if( groupId != null )
  {
   try
   {
    group = biosd.getGroup(groupId);
   }
   catch(MaintenanceModeException e)
   {
   }
  }
  
  if( group == null )
  {
   System.out.println("Group '"+groupId+"' not found");
   return;
  }
  
  PermissionManager pm = Configuration.getDefaultConfiguration().getPermissionManager();
  
  
  
  if( pm.checkPermission(SystemAction.READ, group) != Permit.ALLOW )
  {
   System.out.println("Not Allowed");
   return;
  }
   
  
  String fmt = req.getParameter(BioSDConfigManager.FORMAT_PARAM);
  
  if( fmt != null )
  {
   if("xml".equalsIgnoreCase(fmt))
   {
    resp.setContentType("text/xml");

    biosd.exportGroup(group, resp.getWriter());

    return;
   }
   else if( "json".equalsIgnoreCase(fmt) )
   {
    resp.setContentType("application/json");
    
    ImprintJSONRenderer.render(ImprintBuilder.convert(Collections.singletonList( group ),hint,null,null,null,null),resp.getWriter());
    
    return;
   }
  }
  
  
  RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/ObjectViewer.jsp?");
  req.setAttribute("Object",group);
  dispatcher.forward(req,resp);

 }
}