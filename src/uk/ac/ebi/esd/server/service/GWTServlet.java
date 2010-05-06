package uk.ac.ebi.esd.server.service;

import java.util.List;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.ObjectReport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GWTServlet extends RemoteServiceServlet implements QueryService
{

 private static final long serialVersionUID = -7453910333033735880L;

 @Override
 public List<ObjectReport> selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm,    boolean searchAttrVl)
 {
  return ESDService.getInstance().selectSampleGroups(value, searchSmp, searchGrp, searchAttrNm, searchAttrVl);
 }

 @Override
 public List<ObjectReport> getSamplesByGroup(String grpID)
 {
  return ESDService.getInstance().getSamplesByGroup(grpID);
 }

}
