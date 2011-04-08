package org.eclipse.rap.rwt.supplemental.fileupload.test;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.rap.junit.RAPTestCase;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadServiceHandler;
import org.eclipse.rwt.RWT;

public class FileUploadServiceHandlerTest extends RAPTestCase {

  private static String BOUNDARY_DELIM = "------------cH2KM7cH2ae0Ij5GI3KM7cH2Ij5Ij5";
  private FileUploadServiceHandler handler;
  private String localName;
  private int port;

  protected void setUp() throws Exception {
    handler = new FileUploadServiceHandler();
    localName = RWT.getRequest().getLocalName();
    port = RWT.getRequest().getServerPort();
  }

  protected void tearDown() throws Exception {
    handler.dispose();
  }

  /**
   * Test clean creation and disposal. There is no exposed api to see if the
   * handler was actually registered or unregistered, but we assume that a test
   * for the rwt service handler framework has been passed. Communication with
   * the handler in subsequent tests will prove it is registered.
   */
  public void testCreateAndDisposeFileUploadServiceHandler() {
    try {
      FileUploadServiceHandler localHandler = new FileUploadServiceHandler();
      localHandler.dispose();
      assertEquals( true, localHandler.isDisposed() );
    } catch( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }
  }

  /**
   * Test if the url returned from getURL(String) is valid.
   */
  public void testGetURL() {
    try {
      String[] processIds = {
        "", // corner case
        "testProcessId", // normal case
        "123456789abcdefg", // normal case
        "<>&?" // corner case
      };
      for( int i = 0; i < processIds.length; i++ ) {
        String url = handler.getUrl( processIds[ i ] );
        // test if the url is valid
        assertNotNull( url );
        assertTrue( url.length() > 0 );
        HttpURLConnection con = ( HttpURLConnection )new URL( "http://"
                                                              + localName
                                                              + ":"
                                                              + port
                                                              + url ).openConnection();
        con.disconnect();
        //this is here because sometimes the test will hang....probably a latency issue with
        //opening repeated connections.
        Thread.sleep( 1000 );
      }
    } catch( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    }
  }

  private File runFileUpload( String contentType, String content, boolean testInvalid ) {
    File uploadedFile = null;
    String processId = "testUploadProcessId";
    String url = handler.getUrl( processId );
    HttpURLConnection con = null;
    try {
      con = ( HttpURLConnection )new URL( "http://" + localName + ":" + port + url ).openConnection();
      con.setDoOutput( true );
      con.setDoInput( true );
      con.setAllowUserInteraction( false );
      if( contentType != null )
        con.setRequestProperty( "Content-Type", contentType );
      con.setRequestMethod( "POST" );
      DataOutputStream dstream = new DataOutputStream( con.getOutputStream() );
      // Multi-part should be the only data accepted so this should not be
      // supported.
      dstream.writeBytes( content );
      dstream.close();
      // Must read Response to trigger upload
      InputStream in = con.getInputStream();
      // enable for debugging
      // int x;
      // while ( (x = in.read()) != -1)
      // {
      // System.out.write(x);
      // }
      in.close();
      long contentLength = handler.getContentLength( processId );
      long bytesRead = handler.getBytesRead( processId );
      Exception exception = handler.getException( processId );
      if( testInvalid ) {
        assertTrue( exception != null || contentLength <= 0 && contentLength == bytesRead );
      } else {
        if( exception != null )
          throw exception;
        assertTrue( contentLength > 0 && contentLength == bytesRead );
      }
      uploadedFile = handler.getUploadedFile( processId );
    } catch( Exception e ) {
      e.printStackTrace();
      fail( e.getMessage() );
    } finally {
      if( con != null ) {
        con.disconnect();
      }
      // so that we can reuse the processId again.
      handler.cancel( processId );
    }
    return uploadedFile;
  }

  private String getBoundary( String content, String fileName, String fileType ) {
    StringBuffer boundary = new StringBuffer( "--" ).append( BOUNDARY_DELIM ).append( "\r\n" );
    boundary.append( "Content-Disposition: form-data; name=\"faux_fileupload\"; filename=\"" );
    boundary.append( fileName ).append( "\"\r\n" );
    boundary.append( "Content-Type: " ).append( fileType ).append( "\r\n\r\n" );
    boundary.append( content );
    boundary.append( "\r\n--" + BOUNDARY_DELIM + "--\r\n" );
    return boundary.toString();
  }

  private boolean isFileMatch( String content, File testFile ) {
    boolean good = false;
    byte[] buffer = new byte[ ( int )testFile.length() ];
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream( new FileInputStream( testFile ) );
      bis.read( buffer );
      good = new String( buffer ).equals( content );
    } catch( Exception e ) {
      e.printStackTrace();
      good = false;
    } finally {
      try {
        bis.close();
      } catch( Exception e ) {
        // ignore
      }
    }
    return good;
  }

  /**
   * Tests that an invalid upload will fail.
   */
  public void testValidFileUpload() {
    String content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
    		"Proin rhoncus mollis velit nec dapibus. Proin convallis turpis volutpat " +
    		"odio scelerisque sit amet pharetra mi consectetur. Donec sit amet turpis " +
    		"ac lacus bibendum tempus. Quisque a felis massa. Nam justo nisi, ornare nec " +
    		"mollis ac, posuere quis metus. In eleifend tristique euismod. Proin congue" +
    		" venenatis pharetra. Nullam feugiat, ipsum a porttitor ultrices, sapien est " +
    		"posuere lectus, id porttitor libero metus non sem. Vestibulum lorem tortor, " +
    		"aliquet nec rhoncus quis, laoreet sed felis. In tincidunt, erat vitae tempus " +
    		"sodales, odio lorem rhoncus risus, at molestie nibh dolor eu arcu. Quisque vel " +
    		"sollicitudin magna. Donec volutpat velit et risus posuere sodales. Cras eget metus" +
    		" eget est faucibus ultricies. Quisque purus massa, blandit ac venenatis id," +
    		" mattis ut odio. Etiam euismod, massa et venenatis vulputate, velit arcu ultrices" +
    		" lectus, eget commodo velit erat non turpis. In in lectus augue.";
    File uploadedFile = runFileUpload( "multipart/form-data; boundary=" + BOUNDARY_DELIM,
                                       getBoundary( content, "test_file.txt", "text/plain" ),
                                       false );
    assertNotNull( uploadedFile );
    assertTrue( isFileMatch( content, uploadedFile ) );
  }

  /**
   * Tests that an invalid upload will fail.
   */
  public void testInvalidFileUpload() {
    String content = "Rogue string to try to break the upload handler...ha ha ha.";
    // Corner case: test with no content type
    runFileUpload( null, content, true );
    // Expected case: test with no boundary
    runFileUpload( "multipart/form-data", content, true );
    // Corner case: test with default/unsupported contenttype
    runFileUpload( "application/x-www-form-urlencoded", content, true );
  }
  // TODO listener tests to come soon
}