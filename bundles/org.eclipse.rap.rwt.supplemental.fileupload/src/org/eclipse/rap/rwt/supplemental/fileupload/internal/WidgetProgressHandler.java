/*******************************************************************************
 * Copyright (c) 2011 Texas Engineering Experiment Station
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.supplemental.fileupload.event.FileUploadEvent;
import org.eclipse.swt.widgets.Widget;

public class WidgetProgressHandler {

  private final Map widgetMappings;

  /**
   * Creates a new File Upload Listener.
   */
  public WidgetProgressHandler() {
    super();
    widgetMappings = new HashMap();
  }

  public void register( Widget widget, String processId ) {
    widgetMappings.put( processId, widget );
  }

  public void unregister( String processId ) {
    widgetMappings.remove( processId );
  }

  public void updateProgress( final FileUploadStorageItem fileUploadStorageitem,
                              final String uploadProcessId )
  {
    final Widget widget = ( Widget )widgetMappings.get( uploadProcessId );
    if( widget != null && !widget.isDisposed() ) {
      final long bytesRead = fileUploadStorageitem.getBytesRead();
      final long contentLength = fileUploadStorageitem.getContentLength();
      widget.getDisplay().asyncExec( new Runnable() {

        public void run() {
          final FileUploadEvent evt;
          Exception uploadException = fileUploadStorageitem.getException();
          if( uploadException != null ) {
            evt = new FileUploadEvent( widget, uploadException );
          } else {
            evt = new FileUploadEvent( widget,
                                       Boolean.valueOf( bytesRead == contentLength )
                                         .booleanValue(),
                                       bytesRead,
                                       contentLength );
          }
          evt.processEvent();
        }
      } );
    }
  }
}
