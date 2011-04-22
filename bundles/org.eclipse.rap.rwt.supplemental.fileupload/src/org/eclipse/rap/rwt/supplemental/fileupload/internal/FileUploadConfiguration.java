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

  private long maxFileSize = -1;
  private long maxRequestSize = -1;

  public synchronized long getMaxFileSize() {
    return maxFileSize;
  }

  public synchronized void setMaxFileSize( long maxFileSize ) {
    this.maxFileSize = maxFileSize;
  }

  public synchronized long getMaxRequestSize() {
    return maxRequestSize;
  }

  public synchronized void setMaxRequestSize( long maxRequestSize ) {
    this.maxRequestSize = maxRequestSize;
  }
}
