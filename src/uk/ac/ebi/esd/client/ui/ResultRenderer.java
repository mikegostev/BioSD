package uk.ac.ebi.esd.client.ui;

import uk.ac.ebi.esd.client.query.Report;

public interface ResultRenderer
{
 void showResult( Report rep, String query, boolean sSmp, boolean sGrp, boolean sAtrNm, boolean sAtrVl, int cpage );
}
