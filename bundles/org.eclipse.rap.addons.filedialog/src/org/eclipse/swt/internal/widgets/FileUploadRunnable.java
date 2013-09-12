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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;


public class FileUploadRunnable implements Runnable {

  static enum State { WAITING, UPLOADING, FINISHED, FAILED }

  private final Display display;
  private final FileUpload fileUpload;
  private final UploadPanel uploadPanel;
  private final ProgressCollector progressCollector;
  private final FileUploadHandler handler;
  private final AtomicReference<State> state;
  private final Object lock;

  public FileUploadRunnable( FileUpload fileUpload,
                             UploadPanel uploadPanel,
                             ProgressCollector progressCollector,
                             FileUploadHandler handler )
  {
    this.fileUpload = fileUpload;
    this.uploadPanel = uploadPanel;
    this.progressCollector = progressCollector;
    this.handler = handler;
    display = fileUpload.getDisplay();
    lock = new Object();
    state = new AtomicReference<State>( State.WAITING );
    setupFileUploadHandler( handler, fileUpload );
    uploadPanel.updateIcons( State.WAITING );
  }

  public void run() {
    asyncExec( new Runnable() {
      public void run() {
        fileUpload.submit( handler.getUploadUrl() );
      }
    } );
    if( !display.isDisposed() ) {
      doWait();
    }
    asyncExec( new Runnable() {
      public void run() {
        if( !fileUpload.isDisposed() && ( fileUpload.getStyle() & SWT.MULTI ) != 0 ) {
          fileUpload.dispose();
        }
      }
    } );
  }

  private void setupFileUploadHandler( final FileUploadHandler handler, FileUpload fileUpload ) {
    final UploadProgressListener listener = new UploadProgressListener();
    handler.addUploadListener( listener );
    fileUpload.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        handler.removeUploadListener( listener );
        handler.dispose();
      }
    } );
  }

  void handleProgress( long bytesRead, long contentLength ) {
    if( state.compareAndSet( State.WAITING, State.UPLOADING ) ) {
      uploadPanel.updateIcons( State.UPLOADING );
    }
    double fraction = bytesRead / ( double )contentLength;
    int percent = ( int )Math.floor( fraction * 100 );
    progressCollector.updateProgress( percent );
  }

  void handleFinished( List<String> targetFileNames ) {
    state.set( State.FINISHED );
    uploadPanel.updateIcons( State.FINISHED );
    progressCollector.updateCompletedFiles( targetFileNames );
  }

  void handleFailed() {
    state.set( State.FAILED );
    uploadPanel.updateIcons( State.FAILED );
  }

  State getState() {
    return state.get();
  }

  private void doWait() {
    synchronized( lock ) {
      try {
        lock.wait();
      } catch( InterruptedException exception ) {
        // allow executor to properly shutdown
      }
    }
  }

  private void doNotify() {
    synchronized( lock ) {
      lock.notify();
    }
  }

  private void asyncExec( Runnable runnable ) {
    if( !display.isDisposed() ) {
      display.asyncExec( runnable );
    }
  }

  private final class UploadProgressListener implements FileUploadListener {

    public void uploadProgress( final FileUploadEvent event ) {
      asyncExec( new Runnable() {
        public void run() {
          handleProgress( event.getBytesRead(), event.getContentLength() );
        }
      } );
    }

    public void uploadFinished( FileUploadEvent event ) {
      final List<String> targetFileNames = getTargetFileNames();
      asyncExec( new Runnable() {
        public void run() {
          handleFinished( targetFileNames );
        }
      } );
      doNotify();
    }

    public void uploadFailed( FileUploadEvent event ) {
      asyncExec( new Runnable() {
        public void run() {
          handleFailed();
        }
      } );
      doNotify();
    }

    private List<String> getTargetFileNames() {
      List<String> result = new ArrayList<String>();
      File[] targetFiles = ( ( DiskFileUploadReceiver )handler.getReceiver() ).getTargetFiles();
      for( File targetFile : targetFiles ) {
        result.add( targetFile.getAbsolutePath() );
      }
      return result;
    }

  }

}
