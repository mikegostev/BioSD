package uk.ac.ebi.esd.server.service;

import java.util.List;

import uk.ac.ebi.esd.client.QueryService;
import uk.ac.ebi.esd.client.query.SampleGroupReport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GWTServlet extends RemoteServiceServlet implements QueryService
{

 private static final long serialVersionUID = -7453910333033735880L;

 @Override
 public List<SampleGroupReport> selectSampleGroups(String value, boolean searchGrp, boolean searchAttrNm,    boolean searchAttrVl)
 {
  return ESDService.getInstance().selectSampleGroups(value, searchGrp, searchAttrNm, searchAttrVl);
 }

}
