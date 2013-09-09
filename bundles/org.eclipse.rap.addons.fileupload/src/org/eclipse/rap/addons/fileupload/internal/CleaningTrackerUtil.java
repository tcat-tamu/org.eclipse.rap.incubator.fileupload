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
package org.eclipse.rap.addons.fileupload.internal;

import org.apache.commons.io.FileCleaningTracker;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;


class CleaningTrackerUtil {

  static final String TRACKER_ATTR
    = CleaningTrackerUtil.class.getName().concat( "#cleaningTrackerInstance" );
  private static final FileUploadCleanupHandler LISTENER = new FileUploadCleanupHandler();

  private CleaningTrackerUtil() {
    // prevent instantiation
  }

  public static FileCleaningTracker getCleaningTracker( boolean create ) {
    FileCleaningTracker tracker;
    UISession uisession = RWT.getUISession();
    synchronized( uisession ) {
      tracker = ( FileCleaningTracker )uisession.getAttribute( TRACKER_ATTR );
      if( tracker == null && create ) {
        tracker = new FileCleaningTracker();
        uisession.setAttribute( TRACKER_ATTR, tracker );
        uisession.addUISessionListener( LISTENER );
      }
    }
    return tracker;
  }

  static void stopCleaningTracker( UISession uisession ) {
    synchronized( uisession ) {
      FileCleaningTracker tracker = ( FileCleaningTracker )uisession.getAttribute( TRACKER_ATTR );
      if( tracker != null ) {
        tracker.exitWhenFinished();
        uisession.removeAttribute( TRACKER_ATTR );
      }
    }
  }

  private static class FileUploadCleanupHandler implements UISessionListener {
    public void beforeDestroy( UISessionEvent event ) {
      stopCleaningTracker( event.getUISession() );
    }
  }
}
