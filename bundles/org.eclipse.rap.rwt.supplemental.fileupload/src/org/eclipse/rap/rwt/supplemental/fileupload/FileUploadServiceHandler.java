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
package org.eclipse.rap.rwt.supplemental.fileupload;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.rwt.supplemental.fileupload.event.FileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadConfiguration;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadProgressHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorage;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorageItem;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;


/**
 * Handles file uploads and upload progress updates. Instances of this class must be disposed to
 * prevent a registration leak.
 * <p>
 * Implementation note: uploaded files are currently stored in the java.io.tmpdir.
 * </p>
 * 
 * @since 1.4
 */
public class FileUploadServiceHandler implements IServiceHandler {

  private static final String PARAMETER_UPLOAD_PROCESS_ID = "processId";
  private final IFileUploadConfiguration uploadConfiguration;
  private final String serviceHandlerId;
  private final FileUploadStorage fileUploadStorage;
  private final FileUploadProgressHandler progressHandler;
  private boolean disposed;

  /**
   * Creates and registers a new service handler for file uploads. Instances of this service handler
   * must be disposed to prevent a registration leak.
   */
  public FileUploadServiceHandler() {
    serviceHandlerId = getServiceHandlerId();
    uploadConfiguration = new FileUploadConfiguration();
    fileUploadStorage = new FileUploadStorage();
    progressHandler = new FileUploadProgressHandler();
    RWT.getServiceManager().registerServiceHandler( serviceHandlerId, this );
  }

  /**
   * Returns a unique id for this service handler.
   */
  private String getServiceHandlerId() {
    StringBuffer id = new StringBuffer( FileUploadServiceHandler.class.getName() );
    id.append( hashCode() );
    return id.toString();
  }

  /**
   * Unregisters this service handler. This method must be called to release the service handler
   * registration.
   */
  public void dispose() {
    disposed = true;
    RWT.getServiceManager().unregisterServiceHandler( serviceHandlerId );
  }

  /**
   * Checks if this service handler is disposed.
   * 
   * @return <code>true</code> if handler is disposed, else <code>false</code>
   */
  public boolean isDisposed() {
    return disposed;
  }

  /**
   * Requests to this service handler without a valid session id are ignored for security reasons.
   */
  public void service() throws IOException, ServletException {
    HttpServletRequest request = RWT.getRequest();
    String uploadProcessId = request.getParameter( PARAMETER_UPLOAD_PROCESS_ID );
    HttpSession session = request.getSession( false );
    if( session != null && uploadProcessId != null && !"".equals( uploadProcessId ) ) {
      FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( uploadProcessId );
      if( storageItem == null ) {
        storageItem = new FileUploadStorageItem();
        storageItem.setUploadProcessId( uploadProcessId );
        fileUploadStorage.setUploadStorageItem( uploadProcessId, storageItem );
      }
      if( ServletFileUpload.isMultipartContent( request ) ) {
        // Handle post-request which contains the file to upload
        handleFileUpload( request, storageItem, uploadProcessId );
      }
    }
  }

  /**
   * Adds a listener on a specific upload process. Duplicate registrations have no effect.
   * 
   * @param listener - the listener instance to register
   * @param processId - the id of the upload process that the listener will be notified about
   * @see FileUploadListener
   * @see FileUploadServiceHandler#removeListener(FileUploadListener,String)
   */
  public void addListener( FileUploadListener listener, String processId ) {
    progressHandler.addListener( listener, processId );
  }

  /**
   * Removes a listener on a specific upload process.
   * 
   * @param listener - the listener instance to unregister
   * @param processId - the id of the upload process that the listener was registered with
   * @see FileUploadListener
   * @see FileUploadServiceHandler#addListener(FileUploadListener,String)
   */
  public void removeListener( FileUploadListener listener, String processId ) {
    progressHandler.removeListener( listener, processId );
  }

  /**
   * Returns the number of bytes uploaded for the given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the number of bytes read from the upload stream
   */
  public long getBytesRead( String processId ) {
    FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( processId );
    return storageItem != null ? storageItem.getBytesRead() : 0L;
  }

  /**
   * Returns the total number of bytes expected for the uploaded file for the given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the total number of bytes expected from the upload stream
   */
  public long getContentLength( String processId ) {
    FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( processId );
    return storageItem != null ? storageItem.getContentLength() : 0L;
  }

  /**
   * Returns an exception (if one has been thrown) from the upload process.
   * 
   * @param processId - the id of the upload process
   * @return - an exception
   */
  public Exception getException( String processId ) {
    FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( processId );
    return storageItem != null ? storageItem.getException() : null;
  }

  /**
   * Returns the file reference for the given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the location on disk of the uploaded file
   */
  public File getUploadedFile( String processId ) throws Exception {
    FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( processId );
    File result = null;
    if( storageItem != null ) {
      result = storageItem.getFile();
    }
    return result;
  }

  /**
   * Treats the request as a post request which contains the file to be uploaded. Uses the apache
   * commons fileupload library to extract the file from the request, attaches a
   * {@link FileUploadProgressHandler} to get notified about the progress and writes the file
   * content to the given {@link FileUploadStorageItem}
   * 
   * @param request Request object, must not be null
   * @param storageItem Object where the file content is set to. If null, nothing happens.
   * @param uploadProcessId Each upload action has a unique process identifier to match subsequent
   *          polling calls to get the progress correctly to the uploaded file.
   */
  private void handleFileUpload( HttpServletRequest request,
                                 FileUploadStorageItem storageItem,
                                 String uploadProcessId )
  {
    // Ignore upload requests which have no valid processId
    if( storageItem != null && uploadProcessId != null && !"".equals( uploadProcessId ) ) {
      // Reset storage item to clear values from last upload process
      storageItem.reset();
      // Create file upload factory and upload servlet
      // You could use new DiskFileItemFactory(threshold, location)
      // to configure a custom in-memory threshold and storage location.
      // By default the upload files are stored in the java.io.tmpdir
      DiskFileItemFactory factory = new DiskFileItemFactory();
      ServletFileUpload upload = new ServletFileUpload( factory );
      // apply configuration params
      applyConfiguration( upload );
      // Create a file upload progress listener
      final FileUploadStorageItem copyOfStorageItem = storageItem;
      final String copyOfStorageId = uploadProcessId;
      ProgressListener listener = new ProgressListener() {

        public void update( long aBytesRead, long aContentLength, int anItem ) {
// Note: Apache fileupload 1.2 will throw an exception after the upload is finished.
// https://issues.apache.org/jira/browse/FILEUPLOAD-145
// So we handle the file size violation as best we can from here.
          long fileSizeMax = getConfiguration().getFileSizeMax();
          if( fileSizeMax != -1 && aContentLength > fileSizeMax ) {
            handleException( copyOfStorageItem,
                             copyOfStorageId,
                             new RuntimeException( "File exceeds maximum allowed size." ) );
          } else {
            copyOfStorageItem.updateProgress( aBytesRead, aContentLength );
            progressHandler.updateProgress( copyOfStorageItem, copyOfStorageId );
          }
        }
      };
      // Upload servlet allows to set upload listener
      upload.setProgressListener( listener );
      DiskFileItem fileItem = null;
      try {
        List uploadedItems = upload.parseRequest( request );
        // Only one file upload at once is supported. If there are multiple
        // files, take
        // the first one and ignore other
        if( uploadedItems.size() > 0 ) {
          fileItem = ( DiskFileItem )uploadedItems.get( 0 );
          // Don't check for file size 0 because this prevents uploading new
          // empty office xp documents
          // which have a file size of 0.
          if( !fileItem.isFormField() ) {
            storageItem.setFileItem( fileItem );
          }
        }
      } catch( Exception e ) {
// Note: Apache fileupload 1.2 will throw an exception after the upload is finished.
// https://issues.apache.org/jira/browse/FILEUPLOAD-145
        handleException( storageItem, uploadProcessId, e );
      }
    }
  }

  private void handleException( FileUploadStorageItem storageItem,
                                String uploadProcessId,
                                Exception exception )
  {
    storageItem.setException( exception );
    progressHandler.updateProgress( storageItem, uploadProcessId );
  }

  /**
   * Applies custom configuration parameters specified by the user.
   * 
   * @param upload The upload handler to which the config is applied.
   */
  private void applyConfiguration( ServletFileUpload upload ) {
    IFileUploadConfiguration configuration = getConfiguration();
    upload.setFileSizeMax( configuration.getFileSizeMax() );
    upload.setSizeMax( configuration.getSizeMax() );
  }

  /**
   * Builds an encoded url for the given upload process id which points to this service handler.
   * 
   * @param processId - the id of the upload process
   * @return an encoded url that points to this service handler
   */
  public String getUrl( String processId ) {
    StringBuffer url = new StringBuffer();
    url.append( RWT.getRequest().getContextPath() );
    url.append( RWT.getRequest().getServletPath() );
    url.append( "?" );
    url.append( IServiceHandler.REQUEST_PARAM ).append( "=" ).append( getServiceHandlerId() );
    url.append( "&" );
    url.append( PARAMETER_UPLOAD_PROCESS_ID ).append( "=" ).append( processId );
    // convert to relative URL
    // first slash after double slash of "http://"
    int firstSlash = url.indexOf( "/", url.indexOf( "//" ) + 2 );
    if( firstSlash != -1 ) {
      url.delete( 0, firstSlash ); // Result is sth like
      // "/rap?custom_service_handler..."
    }
    return RWT.getResponse().encodeURL( url.toString() );
  }

  /**
   * Returns a configuration facade.
   * 
   * @return the upload configuation used by this service handler
   */
  public IFileUploadConfiguration getConfiguration() {
    return uploadConfiguration;
  }

  /**
   * Cancels an upload process.
   * 
   * @param processId - the id of the upload process to cancel.
   */
  public void cancel( String processId ) {
    // Handling to actually stop the upload in still needed.
    progressHandler.clearListeners( processId );
    FileUploadStorageItem storageItem = fileUploadStorage.getUploadStorageItem( processId );
    // Reset storage item to clear values from last upload process
    if( storageItem != null ) {
      storageItem.reset();
    }
    fileUploadStorage.setUploadStorageItem( processId, null );
  }
}
