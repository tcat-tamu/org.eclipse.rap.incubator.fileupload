/*******************************************************************************
 * Copyright (c) 2002,2011 Innoopract Informationssysteme GmbH and
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.rwt.supplemental.fileupload.event.FileUploadListener;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadConfiguration;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorage;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorageItem;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadProgressHandler;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;

/**
 * Handles file uploads and upload progress updates. Instances of this class
 * must be disposed to prevent a registration leak.
 * <p>
 * Implementation note: uploaded files are currently stored in the
 * java.io.tmpdir.
 * 
 * @since 1.4
 */
public class FileUploadServiceHandler implements IServiceHandler {

  /**
   * Parameter key for upload process ID.
   */
  private static final String REQUEST_PROCESS_ID = "processId";
  /**
   * Holds configuration data for the uploads.
   */
  private final IFileUploadConfiguration uploadConfiguration;
  private final String serviceHandlerId;
  private final FileUploadStorage fileUploadStorage;
  private final FileUploadProgressHandler progressHandler;

  /**
   * Creates and registers a new service handler for file uploads.
   * 
   * @since 1.4
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
   * Unregisters this service handler. This method must be called to release the
   * service handler registration.
   * 
   * @since 1.4
   */
  public void dispose() {
    RWT.getServiceManager().unregisterServiceHandler( serviceHandlerId );
  }

  /**
   * Requests to this service handler without a valid session id are ignored for
   * security reasons.
   * 
   * @since 1.4
   */
  public void service() throws IOException, ServletException {
    final HttpServletRequest request = RWT.getRequest();
    final String uploadProcessId = request.getParameter( REQUEST_PROCESS_ID );
    final HttpSession session = request.getSession( false );
    if( session != null
        && uploadProcessId != null
        && !"".equals( uploadProcessId ) )
    {
      FileUploadStorageItem fileUploadStorageItem = fileUploadStorage.getUploadStorageItem( uploadProcessId );
      if( fileUploadStorageItem == null ) {
        fileUploadStorageItem = new FileUploadStorageItem();
        fileUploadStorageItem.setUploadProcessId( uploadProcessId );
        fileUploadStorage.setUploadStorageItem( uploadProcessId,
                                                fileUploadStorageItem );
      }
      if( ServletFileUpload.isMultipartContent( request ) ) {
        // Handle post-request which contains the file to upload
        handleFileUpload( request, fileUploadStorageItem, uploadProcessId );
      }
    }
  }

  /**
   * Adds a listener on a specific upload process. Duplicate registrations have
   * no effect.
   * 
   * @param listener - the listener instance to register
   * @param processId - the id of the upload process that the listener will be
   *          notified about
   * @see FileUploadListener
   * @see FileUploadServiceHandler#removeListener(FileUploadListener,String)         
   * @since 1.4
   */
  public void addListener( FileUploadListener listener, String processId ) {
    progressHandler.addListener( listener, processId );
  }

  /**
   * Removes a listener on a specific upload process.
   * 
   * @param listener - the listener instance to unregister
   * @param processId - the id of the upload process that the listener was
   *          registered with
   * @see FileUploadListener         
   * @see FileUploadServiceHandler#addListener(FileUploadListener,String)
   * @since 1.4
   */
  public void removeListener( FileUploadListener listener, String processId ) {
    progressHandler.removeListener( listener, processId );
  }

  /**
   * Returns the number of bytes uploaded for the given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the number of bytes read from the upload stream
   * @since 1.4
   */
  public long getBytesRead( String processId ) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null
                                    ? uploadStorageItem.getBytesRead()
                                    : 0L;
  }

  /**
   * Returns the total number of bytes expected for the uploaded file for the
   * given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the total number of bytes expected from the upload stream
   * @since 1.4
   */
  public long getContentLength( String processId ) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null
                                    ? uploadStorageItem.getContentLength()
                                    : 0L;
  }

  /**
   * Returns an exception (if one has been thrown) from the upload process.
   * 
   * @param processId - the id of the upload process
   * @return - an exception
   * @since 1.4
   */
  public Exception getException( String processId ) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null
                                    ? uploadStorageItem.getException()
                                    : null;
  }

  /**
   * Returns the file reference for the given process id.
   * 
   * @param processId - the id of the upload process
   * @return - the location on disk of the uploaded file
   * @since 1.4
   */
  public File getUploadedFile( String processId ) throws Exception {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    File upload = null;
    if( uploadStorageItem != null ) {
      upload = uploadStorageItem.getFile();
    }
    return upload;
  }

  /**
   * Treats the request as a post request which contains the file to be
   * uploaded. Uses the apache commons fileupload library to extract the file
   * from the request, attaches a {@link FileUploadProgressHandler} to get notified
   * about the progress and writes the file content to the given
   * {@link FileUploadStorageItem}
   * 
   * @param request Request object, must not be null
   * @param fileUploadStorageitem Object where the file content is set to. If
   *          null, nothing happens.
   * @param uploadProcessId Each upload action has a unique process identifier
   *          to match subsequent polling calls to get the progress correctly to
   *          the uploaded file.
   */
  private void handleFileUpload( HttpServletRequest request,
                                 final FileUploadStorageItem fileUploadStorageitem,
                                 final String uploadProcessId )
  {
    // Ignore upload requests which have no valid processId
    if( fileUploadStorageitem != null
        && uploadProcessId != null
        && !"".equals( uploadProcessId ) )
    {
      // Reset storage item to clear values from last upload process
      fileUploadStorageitem.reset();
      // Create file upload factory and upload servlet
      // You could use new DiskFileItemFactory(threshold, location)
      // to configure a custom in-memory threshold and storage location.
      // By default the upload files are stored in the java.io.tmpdir
      final DiskFileItemFactory factory = new DiskFileItemFactory();
      final ServletFileUpload upload = new ServletFileUpload( factory );
      // apply configuration params
      applyConfiguration( upload );
      // Create a file upload progress listener
      final ProgressListener listener = new ProgressListener() {

        public void update( long aBytesRead, long aContentLength, int anItem ) {
          fileUploadStorageitem.updateProgress( aBytesRead, aContentLength );
          progressHandler.updateProgress( fileUploadStorageitem,
                                          uploadProcessId );
        }
      };
      // Upload servlet allows to set upload listener
      upload.setProgressListener( listener );
      DiskFileItem fileItem = null;
      try {
        final List uploadedItems = upload.parseRequest( request );
        // Only one file upload at once is supported. If there are multiple
        // files, take
        // the first one and ignore other
        if( uploadedItems.size() > 0 ) {
          fileItem = ( DiskFileItem )uploadedItems.get( 0 );
          // Don't check for file size 0 because this prevents uploading new
          // empty office xp documents
          // which have a file size of 0.
          if( !fileItem.isFormField() ) {
            fileUploadStorageitem.setFileItem( fileItem );
          }
        }
      } catch( final FileUploadException e ) {
        fileUploadStorageitem.setException( e );
      } catch( final Exception e ) {
        fileUploadStorageitem.setException( e );
      }
    }
  }

  /**
   * Applies custom configuration parameters specified by the user.
   * 
   * @param upload The upload handler to which the config is applied.
   */
  private void applyConfiguration( ServletFileUpload upload ) {
    upload.setFileSizeMax( getConfiguration().getFileSizeMax() );
    upload.setSizeMax( getConfiguration().getSizeMax() );
  }

  /**
   * Builds an encoded url for the given upload process id which points to this
   * service handler.
   * 
   * @param processId - the id of the upload process
   * @return an encoded url that points to this service handler
   * @since 1.4
   */
  public String getUrl( String processId ) {
    final StringBuffer url = new StringBuffer();
    url.append( RWT.getRequest().getContextPath() );
    url.append( RWT.getRequest().getServletPath() );
    url.append( "?" );
    url.append( IServiceHandler.REQUEST_PARAM )
      .append( "=" )
      .append( getServiceHandlerId() );
    url.append( "&" );
    url.append( REQUEST_PROCESS_ID ).append( "=" ).append( processId );
    // convert to relative URL
    // first slash after double slash of "http://"
    final int firstSlash = url.indexOf( "/", url.indexOf( "//" ) + 2 );
    if (firstSlash != -1) {
      url.delete( 0, firstSlash ); // Result is sth like
                                 // "/rap?custom_service_handler..."
    }
    return RWT.getResponse().encodeURL( url.toString() );
  }

  /**
   * Returns a configuration facade.
   * 
   * @return the upload configuation used by this service handler
   * @since 1.4
   */
  public IFileUploadConfiguration getConfiguration() {
    return uploadConfiguration;
  }

  /**
   * Cancels an upload process.
   * 
   * @param processId - the id of the upload process to cancel.
   * @since 1.4
   */
  public void cancel( String processId ) {
    // TODO implement a cancel operation
  }
}
