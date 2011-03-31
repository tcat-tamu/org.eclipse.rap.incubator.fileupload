/*******************************************************************************
 * Copyright (c) 2002,2011 Innoopract Informationssysteme GmbH and
 * Texas Engineering Experiment Station
 * The Texas A&M University System
 * All Rights Reserved. 
 * 
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Austin Riddle (Texas Center for Applied Technology) - 
 *                   migration to support compatibility with varied upload 
 *                   widget implementations
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload;

import org.apache.commons.fileupload.FileUploadBase;

/**
 * Provides a configuration mechanism for the file upload.
 * 
 * @since 1.4
 */
public interface IFileUploadConfiguration {

  /**
   * Sets the max file size in bytes allowed for an upload.
   * 
   * @param fileSizeMax - the maximum file size allowed
   * @since 1.4
   */
  public void setFileMaxSize( long maxFileSize );

  /**
   * Returns the max file size in bytes allowed for an upload.
   * 
   * @since 1.4
   */
  public long getFileSizeMax();

  /**
   * Sets the max size in bytes allowed for a complete request.
   * 
   * @param maxRequestSize the size in bytes
   * @since 1.4
   */
  public void setSizeMax( long maxRequestSize );

  /**
   * Returns the max size in bytes allowed for a complete request.
   * 
   * @since 1.4
   */
  public long getSizeMax();
}
