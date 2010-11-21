package uk.ac.ebi.biosd.client;

import java.util.Map;
import java.util.TreeMap;

public class LinkManager
{
 static
 {
  instance = new LinkManager();
  
  init();
 }
 
 private static LinkManager instance;
 
 private Map<String, LinkClickListener> lsnrs = new TreeMap<String, LinkClickListener>();
 
 private static native void init()
 /*-{
  $wnd.linkClicked =
          @uk.ac.ebi.biosd.client.LinkManager::jsLinkClicked(Ljava/lang/String;Ljava/lang/String;);
 }-*/;
 
 public static LinkManager getInstance()
 {
  return instance;
 }
 
 @SuppressWarnings("unused")
 private static void jsLinkClicked( String linkId, String param )
 {
  getInstance().linkClicked(linkId, param);
 }

 private void linkClicked(String linkId, String param)
 {
  LinkClickListener lsn = lsnrs.get(linkId);
  
  if( lsn != null )
   lsn.linkClicked(param);
 }
 
 public void addLinkClickListener( String linkId, LinkClickListener l )
 {
  lsnrs.put(linkId, l);
 }
 
 public void removeLinkClickListener( String linkId )
 {
  lsnrs.remove(linkId);
 }

}
