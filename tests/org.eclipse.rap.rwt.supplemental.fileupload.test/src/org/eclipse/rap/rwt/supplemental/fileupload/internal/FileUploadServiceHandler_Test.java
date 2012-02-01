/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.TestAdapter;
import org.eclipse.rap.rwt.supplemental.fileupload.test.FileUploadTestUtil;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadReceiver;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.TestRequest;
import org.eclipse.rap.rwt.testfixture.TestResponse;
import org.eclipse.rwt.RWT;


public class FileUploadServiceHandler_Test extends TestCase {

  private FileUploadServiceHandler serviceHandler;
  private File tempDirectory;
  private TestFileUploadListener testListener;
  private TestFileUploadReceiver testReceiver;
  private FileUploadHandler uploadHandler;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    serviceHandler = new FileUploadServiceHandler();
    testReceiver = new TestFileUploadReceiver();
    uploadHandler = new FileUploadHandler( testReceiver );
    testListener = new TestFileUploadListener();
    tempDirectory = FileUploadTestUtil.createTempDirectory();
  }

  @Override
  protected void tearDown() throws Exception {
    FileUploadTestUtil.deleteRecursively( tempDirectory );
    tempDirectory = null;
    testListener = null;
    serviceHandler = null;
    Fixture.tearDown();
  }

  public void testUploadShortFile() throws Exception {
    uploadHandler.addUploadListener( testListener );
    String content = "Lorem ipsum dolor sit amet.";

    fakeUploadRequest( content, "text/plain", "short.txt"  );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    assertEquals( "progress.finished.", testListener.getLog() );
    FileUploadEvent event = testListener.getLastEvent();
    assertEquals( "short.txt", event.getFileName() );
    assertEquals( "text/plain", event.getContentType() );
    assertTrue( event.getContentLength() > content.length() );
    assertEquals( event.getContentLength(), event.getBytesRead() );
    assertEquals( content, new String( testReceiver.getContent() ) );
  }

  public void testUploadBigFile() throws Exception {
    TestFileUploadListener testListener = new TestFileUploadListener() {
      @Override
      public void uploadProgress( FileUploadEvent info ) {
        log.append( "progress(" + info.getBytesRead() + "/" + info.getContentLength() + ").");
      }
    };
    uploadHandler.addUploadListener( testListener );
    String content = createExampleContent( 12000 );

    fakeUploadRequest( content, "text/plain", "test.txt"  );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    String expected = "progress(4096/12134).progress(8174/12134).progress(12134/12134).finished.";
    assertEquals( expected, testListener.getLog() );
    FileUploadEvent uploadedItem = testListener.getLastEvent();
    assertEquals( content, new String( testReceiver.getContent() ) );
    assertEquals( "text/plain", uploadedItem.getContentType() );
  }

  public void testCanUploadEmptyFile() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "", "text/plain", "empty.txt"  );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    assertEquals( "progress.finished.", testListener.getLog() );
    assertEquals( "", new String( testReceiver.getContent() ) );
  }

  public void testCanUploadFileWithoutContentType() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "Some content", null, "test.txt"  );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    assertEquals( "progress.finished.", testListener.getLog() );
    FileUploadEvent uploadedItem = testListener.getLastEvent();
    assertEquals( "Some content", new String( testReceiver.getContent() ) );
    assertEquals( null, uploadedItem.getContentType() );
  }

  public void testUploadWithoutToken() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( null );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_FORBIDDEN, getResponseErrorStatus() );
    assertEquals( "", testListener.getLog() );
  }

  public void testUploadWithInvalidToken() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "unknown-id" );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_FORBIDDEN, getResponseErrorStatus() );
    assertEquals( "", testListener.getLog() );
  }

  public void testUploadWithGetRequest() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "Some content", "text/plain", "test.txt"  );
    TestRequest request = ( TestRequest )RWT.getRequest();
    request.setMethod( "GET" );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_METHOD_NOT_ALLOWED, getResponseErrorStatus() );
    assertEquals( "", testListener.getLog() );
  }

  public void testUploadRequestWithWrongContentType() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "Some content", "text/plain", "test.txt"  );
    TestRequest request = ( TestRequest )RWT.getRequest();
    request.setContentType( "application/octet-stream" );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, getResponseErrorStatus() );
    assertEquals( "", testListener.getLog() );
  }

  public void testUploadRequestWithoutBoundary() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "Some content", "text/plain", "test.txt"  );
    TestRequest request = ( TestRequest )RWT.getRequest();
    request.setBody( "some bogus body content" );
    serviceHandler.service();

    assertEquals( "progress.failed.", testListener.getLog() );
  }

  // Some browsers, such as IE and Opera, include path information, we cut them off for consistency
  public void testOriginalFileNameWithPathSegment() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "some content", "text/plain", "/tmp/some.txt"  );
    serviceHandler.service();

    FileUploadEvent uploadedItem = testListener.getLastEvent();
    assertEquals( "some.txt", uploadedItem.getFileName() );
  }

  // Some browsers, such as IE and Opera, include path information, we cut them off for consistency
  public void testOriginalFileWithWindowsPath() throws Exception {
    uploadHandler.addUploadListener( testListener );

    fakeUploadRequest( "some content", "text/plain", "C:\\temp\\some.txt"  );
    serviceHandler.service();

    FileUploadEvent uploadedItem = testListener.getLastEvent();
    assertEquals( "some.txt", uploadedItem.getFileName() );
  }

  public void testGetURL() {
    String head = "rap?custom_service_handler=org.eclipse.rap.fileupload&token=";

    assertEquals( head, FileUploadServiceHandler.getUrl( "" ) );
    assertEquals( head + "<>&?", FileUploadServiceHandler.getUrl( "<>&?" ) );
    assertEquals( head + "testToken", FileUploadServiceHandler.getUrl( "testToken" ) );
    assertEquals( head + "123456789abcdef", FileUploadServiceHandler.getUrl( "123456789abcdef" ) );
  }

  public void testFileUploadCleanupThreadCreated() throws Exception {
    assertNull( findFileReaper() );

    simulateUpload();

    assertNotNull( findFileReaper() );
  }

  public void testFileUploadCleanupThreadDestroyed() throws Exception {
    assertNull( findFileReaper() );

    simulateUpload();
    RWT.getRequest().getSession().invalidate();

    assertNull( findFileReaper() );
  }

  private void simulateUpload() throws IOException, ServletException {
    String content = "Lorem ipsum dolor sit amet.";
    fakeUploadRequest( content, "text/plain", "short.txt"  );
    serviceHandler.service();
  }

  private Thread findFileReaper() throws InterruptedException {
    // File reaper is destroyed upon garbage collection
    System.gc();
    // Allow file reaper to die
    Thread.sleep( 10 );
    Set threadSet = Thread.getAllStackTraces().keySet();
    Iterator threadItr = threadSet.iterator();
    Thread fileReaper = null;
    while ( threadItr.hasNext() && fileReaper == null ) {
      Thread t = ( Thread ) threadItr.next();
      if ( t.getName().equals( "File Reaper" ) ) {
        fileReaper = t;
      }
    }
    return fileReaper;
  }

  private void fakeUploadRequest( String token ) {
    FileUploadTestUtil.fakeUploadRequest( token, "TestContent", "text/plain", "test.txt" );
  }

  private void fakeUploadRequest( String content, String contentType, String fileName ) {
    String token = TestAdapter.getTokenFor( uploadHandler );
    FileUploadTestUtil.fakeUploadRequest( token, content, contentType, fileName );
  }

  private static int getResponseErrorStatus() {
    TestResponse response = ( TestResponse )RWT.getResponse();
    return response.getErrorStatus();
  }

  private static String createExampleContent( int length ) {
    byte[] bytes = new byte[ length ];
    for( int i = 0; i < length; i++ ) {
      int col = i % 91;
      bytes[ i ] = ( byte )( col == 90 ? 10 : 33 + col );
    }
    return new String( bytes );
  }
}
