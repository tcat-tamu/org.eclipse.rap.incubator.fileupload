/*******************************************************************************
 * Copyright (c) 2002-2007 Critical Software S.A. and
 * Texas Engineering Experiment Station
 * The Texas A&M University System
 * All Rights Reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tiago Rodrigues (Critical Software S.A.) - initial implementation
 *     Joel Oliveira (Critical Software S.A.) - initial commit
 *     Austin Riddle (Texas Center for Applied Technology) - 
 *                   migration to support compatibility with varied upload 
 *                   widget implementations
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.event;

import org.eclipse.rwt.Adaptable;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * Represents an Upload Event.
 *
 * @author tjarodrigues
 * @version $Revision: 1.1 $
 */
public class FileUploadEvent extends TypedEvent {
  
  private static final long serialVersionUID = 1L;
  /**
   * Hopefully, this isn't occupied by another custom event type
   */
  private static final int UPLOAD_FINISHED = 101;
  private static final int UPLOAD_IN_PROGRESS = 102;
  private static final int UPLOAD_EXCEPTION = 103;
  
  private static final Class LISTENER = FileUploadListener.class;
  private final boolean finished;
  private final long uploadedParcial;
  private final long uploadedTotal;
  private final Exception uploadException;

  
  /**
   * Returns an exception that occurred during upload during
   * processing the file on the server side.
   */
  public Exception getUploadException() {
    return uploadException;
  }

  /**
   * Checks if the Upload has finished.
   * 
   * @return <code>True</code> if the Upload has finished, <code>False</code>
   *         otherwise.
   */
  public final boolean isFinished() {
    return finished;
  }

  /**
   * Gets the partial amount of data uploaded.
   *
   * @return The partial amount of data uploaded.
   */
  public final long getUploadedParcial() {
    return this.uploadedParcial;
  }

  /**
   * Gets the total file size.
   *
   * @return The total file size.
   */
  public final long getUploadedTotal() {
    return this.uploadedTotal;
  }

  /**
   * Creates a new instance of the Upload Event.
   *
   * @param finished Indicates if the upload is finished.
   * @param uploadedParcial The partial amount of data uploaded.
   * @param uploadedTotal The total file size.
   * @param widget The sender of the event, must not be null
   */
  public FileUploadEvent( final Widget widget,
                      final boolean finished,
                      final long uploadedParcial,
                      final long uploadedTotal )
  {
    super( widget, finished
                           ? UPLOAD_FINISHED
                           : UPLOAD_IN_PROGRESS );
    this.finished = finished;
    this.uploadedParcial = uploadedParcial;
    this.uploadedTotal = uploadedTotal;
    this.uploadException = null;
  }
  
  public FileUploadEvent( final Widget widget, final Exception uploadException ) {
    super(widget, UPLOAD_EXCEPTION);
    this.finished = true;
    this.uploadedParcial = -1;
    this.uploadedTotal = -1;
    this.uploadException = uploadException;
  }

  protected void dispatchToObserver( final Object listener ) {
    switch( getID() ) {
      case UPLOAD_IN_PROGRESS:
        ( ( FileUploadListener )listener ).uploadInProgress( this );
      break;
      case UPLOAD_FINISHED:
        ( ( FileUploadListener )listener ).uploadFinished( this );
      break;
      case UPLOAD_EXCEPTION:
        ( ( FileUploadListener )listener ).uploadException( this );
      break;
      default:
        throw new IllegalStateException( "Invalid event handler type." );
    }
  }

  protected Class getListenerType() {
    return LISTENER;
  }

  protected boolean allowProcessing() {
    return true;
  }

  public static void addListener( final Adaptable adaptable, 
                                  final FileUploadListener listener )
  {
    addListener( adaptable, LISTENER, listener );
  }

  public static void removeListener( final Adaptable adaptable, 
                                     final FileUploadListener listener )
  {
    removeListener( adaptable, LISTENER, listener );
  }
  
  public static boolean hasListener( final Adaptable adaptable ) {
    return hasListener( adaptable, LISTENER );
  }
  
  public static Object[] getListeners( final Adaptable adaptable ) {
    return getListener( adaptable, LISTENER );
  }
}
