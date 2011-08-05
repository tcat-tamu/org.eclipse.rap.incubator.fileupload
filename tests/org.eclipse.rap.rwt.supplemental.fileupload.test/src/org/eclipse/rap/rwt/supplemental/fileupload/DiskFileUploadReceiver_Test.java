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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.supplemental.fileupload.test.FileUploadTestUtil;


public class DiskFileUploadReceiver_Test extends TestCase {

  private File createdFile;

  protected void tearDown() throws Exception {
    if( createdFile != null ) {
      createdFile.delete();
      createdFile = null;
    }
  }

  public void testInitialGetTargetFile() {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    assertNull( receiver.getTargetFile() );
  }

  public void testCreateTargetFile() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    createdFile = receiver.createTargetFile( "foo.bar" );

    assertTrue( createdFile.exists() );
    assertTrue( createdFile.getName().startsWith( "foo." ) );
    assertTrue( createdFile.getName().endsWith( ".bar" ) );
  }

  public void testCreatedTargetFilesDiffer() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    createdFile = receiver.createTargetFile( "foo.bar" );
    File createdFile2 = receiver.createTargetFile( "foo.bar" );
    createdFile2.deleteOnExit();

    assertFalse( createdFile.getAbsolutePath().equals( createdFile2.getAbsolutePath() ) );
  }

  public void testReceive() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    String content = "Hello world!";

    IFileUploadDetails details = new FileUploadDetails( "foo.bar", "text/plain", content.length() );
    receiver.receive( new ByteArrayInputStream( content.getBytes() ), details );
    createdFile = receiver.getTargetFile();

    assertNotNull( createdFile );
    assertTrue( createdFile.exists() );
    assertEquals( content, FileUploadTestUtil.getFileContents( createdFile ) );
  }

  public void testReceiveWithNullDetails() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    String content = "Hello world!";

    receiver.receive( new ByteArrayInputStream( content.getBytes() ), null );
    createdFile = receiver.getTargetFile();

    assertNotNull( createdFile );
    assertTrue( createdFile.exists() );
    assertTrue( createdFile.getName().startsWith( "upload." ) );
    assertTrue( createdFile.getName().endsWith( ".tmp" ) );
    assertEquals( content, FileUploadTestUtil.getFileContents( createdFile ) );
  }
}
