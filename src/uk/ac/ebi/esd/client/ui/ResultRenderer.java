package uk.ac.ebi.esd.client.ui;

import java.util.List;

import uk.ac.ebi.esd.client.query.ObjectReport;

public interface ResultRenderer
{
 void showResult( List<ObjectReport> res, String query, boolean sSmp, boolean sGrp, boolean sAtrNm, boolean sAtrVl );
}
