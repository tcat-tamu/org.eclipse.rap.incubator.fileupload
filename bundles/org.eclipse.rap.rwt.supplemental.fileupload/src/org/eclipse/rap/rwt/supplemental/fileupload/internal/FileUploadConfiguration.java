/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Austin Riddle (Texas Center for Applied Technology) - migration to support
 *                  compatibility with varied upload widget implementations
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import org.eclipse.rap.rwt.supplemental.fileupload.IFileUploadConfiguration;


public class FileUploadConfiguration implements IFileUploadConfiguration {

  private long fileSizeMax = -1;
  private long sizeMax = -1;

  public synchronized long getFileSizeMax() {
    return fileSizeMax;
  }

  public synchronized long getSizeMax() {
    return sizeMax;
  }

  public synchronized void setFileMaxSize( final long fileSizeMax ) {
    this.fileSizeMax = fileSizeMax;
  }

  public synchronized void setSizeMax( final long sizeMax ) {
    this.sizeMax = sizeMax;
  }
}
