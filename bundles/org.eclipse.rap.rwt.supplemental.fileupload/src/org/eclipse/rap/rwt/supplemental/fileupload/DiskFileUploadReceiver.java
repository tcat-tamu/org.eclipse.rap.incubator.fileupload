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
package org.eclipse.rap.rwt.supplemental.fileupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A file upload receiver that stores received files on disk.
 */
public class DiskFileUploadReceiver extends FileUploadReceiver {

  private File targetFile;

  public void receive( InputStream dataStream ) throws IOException {
    File targetFile = createTargetFile();
    FileOutputStream outputStream = new FileOutputStream( targetFile );
    try {
      copy( dataStream, outputStream );
    } finally {
      outputStream.close();
    }
    this.targetFile = targetFile;
  }

  /**
   * Returns the file that the received data has been saved to.
   *
   * @return the target file or <code>null</code> if no file has been stored yet
   */
  public File getTargetFile() {
    return targetFile;
  }

  /**
   * Creates a file to save the received data to. Subclasses may override.
   *
   * @return the file to store the data in
   */
  protected File createTargetFile() throws IOException {
    return File.createTempFile( "upload.", ".tmp" );
  }

  private static void copy( InputStream inputStream, OutputStream outputStream )
    throws IOException
  {
    byte[] buffer = new byte[ 8192 ];
    boolean finished = false;
    while( !finished ) {
      int bytesRead = inputStream.read( buffer );
      if( bytesRead != -1 ) {
        outputStream.write( buffer, 0, bytesRead );
      } else {
        finished = true;
      }
    }
  }
}
