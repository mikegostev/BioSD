<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="java.io.IOException"%>
<%@page import="javax.servlet.jsp.JspWriter"%>
<%@page import="uk.ac.ebi.age.model.AgeObjectAttribute"%>
<%@page import="uk.ac.ebi.age.model.Attributed"%>
<html lang="en">

<%@page import="java.util.Iterator"%>
<%@page import="com.pri.util.StringUtils"%>
<%@page import="uk.ac.ebi.age.model.AgeAttribute"%>
<%@page import="uk.ac.ebi.age.model.AgeAttributeClass"%>
<%@page import="java.util.Collection"%>
<%@page import="uk.ac.ebi.age.model.AgeObject"%>
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
 AgeObject obj = (AgeObject) request.getAttribute("Object");
%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="en-GB">
<meta http-equiv="Window-target" content="_top">
<meta name="no-email-collection"
	content="http://www.unspam.com/noemailcollection/">
<title> Sample <%=obj.getId()%></title>
<link rel="SHORTCUT ICON" href="http://www.ebi.ac.uk/bookmark.ico">
<link rel="stylesheet" href="../sview_files/contents.css" type="text/css">
<link rel="stylesheet" href="../sview_files/userstyles.css" type="text/css">
<script src="../sview_files/ga.js" async="" type="text/javascript"></script>
<script src="../sview_files/contents.js" type="text/javascript"></script>
<link rel="alternate" title="EBI News RSS"
	href="http://www.ebi.ac.uk/Information/News/rss/ebinews.xml"
	type="application/rss+xml">
<style type="text/css">
@media print {
	body,.contents,.header,.contentsarea,.head {
		position: relative;
	}
}
</style>
<link rel="stylesheet" href="../sview_files/ae_common_20.css"
	type="text/css">
<link rel="stylesheet" href="../sview_files/ae_html_page_20.css"
	type="text/css">
<link rel="stylesheet" href="../sview_files/ae_idf_view_20.css"
	type="text/css">
<script src="../sview_files/jquery-1.js" type="text/javascript"></script>
<script src="../sview_files/jquery.js" type="text/javascript"></script>
<script src="../sview_files/ae_common_20.js" type="text/javascript"></script>
<script src="../sview_files/ae_idf_view_20.js" type="text/javascript"></script>
<script type="text/javascript">
	var contextPath = "/arrayexpress";
</script>

</head>
<body class="">
	<div class="headerdiv" id="headerdiv"
		style="position: absolute; z-index: 1;">
		<iframe src="/inc/head.htm" name="head" id="head"
			marginwidth="0px" marginheight="0px"
			style="position: absolute; z-index: 1; height: 57px;" frameborder="0"
			scrolling="no" width="100%">Your browser does not support
			inline frames or is currently configured not to display inline
			frames. Content can be viewed at actual source page:
			http://www.ebi.ac.uk/inc/head.html</iframe>
	</div>

	<div id="ae_contents" class="ae_contents_frame ae_assign_font">
		<div id="ae_contents_container">
			<div id="ae_contents_box_100pc">
				<div id="ae_content">
					<div id="ae_navi">
						<a href="http://www.ebi.ac.uk/">EBI</a> &gt; <a
							href="http://www.ebi.ac.uk/biosamples">biosamples</a> &gt; Sample <%=obj.getId()%>
					</div>
					<div id="ae_summary_box">
						<div id="ae_accession">
						</div>
						<div id="ae_title">
						</div>
					</div>
					<div id="ae_results_box">
						<table id="ae_results_table" border="0" cellpadding="0"
							cellspacing="0">
							<tbody>
<%!
private void printAttbt( Attributed atObj, JspWriter out ) throws IOException
{
 Iterator clsIt = atObj.getAttributeClasses().iterator();

 while( clsIt.hasNext() )
 {
  AgeAttributeClass cls = (AgeAttributeClass) clsIt.next();
  
  Iterator prmIt = atObj.getAttributesByClass(cls, false).iterator();
  
  while( prmIt.hasNext() )
  {
   AgeAttribute attr = (AgeAttribute) prmIt.next();
   
   if( attr instanceof AgeObjectAttribute )
    printAttbt( ((AgeObjectAttribute)attr).getValue(), out );
   else
   {
     String val = attr.getValue().toString();

     if(val.length() > 8 && "http://".equalsIgnoreCase(val.substring(0, 7)))
      val = "<a target=\"_blank\" href=\"" + val + "\">" + StringUtils.htmlEscaped(val) + "</a>";
     else
      val = StringUtils.htmlEscaped(val);

     out.print("<tr><td class=\"col_1\">" + StringUtils.htmlEscaped(cls.getName()) + "</td><td class=\"col_2\">" + val
       + "</td></tr>");
     
     if( attr.getAttributes() != null )
      printAttbt( attr, out);
    }
   }
  }
 }
%>
<%

printAttbt( obj, out );

%>

							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<noscript>
		<div id="ae_noscript"
			class="ae_contents_frame ae_assign_font ae_white_bg">
			<div class="ae_center_box">
				<div id="ae_contents_box_915px">
					<div class="ae_error_area">ArrayExpress uses JavaScript for
						better data handling and enhanced representation. Please enable
						JavaScript if you want to continue browsing ArrayExpress.</div>
				</div>
			</div>
		</div>
	</noscript>
	<div id="ebi_footer">
		<iframe src="/inc/foot.htm" name="foot" marginwidth="0px"
			marginheight="0px" style="z-index: 2;" frameborder="0" height="22px"
			scrolling="no" width="800px">Your browser does not support
			inline frames or is currently configured not to display inline
			frames. Content can be viewed at actual source page:
			http://www.ebi.ac.uk/inc/foot.html</iframe>
	</div>
</body>
</html>