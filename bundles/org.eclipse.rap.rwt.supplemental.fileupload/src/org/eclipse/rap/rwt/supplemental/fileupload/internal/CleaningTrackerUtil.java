/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import org.apache.commons.io.FileCleaningTracker;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ISessionStore;
import org.eclipse.rap.rwt.service.SessionStoreEvent;
import org.eclipse.rap.rwt.service.SessionStoreListener;


class CleaningTrackerUtil {

  static final String TRACKER_ATTR
    = CleaningTrackerUtil.class.getName().concat( "#cleaningTrackerInstance" );
  private static final FileUploadCleanupHandler LISTENER = new FileUploadCleanupHandler();

  private CleaningTrackerUtil() {
    // prevent instantiation
  }

  public static FileCleaningTracker getCleaningTracker( boolean create ) {
    FileCleaningTracker tracker;
    ISessionStore store = RWT.getSessionStore();
    synchronized( store ) {
      tracker = ( FileCleaningTracker )store.getAttribute( TRACKER_ATTR );
      if( tracker == null && create ) {
        tracker = new FileCleaningTracker();
        store.setAttribute( TRACKER_ATTR, tracker );
        store.addSessionStoreListener( LISTENER );
      }
    }
    return tracker;
  }

  static void stopCleaningTracker( ISessionStore store ) {
    synchronized( store ) {
      FileCleaningTracker tracker = ( FileCleaningTracker )store.getAttribute( TRACKER_ATTR );
      if( tracker != null ) {
        tracker.exitWhenFinished();
        store.removeAttribute( TRACKER_ATTR );
      }
    }
  }

  private static class FileUploadCleanupHandler implements SessionStoreListener {
    public void beforeDestroy( SessionStoreEvent event ) {
      stopCleaningTracker( event.getSessionStore() );
    }
  }
}
