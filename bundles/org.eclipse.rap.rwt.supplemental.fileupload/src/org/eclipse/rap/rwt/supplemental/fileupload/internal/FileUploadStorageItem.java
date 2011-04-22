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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.disk.DiskFileItem;


public class FileUploadStorageItem {

  private DiskFileItem fileItem;
  private File file;
  private String contentType;
  private String uploadProcessId;
  private long bytesRead;
  private long contentLength;
  private Exception exception;

  public FileUploadStorageItem() {
    reset();
  }

  public synchronized InputStream getFileInputStream() throws IOException {
    InputStream fileStream = null;
    if( fileItem != null )
      return this.fileItem.getInputStream();
    return fileStream;
  }

  public synchronized File getFile() throws Exception {
    if( fileItem != null && file == null ) {
      File parent = fileItem.getStoreLocation();
      if( parent == null ) {
        parent = File.createTempFile( String.valueOf( hashCode() ), uploadProcessId );
      } else {
        parent = File.createTempFile( String.valueOf( hashCode() ),
                                      uploadProcessId,
                                      parent.getParentFile() );
      }
      parent.delete();
      parent.mkdirs();
      parent.deleteOnExit();
      if( parent.exists() && parent.isDirectory() ) {
        String clientFileName = new File( fileItem.getName() ).getName();
        file = new File( parent, clientFileName );
        file.deleteOnExit();
        fileItem.write( file );
      }
    }
    return file;
  }

  public synchronized void setFileItem( DiskFileItem item ) {
    fileItem = item;
    setContentType( fileItem.getContentType() );
  }

  private synchronized void setContentType( String contentType ) {
    this.contentType = contentType;
  }

  public synchronized String getContentType() {
    return this.contentType;
  }

  public synchronized void setUploadProcessId( String uploadProcessId ) {
    this.uploadProcessId = uploadProcessId;
  }

  public synchronized String getUploadProcessId() {
    return this.uploadProcessId;
  }

  public synchronized void updateProgress( long bytesRead, long contentLength ) {
    this.bytesRead = bytesRead;
    this.contentLength = contentLength;
  }

  public synchronized long getBytesRead() {
    return bytesRead;
  }

  public synchronized long getContentLength() {
    return contentLength;
  }

  public synchronized void reset() {
    contentLength = -1;
    bytesRead = -1;
    contentType = null;
    fileItem = null;
    file = null;
    exception = null;
  }

  public synchronized void setException( Exception e ) {
    this.exception = e;
  }

  public synchronized Exception getException() {
    return exception;
  }
}
