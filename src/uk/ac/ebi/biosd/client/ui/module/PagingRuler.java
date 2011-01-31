package uk.ac.ebi.biosd.client.ui.module;

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
 
 public void setContents(int cpage, int total, int perpage, String pfx, String sfx)
 {
  StringBuilder sb = new StringBuilder();
  
  int pages = total/perpage+1;
  
  sb.append("<span class='pageRuler'>");
  
  if( pfx != null )
   sb.append("<span class='pageRulerPrefix'>").append(pfx).append("</span>");
  
  sb.append("Page ").append(cpage).append(" of ").append(pages);

  if( pages > 1 )
  {
   sb.append("<span class='pagerSpacer'></span>Pages: ");

   addPageLink(1, cpage, sb);

   int pos = 2;

   int start = cpage - 1;

   // if( cpage > 2)
   // start = cpage-1;

   int mid = start / 2 + 1;

   if(mid > 1 && mid < start)
   {
    if(mid == 3)
     addPageLink(2, cpage, sb);
    else if(mid > 2)
     sb.append("<span class='el'>..&nbsp;</span>");

    // else
    // addPageLink(2,cpage, sb);

    addPageLink(mid, cpage, sb);

    if(mid == start - 2)
     addPageLink(mid + 1, cpage, sb);
    else if(mid < start - 1)
     sb.append("<span class='el'>..&nbsp;</span>");

    // sb.append("<span class='el'>..&nbsp;</span>");
   }

   int i = start < pos ? pos : start;

   for(; i <= start + 2 && i < pages; i++)
    addPageLink(i, cpage, sb);

   pos = i - 1;

   mid = (pages + pos) / 2 + 1;

   if(mid > pos && mid < pages)
   {
    if(mid == pos + 2)
     addPageLink(mid - 1, cpage, sb);
    else if(mid > pos + 2)
     sb.append("<span class='el'>..&nbsp;</span>");

    addPageLink(mid, cpage, sb);

    pos = mid + 1;
   }
   else
    pos++;

   if(pos == pages - 1)
    addPageLink(pos, cpage, sb);
   else if(pos < pages)
    sb.append("<span class='el'>..&nbsp;</span>");

   addPageLink(pages, cpage, sb);

   // if( mid > i && mid < pages )
   // {
   // if( mid < pages-1 )
   // sb.append("<span class='el'>..&nbsp;</span>");
   //
   // addPageLink(pages,sb);
   // }

  }
  
  if( sfx != null )
   sb.append("<span class='pageRulerSuffix'>").append(sfx).append("</span>");

  sb.append("</span>");

  setContents(sb.toString());
 
 }
 
 private void addPageLink( int i, int cpage, StringBuilder sb )
 {
  if( i == cpage )
   sb.append("<span class='currentPageLink'>");
  
  sb.append("<a class='el' href='javascript:linkClicked(&quot;").append(subj).append("&quot;,&quot;").append(i).append("&quot;)'>").append(i).append("</a>&nbsp;");

  if( i == cpage )
   sb.append("</span>");
 }
}
