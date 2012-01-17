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
package org.eclipse.rap.rwt.supplemental.fileupload;


/**
 * Listener for the file upload progress events.
 * <p>Note: This listener is not called from the UI thread.</p>
 *
 * @see FileUploadEvent
 */
public interface FileUploadListener {

  /**
   * Called when new information about an in-progress upload is available.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent#getBytesRead()
   */
  void uploadProgress( FileUploadEvent event );

  /**
   * Called when a file upload has finished successfully.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent
   */
  void uploadFinished( FileUploadEvent event );

  /**
   * Called when a file upload failed.
   *
   * @param event event object that contains information about the uploaded file
   * @see FileUploadEvent#getErrorMessage()
   */
  void uploadFailed( FileUploadEvent event );
}
