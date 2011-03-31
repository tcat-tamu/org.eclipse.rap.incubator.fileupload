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
package org.eclipse.rap.rwt.supplemental.fileupload.event;

/**
 * Instances of this class represent an error that has occurred with an upload.
 * 
 * @since 1.4
 */
public class FileUploadException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private final String uploadProcessId;

  /**
   * Creates a new exception that pertains to an upload process.
   * 
   * @param uploadProcessId - the upload process id
   * @param nestedException - the nested exception that caused the failure
   * @since 1.4
   */
  public FileUploadException( String uploadProcessId, Exception nestedException ) {
    super( "Upload with process id: " + uploadProcessId + " failed.", nestedException );
    this.uploadProcessId = uploadProcessId;
  }

  /**
   * Returns the upload process id to which this exception pertains.
   * 
   * @return the upload process id
   * @since 1.4
   */
  public String getUploadProcessId() {
    return uploadProcessId;
  }
}
