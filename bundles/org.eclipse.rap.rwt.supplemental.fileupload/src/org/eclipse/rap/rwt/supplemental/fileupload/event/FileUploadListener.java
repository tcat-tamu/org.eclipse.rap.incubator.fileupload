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

import java.util.EventListener;


/**
 * Use a UploadListener to get notified when a file upload has 
 * finished.
 * 
 * @author tjarodrigues
 * @version $Revision: 1.1 $
 */
public interface FileUploadListener extends EventListener{

  /**
   * Is called, when uploading a file has been finished sucessfully.
   * @param uploadEvent The Upload Event to be fired. 
   * {@link FileUploadEvent#getSource()} returns the upload
   * widget which triggered the event. All other fields are empty
   */
  public void uploadFinished( final FileUploadEvent uploadEvent );
  
  
  /**
   * Is called when the upload is in progress. You may use the
   * {@link FileUploadEvent} to get details on the progress.
   */
  public void uploadInProgress( final FileUploadEvent uploadEvent );


  /**
   * Signals that an exception has ocurred while receiving the
   * file to be uploaded. The exception can be retrieved
   * using {@link FileUploadEvent#getUploadException()}.
   */
  public void uploadException( final FileUploadEvent uploadEvent );
}
