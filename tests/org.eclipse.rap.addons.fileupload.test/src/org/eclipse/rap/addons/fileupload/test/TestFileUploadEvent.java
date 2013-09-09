/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.fileupload.test;

import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;


public class TestFileUploadEvent extends FileUploadEvent {

  public TestFileUploadEvent( FileUploadHandler handler ) {
    super( handler );
  }

  private static final long serialVersionUID = 1L;

  @Override
  public FileDetails[] getFileDetails() {
    return new FileDetails[ 0 ];
  }

  @Override
  public long getContentLength() {
    return 0;
  }

  @Override
  public long getBytesRead() {
    return 0;
  }

  @Override
  public Exception getException() {
    return null;
  }

}