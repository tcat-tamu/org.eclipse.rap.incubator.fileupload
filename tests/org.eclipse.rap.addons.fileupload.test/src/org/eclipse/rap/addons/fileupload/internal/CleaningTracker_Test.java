/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.fileupload.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CleaningTracker_Test {

  private File tempFile;
  private CleaningTracker cleaningTracker;

  @Before
  public void setUp() throws IOException {
    tempFile = File.createTempFile( "temp-", null );
    cleaningTracker = new CleaningTracker();
  }

  @After
  public void tearDown() {
    tempFile.delete();
  }

  @Test
  public void testDeleteTemporaryFiles_trackedByFile_1() {
    cleaningTracker.track( tempFile, null );

    cleaningTracker.deleteTemporaryFiles();

    assertFalse( tempFile.exists() );
    assertEquals( 0, cleaningTracker.getDeleteFailures().size() );
  }

  @Test
  public void testDeleteTemporaryFiles_trackedByFile_2() {
    cleaningTracker.track( tempFile, null, null );

    cleaningTracker.deleteTemporaryFiles();

    assertFalse( tempFile.exists() );
    assertEquals( 0, cleaningTracker.getDeleteFailures().size() );
  }

  @Test
  public void testDeleteTemporaryFiles_trackedByPath_1() {
    cleaningTracker.track( tempFile.getAbsolutePath(), null );

    cleaningTracker.deleteTemporaryFiles();

    assertFalse( tempFile.exists() );
    assertEquals( 0, cleaningTracker.getDeleteFailures().size() );
  }

  @Test
  public void testDeleteTemporaryFiles_trackedByPath_2() {
    cleaningTracker.track( tempFile.getAbsolutePath(), null, null );

    cleaningTracker.deleteTemporaryFiles();

    assertFalse( tempFile.exists() );
    assertEquals( 0, cleaningTracker.getDeleteFailures().size() );
  }

  @Test
  public void testExitWhenFinished_deletesTemporaryFiles() {
    cleaningTracker.track( tempFile, null );

    cleaningTracker.exitWhenFinished();

    assertFalse( tempFile.exists() );
    assertEquals( 0, cleaningTracker.getDeleteFailures().size() );
  }

  @Test
  public void testGetTrackCount() {
    cleaningTracker.track( tempFile, null );

    assertEquals( 1, cleaningTracker.getTrackCount() );
  }

  @Test
  public void testGetDeleteFailures() {
    String path = tempFile.getAbsolutePath();
    tempFile.delete();
    tempFile = mock( File.class );
    doReturn( Boolean.FALSE ).when( tempFile ).delete();
    doReturn( path ).when( tempFile ).getAbsolutePath();
    cleaningTracker.track( tempFile, null );

    cleaningTracker.deleteTemporaryFiles();

    assertEquals( 1, cleaningTracker.getDeleteFailures().size() );
  }

}
