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

  private static final String DEFAULT_TARGET_FILE_NAME = "upload.tmp";

  private File targetFile;

  public void receive( InputStream dataStream, IFileUploadDetails details ) throws IOException {
    File targetFile = createTargetFile( details );
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
   * @param details the details of the uploaded file like file name, content-type and size
   * @return the file to store the data in
   */
  protected File createTargetFile( IFileUploadDetails details ) throws IOException {
    String fileName = DEFAULT_TARGET_FILE_NAME;
    if( details != null && details.getFileName() != null ) {
      fileName = details.getFileName();
    }
    StringBuilder parentFileName = new StringBuilder( fileName );
    parentFileName.append( "." );
    File parentDir = File.createTempFile( parentFileName.toString(), "" );
    // [ar] by default, a file is created.
    parentDir.delete();
    File targetFile = null;
    if( parentDir.mkdir() ) {
      parentDir.deleteOnExit();
      targetFile = new File( parentDir, fileName );
    } else {
      String prefix = createPrefix( fileName );
      String suffix = createSuffix( fileName );
      targetFile = File.createTempFile( prefix, suffix );
    }
    return targetFile;
  }

  private String createPrefix( String fileName ) {
    int dotIndex = fileName.lastIndexOf( '.' );
    return dotIndex == -1 ? fileName : fileName.substring( 0, dotIndex + 1 );
  }

  private String createSuffix( String fileName ) {
    int dotIndex = fileName.lastIndexOf( '.' );
    return dotIndex == -1 ? null : fileName.substring( dotIndex );
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
