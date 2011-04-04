/*******************************************************************************
 * Copyright (c) 2011 Texas Center for Applied Technology
 * Texas Engineering Experiment Station
 * The Texas A&M University System
 * All Rights Reserved. 
 * 
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Austin Riddle (Texas Center for Applied Technology) - 
 *                   initial api and implementation
 * 
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.supplemental.fileupload.event.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.event.FileUploadListener;

public class FileUploadProgressHandler {

  private final Map listeners;

  public FileUploadProgressHandler() {
    super();
    listeners = new HashMap();
  }

  public synchronized void addListener( FileUploadListener listener,
                                        String processId )
  {
    List listenerList = ( List )listeners.get( processId );
    if( listenerList == null ) {
      listenerList = new ArrayList();
      listeners.put( processId, listenerList );
    }
    if( !listenerList.contains( listener ) ) {
      listenerList.add( listener );
    }
  }

  public synchronized void removeListener( FileUploadListener listener,
                                           String processId )
  {
    List listenerList = ( List )listeners.get( processId );
    if( listenerList != null ) {
      listenerList.remove( listener );
    }
  }

  public synchronized void clearListeners( String processId ) {
    listeners.remove( processId );
  }

  public synchronized void updateProgress( final FileUploadStorageItem fileUploadStorageitem,
                                           final String uploadProcessId )
  {
    List procListeners = ( List )listeners.get( uploadProcessId );
    if( procListeners != null && procListeners.size() > 0) {
      final long bytesRead = fileUploadStorageitem.getBytesRead();
      final long contentLength = fileUploadStorageitem.getContentLength();
      String contentType = fileUploadStorageitem.getContentType();
      for( int i = 0; i < procListeners.size(); i++ ) {
        FileUploadListener listener = ( FileUploadListener )procListeners.get( i );
        Exception uploadException = fileUploadStorageitem.getException();
        final FileUploadEvent evt = new FileUploadEvent( uploadProcessId,
                                                         bytesRead,
                                                         contentLength,
                                                         contentType,
                                                         uploadException );
        boolean finished = bytesRead == contentLength &&
                           uploadException == null;
        if( uploadException != null ) {
          listener.uploadException( evt );
        } else if( finished ) {
          listener.uploadFinished( evt );
        } else {
          listener.uploadInProgress( evt );
        }
      }
    }
  }
}
