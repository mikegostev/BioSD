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
  
  int pos = 2;
  
  int start=cpage-1;
  
//  if( cpage > 2)
//   start = cpage-1;
 
  int mid = start/2+1;

  if( mid > 1 && mid < start )
  {
   if( mid > 2 )
    sb.append("<span class='el'>..&nbsp;</span>");
   
   addPageLink(mid, sb);

   if( mid < start-1 )
    sb.append("<span class='el'>..&nbsp;</span>");
  }
  
  int i = start < pos? pos:start;
  
  for( ; i <= start+2 && i < pages; i++ )
   addPageLink(i,sb);
  
  pos = i;
  
  mid = (pages+pos)/2+1;
  
  
  if( mid > pos && mid < pages )
  {
   if( mid > i+1 )
    sb.append("<span class='el'>..&nbsp;</span>");

   addPageLink(mid,sb);
   
   pos=mid+1;
  }
  
  if( pos < pages )
   sb.append("<span class='el'>..&nbsp;</span>");
  
  addPageLink(pages,sb);

  
//  if( mid > i && mid < pages )
//  {
//   if( mid < pages-1 )
//    sb.append("<span class='el'>..&nbsp;</span>");
//   
//   addPageLink(pages,sb);
//  }
  
  sb.append("</span>");
  setContents(sb.toString());
 
 }
 
 private void addPageLink( int i, StringBuilder sb )
 {
  sb.append("<a class='el' href='javascript:linkClicked(&quot;").append(subj).append("&quot;,&quot;").append(i).append("&quot;)'>").append(i).append("</a>&nbsp;");
 }
}
