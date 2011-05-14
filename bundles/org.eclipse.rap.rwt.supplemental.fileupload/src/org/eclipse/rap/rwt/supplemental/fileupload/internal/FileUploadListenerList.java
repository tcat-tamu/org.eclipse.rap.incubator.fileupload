/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.IFileUploadListener;


public final class FileUploadListenerList {

  private final Set listeners;

  public FileUploadListenerList() {
    listeners = new HashSet();
  }

  public void addUploadListener( IFileUploadListener listener ) {
    listeners.add( listener );
  }

  public void removeUploadListener( IFileUploadListener listener ) {
    listeners.remove( listener );
  }

  public void notifyUploadProgress( FileUploadEvent event ) {
    Iterator iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      IFileUploadListener listener = ( IFileUploadListener )iterator.next();
      listener.uploadProgress( event );
    }
  }

  public void notifyUploadFinished( FileUploadEvent event ) {
    Iterator iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      IFileUploadListener listener = ( IFileUploadListener )iterator.next();
      listener.uploadFinished( event );
    }
  }

  public void notifyUploadFailed( FileUploadEvent event ) {
    Iterator iterator = listeners.iterator();
    while( iterator.hasNext() ) {
      IFileUploadListener listener = ( IFileUploadListener )iterator.next();
      listener.uploadFailed( event );
    }
  }
}
