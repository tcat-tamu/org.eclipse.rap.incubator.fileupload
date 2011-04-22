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


/**
 * Instances of this class are used to notify listeners about the progress of a
 * file upload.
 * 
 * @see FileUploadListener
 * @since 1.4
 */
public class FileUploadEvent {

  private final String uploadProcessId;
  private final long bytesRead;
  private final long totalBytes;
  private final FileUploadException uploadException;
  private final String contentType;
  
  /**
   * Creates an upload event that contains progress information about an upload
   * process. Any exception specified is wrapped in a FileUploadException.
   * 
   * @param uploadedProcessId - the process id of the upload to which this event
   *          pertains
   * @param uploadedParcial - the amount of data uploaded thusfar
   * @param uploadedTotal - the total number of bytes expected for the uploaded
   *          file
   * @param contentType 
   * @param uploadException - an exception regarding upload failure, may be
   *          <code>null</code>
   * @since 1.4
   */
  public FileUploadEvent( String uploadProcessId,
                          long uploadedParcial,
                          long uploadedTotal,
                          String contentType, 
                          Exception uploadException )
  {
    this.uploadProcessId = uploadProcessId;
    this.bytesRead = uploadedParcial;
    this.totalBytes = uploadedTotal;
    this.contentType = contentType;
    this.uploadException = new FileUploadException( uploadProcessId, uploadException );
  }

  /**
   * Returns the process id of the upload to which this event pertains.
   * 
   * @return the upload process id
   * @since 1.4
   */
  public String getUploadProcessId() {
    return uploadProcessId;
  }

  /**
   * Returns an exception if one occurred during the upload processing on the
   * server side.
   * 
   * @return an upload exception
   * @since 1.4
   */
  public FileUploadException getUploadException() {
    return uploadException;
  }

  /**
   * Returns the number of bytes read thusfar in the upload process.
   * 
   * @return the amount of data uploaded
   * @since 1.4
   */
  public final long getBytesRead() {
    return this.bytesRead;
  }

  /**
   * Returns the total number of bytes expected for the file being uploaded.
   * 
   * @return he total file size
   * @since 1.4
   */
  public final long getTotalBytes() {
    return this.totalBytes;
  }
  
  /**
   * Returns the content type of the file being uploaded.
   * 
   * @return the content type
   * @since 1.4
   */
  public String getContentType() {
    return contentType;
  }

}
