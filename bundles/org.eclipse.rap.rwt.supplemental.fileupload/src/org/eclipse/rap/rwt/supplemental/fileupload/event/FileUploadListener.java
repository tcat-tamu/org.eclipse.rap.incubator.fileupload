/*******************************************************************************
 * Copyright (c) 2002, 2011 Critical Software S.A. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Tiago Rodrigues (Critical Software S.A.) - initial implementation
 *    Joel Oliveira (Critical Software S.A.) - initial commit
 *    Austin Riddle (Texas Center for Applied Technology) - migration to support
 *                  compatibility with varied upload widget implementations
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.event;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadServiceHandler;


/**
 * Listener interface for obtaining progress about particular upload processes. Instances of this
 * class may be registered using an instanceof FileUploadServiceHandler.
 * 
 * @see FileUploadEvent
 * @see FileUploadServiceHandler#addListener(FileUploadListener,String)
 * @see FileUploadServiceHandler#removeListener(FileUploadListener,String)
 * @since 1.4
 */
public interface FileUploadListener {

  /**
   * Called when a file upload has finished sucessfully.
   * 
   * @param uploadEvent - event that contains information about the uploaded file.
   * @see FileUploadEvent
   */
  public void uploadFinished( FileUploadEvent uploadEvent );

  /**
   * Called when new information about an in-progress upload is available.
   * 
   * @param uploadEvent - event that contains information about the uploaded file.
   * @see FileUploadEvent
   */
  public void uploadInProgress( FileUploadEvent uploadEvent );

  /**
   * Called when an exception has ocurred during an upload process. The exception can be retrieved
   * using {@link FileUploadEvent#getUploadException()}.
   * 
   * @see FileUploadEvent
   */
  public void uploadException( FileUploadEvent uploadEvent );
}
