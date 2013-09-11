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
package org.eclipse.rap.addons.fileupload.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;


final class FileUploadProcessor {

  private final FileUploadHandler handler;
  private final FileUploadTracker tracker;
  private final CleaningTracker cleaningTracker;

  FileUploadProcessor( FileUploadHandler handler ) {
    this.handler = handler;
    tracker = new FileUploadTracker( handler );
    cleaningTracker = new CleaningTracker();
  }

  void handleFileUpload( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    try {
      List<FileItem> fileItems = readUploadedFileItems( request );
      if( !fileItems.isEmpty() ) {
        FileUploadReceiver receiver = handler.getReceiver();
        for( FileItem fileItem : fileItems ) {
          String fileName = stripFileName( fileItem.getName() );
          String contentType = fileItem.getContentType();
          long contentLength = fileItem.getSize();
          FileDetails details = new FileDetailsImpl( fileName, contentType, contentLength );
          receiver.receive( fileItem.getInputStream(), details );
          tracker.addFile( details );
        }
        tracker.handleFinished();
      } else {
        String errorMessage = "No file upload data found in request";
        tracker.setException( new Exception( errorMessage ) );
        tracker.handleFailed();
        response.sendError( HttpServletResponse.SC_BAD_REQUEST, errorMessage );
      }
    } catch( FileSizeLimitExceededException exception ) {
      tracker.setException( exception );
      tracker.handleFailed();
      response.sendError( HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, exception.getMessage() );
    } catch( Exception exception ) {
      tracker.setException( exception );
      tracker.handleFailed();
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage() );
    }
    cleaningTracker.deleteTemporaryFiles();
  }

  private List<FileItem> readUploadedFileItems( HttpServletRequest request )
    throws FileUploadException
  {
    List<FileItem> result = new ArrayList<FileItem>();
    ServletFileUpload upload = createUpload();
    List uploadedItems = upload.parseRequest( request );
    for( Object uploadedItem : uploadedItems ) {
      FileItem fileItem = ( FileItem )uploadedItem;
      // Don't check for file size == 0 because this would prevent uploading empty files
      if( !fileItem.isFormField() ) {
        result.add( fileItem );
      }
    }
    return result;
  }

  private ServletFileUpload createUpload() {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setFileCleaningTracker( cleaningTracker );
    ServletFileUpload upload = new ServletFileUpload( factory );
    upload.setFileSizeMax( handler.getMaxFileSize() );
    upload.setProgressListener( createProgressListener() );
    return upload;
  }

  private ProgressListener createProgressListener() {
    ProgressListener result = new ProgressListener() {
      long prevTotalBytesRead = -1;
      public void update( long totalBytesRead, long contentLength, int item ) {
        // Depending on the servlet engine and other environmental factors,
        // this listener may be notified for every network packet, so don't notify unless there
        // is an actual increase.
        if ( totalBytesRead > prevTotalBytesRead ) {
          prevTotalBytesRead = totalBytesRead;
          tracker.setContentLength( contentLength );
          tracker.setBytesRead( totalBytesRead );
          tracker.handleProgress();
        }
      }
    };
    return result;
  }

  private static String stripFileName( String name ) {
    String result = name;
    int lastSlash = result.lastIndexOf( '/' );
    if( lastSlash != -1 ) {
      result = result.substring( lastSlash + 1 );
    } else {
      int lastBackslash = result.lastIndexOf( '\\' );
      if( lastBackslash != -1 ) {
        result = result.substring( lastBackslash + 1 );
      }
    }
    return result;
  }

}
