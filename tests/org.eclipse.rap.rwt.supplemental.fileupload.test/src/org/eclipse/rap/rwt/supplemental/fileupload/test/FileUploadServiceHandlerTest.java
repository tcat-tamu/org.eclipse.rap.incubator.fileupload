package org.eclipse.rap.rwt.supplemental.fileupload.test;

import java.net.URL;
import java.net.URLConnection;

import org.eclipse.rap.junit.RAPTestCase;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadServiceHandler;
import org.eclipse.rwt.RWT;

public class FileUploadServiceHandlerTest extends RAPTestCase {

  /**
   * Test clean creation and disposal. There is no exposed api to see if the handler was actually 
   * registered or unregistered, but we assume that a test for the rwt service handler framework
   * has been passed. Communication with the handler in subsequent tests will prove it is
   * registered. 
   */
  public void testCreateAndDisposeFileUploadServiceHandler() {
    try {
      FileUploadServiceHandler handler = new FileUploadServiceHandler();
      handler.dispose();
      assertEquals( true, handler.isDisposed() );
    } catch( Exception e ) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  /**
   * Test if the url returned from getURL(String) is valid. 
   */
  public void testGetURL() {
    try {
      FileUploadServiceHandler handler = new FileUploadServiceHandler();
      String[] processIds = {
        "",  //corner case
        "testProcessId", //normal case
        "123456789abcdefg", //normal case
        "<>&?"  //corner case
      };
      
      for (int i = 0; i < processIds.length; i++) {
        String url = handler.getUrl( processIds[i] );
        //test if the url is valid
        assertNotNull( url );
        assertTrue( url.length() > 0 );
        String localName = RWT.getRequest().getLocalName();
        URLConnection con = new URL("http://"+localName+url).openConnection();
      }
      handler.dispose();
    } catch( Exception e ) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  //TODO upload tests to come soon
  //TODO listener tests to come soon
  
}