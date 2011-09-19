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

import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadReceiver;
import org.eclipse.rap.rwt.testfixture.Fixture;

import junit.framework.TestCase;


public class FileUploadEvent_Test extends TestCase {

  private FileUploadHandler handler;

  protected void setUp() throws Exception {
    Fixture.setUp();
    handler = new FileUploadHandler( new TestFileUploadReceiver() );
  }

  protected void tearDown() throws Exception {
    handler.dispose();
    handler = null;
    Fixture.tearDown();
  }

  public void testCannotCreateWithNullSource() {
    try {
      new TestFileUploadEvent( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testGetSource() {
    TestFileUploadEvent event = new TestFileUploadEvent( handler );

    assertSame( handler, event.getSource() );
  }

  public void testDispatchProgress() {
    TestFileUploadListener listener = new TestFileUploadListener();
    handler.addUploadListener( listener );

    new TestFileUploadEvent( handler ).dispatchProgress();

    assertEquals( "progress.", listener.getLog() );
  }
  
  public void testDispatchFinished() {
    TestFileUploadListener listener = new TestFileUploadListener();
    handler.addUploadListener( listener );
    
    new TestFileUploadEvent( handler ).dispatchFinished();
    
    assertEquals( "finished.", listener.getLog() );
  }
  
  public void testDispatchFailed() {
    TestFileUploadListener listener = new TestFileUploadListener();
    handler.addUploadListener( listener );
    
    new TestFileUploadEvent( handler ).dispatchFailed();
    
    assertEquals( "failed.", listener.getLog() );
  }
}
