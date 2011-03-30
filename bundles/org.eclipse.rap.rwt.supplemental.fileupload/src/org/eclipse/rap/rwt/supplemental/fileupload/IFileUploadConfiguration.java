/*******************************************************************************
 * Copyright (c) 2002-2011 Innoopract Informationssysteme GmbH and
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
 * Provides a configuration mechanism for the file upload. Note that this
 * configuration is shared for all upload widgets.
 */
public interface IFileUploadConfiguration {

  /**
   *@see FileUploadBase#setFileSizeMax(long)
   */
  public void setFileMaxSize( long fileSizeMax );

  /**
   *@see FileUploadBase#getFileSizeMax()
   */
  public long getFileSizeMax();

  /**
   *@see FileUploadBase#setSizeMax(long)
   */
  public void setSizeMax( long sizeMax );

  /**
   *@see FileUploadBase#getSizeMax()
   */
  public long getSizeMax();
}
