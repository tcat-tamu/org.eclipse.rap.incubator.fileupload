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
package org.eclipse.swt.internal.widgets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.FileUploadRunnable.State;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FileUploadRunnable_Test {

  private Display display;
  private Shell shell;
  private FileUploadRunnable runnable;
  private FileUpload fileUpload;
  private UploadPanel uploadPanel;
  private ProgressCollector progressCollector;
  private FileUploadHandler handler;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    fileUpload = mock( FileUpload.class );
    when( fileUpload.getDisplay() ).thenReturn( display );
    uploadPanel = mock( UploadPanel.class );
    progressCollector = mock( ProgressCollector.class );
    handler = spy( new FileUploadHandler( mock( DiskFileUploadReceiver.class ) ) );
    runnable = new FileUploadRunnable( fileUpload, uploadPanel, progressCollector, handler );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testCreate_addsUploadListener() {
    verify( handler ).addUploadListener( any( FileUploadListener.class ) );
  }

  @Test
  public void testCreate_updatesUploadPanelIcons() {
    verify( uploadPanel ).updateIcons( State.WAITING );
  }

  @Test
  public void testFileUploadDispose_removesUploadListener() {
    fileUpload = new FileUpload( shell, SWT.NONE );
    runnable = new FileUploadRunnable( fileUpload, uploadPanel, progressCollector, handler );

    fileUpload.dispose();

    verify( handler ).removeUploadListener( any( FileUploadListener.class ) );
  }

  @Test
  public void testFileUploadDispose_disposesHandler() {
    fileUpload = new FileUpload( shell, SWT.NONE );
    runnable = new FileUploadRunnable( fileUpload, uploadPanel, progressCollector, handler );

    fileUpload.dispose();

    verify( handler ).dispose();
  }

  @Test
  public void testGetState_initial() {
    assertEquals( State.WAITING, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_triggersHandleProgress() {
    new TestFileUploadEvent( handler ).dispatchProgress();
    runEventsLoop();

    assertEquals( State.UPLOADING, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_triggersHandleFinished() {
    new TestFileUploadEvent( handler ).dispatchFinished();
    runEventsLoop();

    assertEquals( State.FINISHED, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_triggersHandleFailed() {
    new TestFileUploadEvent( handler ).dispatchFailed();
    runEventsLoop();

    assertEquals( State.FAILED, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_doesNotTriggerHandleProgress_onDisposedDisplay() {
    new TestFileUploadEvent( handler ).dispatchProgress();
    display.dispose();

    assertEquals( State.WAITING, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_doesNotTriggerHandleFinished_onDisposedDisplay() {
    new TestFileUploadEvent( handler ).dispatchFinished();
    display.dispose();

    assertEquals( State.WAITING, runnable.getState() );
  }

  @Test
  public void testFileUploadEvent_doesNotTriggerHandleFailed_onDisposedDisplay() {
    new TestFileUploadEvent( handler ).dispatchFailed();
    display.dispose();

    assertEquals( State.WAITING, runnable.getState() );
  }

  @Test
  public void testHandleProgress_updatesIcons() {
    runnable.handleProgress( 100, 200 );

    verify( uploadPanel ).updateIcons( State.UPLOADING );
  }

  @Test
  public void testHandleProgress_twice_updatesIconsOnce() {
    runnable.handleProgress( 100, 200 );
    runnable.handleProgress( 150, 200 );

    verify( uploadPanel ).updateIcons( State.UPLOADING );
  }

  @Test
  public void testHandleProgress_updatesProgress() {
    runnable.handleProgress( 100, 200 );

    verify( progressCollector ).updateProgress( 50 );
  }

  @Test
  public void testHandleFinished_updatesIcons() {
    runnable.handleFinished( Collections.EMPTY_LIST );

    verify( uploadPanel ).updateIcons( State.FINISHED );
  }

  @Test
  public void testHandleFinished_updatesCompletedFiles() {
    List<String> completedFiles = new ArrayList<String>();
    completedFiles.add( "foo" );
    completedFiles.add( "bar" );

    runnable.handleFinished( completedFiles );

    verify( progressCollector ).updateCompletedFiles( eq( completedFiles ) );
  }

  @Test
  public void testHandleFailed_updatesIcons() {
    runnable.handleFailed();

    verify( uploadPanel ).updateIcons( State.FAILED );
  }

  @Test
  public void testRun_onDisposedDisplay() {
    display.dispose();

    runnable.run();
  }

  @Test
  public void testRun_callsFileUploadSubmit() {
    sheduleFinishedEvent();

    runnable.run();
    runEventsLoop();

    verify( fileUpload ).submit( anyString() );
  }

  @Test
  public void testRun_disposesMutliFileUpload() {
    doReturn( Integer.valueOf( SWT.MULTI ) ).when( fileUpload ).getStyle();
    sheduleFinishedEvent();

    runnable.run();
    runEventsLoop();

    verify( fileUpload ).dispose();
  }

  @Test
  public void testRun_doesNotDisposeSingleFileUpload() {
    sheduleFinishedEvent();

    runnable.run();
    runEventsLoop();

    verify( fileUpload, never() ).dispose();
  }

  private void sheduleFinishedEvent() {
    Thread thread = new Thread( new Runnable() {
      public void run() {
        try {
          Thread.sleep( 200 );
        } catch( InterruptedException e ) {
        }
        new TestFileUploadEvent( handler ).dispatchFinished();
      }
    } );
    thread.start();
  }

  private void runEventsLoop() {
    while( display.readAndDispatch() ) {
    }
  }

  public class TestFileUploadEvent extends FileUploadEvent {

    public TestFileUploadEvent( FileUploadHandler handler ) {
      super( handler );
    }

    private static final long serialVersionUID = 1L;

    @Override
    public FileDetails[] getFileDetails() {
      return new FileDetails[ 0 ];
    }

    @Override
    public long getContentLength() {
      return 0;
    }

    @Override
    public long getBytesRead() {
      return 0;
    }

    @Override
    public Exception getException() {
      return null;
    }

    @Override
    public void dispatchProgress() {
      super.dispatchProgress();
    }

    @Override
    public void dispatchFinished() {
      super.dispatchFinished();
    }

    @Override
    public void dispatchFailed() {
      super.dispatchFailed();
    }

  }

}
