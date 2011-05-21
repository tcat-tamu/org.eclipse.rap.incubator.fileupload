/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.test;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;


public class TestFileUploadEvent extends FileUploadEvent {

  public TestFileUploadEvent( FileUploadHandler handler ) {
    super( handler );
  }

  private static final long serialVersionUID = 1L;

  public String getFileName() {
    return null;
  }

  public Exception getException() {
    return null;
  }

  public String getContentType() {
    return null;
  }

  public long getContentLength() {
    return 0;
  }

  public long getBytesRead() {
    return 0;
  }
}