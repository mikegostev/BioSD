package uk.ac.ebi.biosd.server.service;

import uk.ac.ebi.biosd.client.QueryService;
import uk.ac.ebi.biosd.client.query.Report;
import uk.ac.ebi.biosd.client.query.SampleList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GWTServlet extends RemoteServiceServlet implements QueryService
{
 private static final long serialVersionUID = -7453910333033735880L;

 @Override
 public Report selectSampleGroups(String value, boolean searchSmp, boolean searchGrp, boolean searchAttrNm,    boolean searchAttrVl, int offs, int cnt)
 {
  return BioSDService.getInstance().selectSampleGroups(value, searchSmp, searchGrp, searchAttrNm, searchAttrVl, offs,  cnt);
 }

 @Override
 public SampleList getSamplesByGroup(String grpID, int offs, int cnt)
 {
  return BioSDService.getInstance().getSamplesByGroup(grpID, offs, cnt);
 }

 @Override
 public SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAtNames, boolean searchAtValues, int offs, int cnt)
 {
  return BioSDService.getInstance().getSamplesByGroupAndQuery(grpId, query, searchAtNames, searchAtValues, offs, cnt);
 }

 @Override
 public Report getAllGroups(int offs, int count)
 {
  return BioSDService.getInstance().getAllGroups(offs, count);
 }

}
