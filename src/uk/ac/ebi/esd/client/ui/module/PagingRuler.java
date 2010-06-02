package uk.ac.ebi.esd.client.ui.module;

import com.smartgwt.client.widgets.HTMLFlow;

public class PagingRuler extends HTMLFlow
{
 private String subj;
 
 public PagingRuler( String sbj)
 {
  setHeight(24);
  setShowEdges(true);
  setEdgeSize(1);
  setPadding(3);
  
  subj = sbj;
 }
 
 public void setPagination(int cpage, int total, int perpage)
 {
  StringBuilder sb = new StringBuilder();
  
  int pages = total/perpage+1;
  
  sb.append("<span class='groupPageRuler'>Page ").append(cpage).append(" of ").append(pages).append("<span class='pagerSpacer'></span>Pages: ");

  addPageLink(1, sb);
  
  int start=2;
  
  if( cpage > 2)
   start = cpage-1;
 
  int mid = start/2+1;

  if( mid > 1 && mid < start )
  {
   if( mid != 2 )
    sb.append("<span class='el'>..&nbsp;</span>");
   
   addPageLink(mid, sb);

   if( mid+1 != start )
    sb.append("<span class='el'>..&nbsp;</span>");
  }
  
  int i;
  
  for( i=start; i < start+3 && i <= pages; i++ )
   addPageLink(i,sb);
  
  mid = (pages+i)/2+1;
  
  
  if( mid > i && mid <= pages )
  {
   if( mid != i )
    sb.append("<span class='el'>..&nbsp;</span>");

   addPageLink(mid,sb);
  }
  
  if( mid > i && mid < pages )
  {
   if( mid != pages-1 )
    sb.append("<span class='el'>..&nbsp;</span>");
   
   addPageLink(pages,sb);
  }
  
  sb.append("</span>");
  setContents(sb.toString());
 
 }
 
 private void addPageLink( int i, StringBuilder sb )
 {
  sb.append("<a class='el' href='javascript:linkClicked(&quot;").append(subj).append("&quot;,&quot;").append(i).append("&quot;)'>").append(i).append("</a>&nbsp;");
 }
}
