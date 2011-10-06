/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadHandlerStore;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadServiceHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.test.FileUploadTestUtil;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadReceiver;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.TestResponse;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.application.RWTFactory;


public class FileUploadHandler_Test extends TestCase {

  private FileUploadServiceHandler serviceHandler;
  private TestFileUploadListener uploadListener;
  private FileUploadHandler handler;

  protected void setUp() throws Exception {
    Fixture.setUp();
    handler = new FileUploadHandler( new TestFileUploadReceiver() );
    uploadListener = new TestFileUploadListener();
    serviceHandler = new FileUploadServiceHandler();
  }

  protected void tearDown() throws Exception {
    serviceHandler = null;
    uploadListener = null;
    handler.dispose();
    handler = null;
    Fixture.tearDown();
  }

  public void testCannotCreateWithNull() {
    try {
      new FileUploadHandler( null );
      fail();
    } catch( NullPointerException expected ) {
    }
  }

  public void testInitialized() {
    assertTrue( handler.getUploadUrl().indexOf( handler.getToken() ) != -1 );
    assertSame( handler, getRegisteredHandler( handler.getToken() ) );
  }

  public void testDispose() {
    handler.dispose();

    assertNull( getRegisteredHandler( handler.getToken() ) );
  }

  public void testGetReceiver() {
    FileUploadReceiver receiver = new TestFileUploadReceiver();
    FileUploadHandler handler = new FileUploadHandler( receiver );

    assertSame( receiver, handler.getReceiver() );
  }

  public void testAddListenerWithNull() {
    try {
      handler.addUploadListener( null );
      fail();
    } catch( NullPointerException expected ) {
    }
  }

  public void testAddListener() {
    handler.addUploadListener( uploadListener );

    TestFileUploadEvent event = new TestFileUploadEvent( handler );
    event.dispatchProgress();

    assertEquals( "progress.", uploadListener.getLog() );
    assertSame( event, uploadListener.getLastEvent() );
  }

  public void testAddListenerTwice() {
    handler.addUploadListener( uploadListener );
    handler.addUploadListener( uploadListener );

    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "progress.", uploadListener.getLog() );
  }

  public void testAddMultipleListeners() {
    TestFileUploadListener anotherUploadListener = new TestFileUploadListener();

    handler.addUploadListener( uploadListener );
    handler.addUploadListener( anotherUploadListener );
    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "progress.", uploadListener.getLog() );
    assertEquals( "progress.", anotherUploadListener.getLog() );
  }

  public void testRemoveListenerWithNull() {
    handler.addUploadListener( uploadListener );

    try {
      handler.removeUploadListener( null );
      fail();
    } catch( NullPointerException expected ) {
    }
  }

  public void testRemoveListener() {
    handler.addUploadListener( uploadListener );

    handler.removeUploadListener( uploadListener );
    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "", uploadListener.getLog() );
  }

  public void testRemoveListenerTwice() {
    handler.addUploadListener( uploadListener );

    handler.removeUploadListener( uploadListener );
    handler.removeUploadListener( uploadListener );
    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "", uploadListener.getLog() );
  }

  public void testRemoveOneOfTwoListeners() {
    handler.addUploadListener( uploadListener );
    TestFileUploadListener anotherUploadListener = new TestFileUploadListener();
    handler.addUploadListener( anotherUploadListener );

    handler.removeUploadListener( anotherUploadListener );
    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "progress.", uploadListener.getLog() );
    assertEquals( "", anotherUploadListener.getLog() );
  }

  public void testUpload() throws Exception {
    TestFileUploadReceiver receiver = new TestFileUploadReceiver();
    FileUploadHandler handler = new FileUploadHandler( receiver );
    String content = "Lorem ipsum dolor sit amet.";

    fakeUploadRequest( handler, content, "text/plain", "short.txt" );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    assertEquals( content.length(), receiver.getTotal() );
  }

  public void testUploadWithMaxLimit() throws Exception {
    TestFileUploadReceiver receiver = new TestFileUploadReceiver();
    FileUploadHandler handler = new FileUploadHandler( receiver );
    handler.setMaxFileSize( 1000 );
    String content = "Lorem ipsum dolor sit amet.\n";

    fakeUploadRequest( handler, content, "text/plain", "short.txt" );
    serviceHandler.service();

    assertEquals( 0, getResponseErrorStatus() );
    assertEquals( content.length(), receiver.getTotal() );
  }

  public void testUploadWithExceedMaxLimit() throws Exception {
    TestFileUploadReceiver receiver = new TestFileUploadReceiver();
    FileUploadHandler handler = new FileUploadHandler( receiver );
    handler.setMaxFileSize( 1000 );
    StringBuffer buffer = new StringBuffer();
    for( int i = 0; i < 40; i++ ) {
      buffer.append( "Lorem ipsum dolor sit amet.\n" );
    }
    String content = buffer.toString();

    fakeUploadRequest( handler, content, "text/plain", "short.txt" );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, getResponseErrorStatus() );
    assertTrue( getResponseContent().indexOf( "file exceeds its maximum permitted  size" ) != -1 );
  }

  public void testUploadWithException() throws Exception {
    FileUploadReceiver receiver = new FileUploadReceiver() {
      public void receive( InputStream dataStream, IFileUploadDetails details ) throws IOException {
        throw new IOException( "the error message" );
      }
    };
    FileUploadHandler handler = new FileUploadHandler( receiver );

    fakeUploadRequest( handler, "The content", "text/plain", "short.txt" );
    serviceHandler.service();

    assertEquals( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getResponseErrorStatus() );
    assertTrue( getResponseContent().indexOf( "the error message" ) != -1 );
  }

  public void testServiceHandlerIsRegistered() throws IOException, ServletException {
    TestFileUploadListener listener = new TestFileUploadListener();
    handler.addUploadListener( listener );

    fakeUploadRequest( handler, "The content", "text/plain", "short.txt" );
    RWTFactory.getServiceManager().getHandler().service();

    assertNotNull( listener.getLastEvent() );
  }

  private static void fakeUploadRequest( FileUploadHandler handler,
                                         String content,
                                         String contentType,
                                         String fileName )
  {
    FileUploadTestUtil.fakeUploadRequest( handler, content, contentType, fileName );
  }

  private static FileUploadHandler getRegisteredHandler( String token ) {
    return FileUploadHandlerStore.getInstance().getHandler( token );
  }

  private static int getResponseErrorStatus() {
    TestResponse response = ( TestResponse )RWT.getResponse();
    return response.getErrorStatus();
  }

  private static String getResponseContent() {
    TestResponse response = ( TestResponse )RWT.getResponse();
    return response.getContent();
  }
}
