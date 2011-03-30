/*******************************************************************************
 * Copyright (c) 2002-2011 Innoopract Informationssysteme GmbH and
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
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadConfiguration;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorage;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.FileUploadStorageItem;
import org.eclipse.rap.rwt.supplemental.fileupload.internal.WidgetProgressHandler;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.swt.widgets.Widget;


/**
 * Handles file uploads and upload progress updates. Instances of this class must be 
 * disposed to prevent a registration leak. 
 * <p> 
 * Implementation note: uploaded files are currently stored in the  
 * java.io.tmpdir. See 
 * {@link #handleFileUpload(HttpServletRequest, FileUploadStorageItem)} on
 * how to change this.
 * 
 */
public class FileUploadServiceHandler implements IServiceHandler {

  private static final String REQUEST_PROCESS_ID = "processId";
  private static final String REQUEST_WIDGET_ID = "widgetId";
  
  /**
   * Holds configuration data for the upload widget.
   */
  private final IFileUploadConfiguration uploadConfiguration;
  private final String serviceHandlerId;
  private final FileUploadStorage fileUploadStorage;
  private final WidgetProgressHandler progressHandler;
  
  
  public FileUploadServiceHandler() {
    serviceHandlerId = getServiceHandlerId();
    uploadConfiguration = new FileUploadConfiguration();
    fileUploadStorage = new FileUploadStorage();
    progressHandler = new WidgetProgressHandler();
    RWT.getServiceManager().registerServiceHandler(serviceHandlerId, this);
  }
  
  /**
   * Returns a unique id for this service handler class.
   */
  private String getServiceHandlerId() {
    StringBuilder id = new StringBuilder(FileUploadServiceHandler.class.getName());
    id.append( hashCode() );
    return id.toString();
  }
  
  public void dispose() {
    RWT.getServiceManager().unregisterServiceHandler(serviceHandlerId);
  }
  
  /**
   * Requests to this service handler without a valid session id are ignored for
   * security reasons. The same applies to request with widgetIds which haven't been
   * registered at the session singleton {@link FileUploadStorage}.
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
      if (fileUploadStorageItem == null) {
        fileUploadStorageItem = new FileUploadStorageItem();
        fileUploadStorageItem.setUploadProcessId( uploadProcessId );
        fileUploadStorage.setUploadStorageItem( uploadProcessId, fileUploadStorageItem );
      }
      
      // fileUploadStorageItem can be null, if Upload widget is dispsed!
      if (ServletFileUpload.isMultipartContent(request)) {
        // Handle post-request which contains the file to upload
        handleFileUpload( request, fileUploadStorageItem, uploadProcessId );
      }
      
    }
  }
  
  public void registerWidget( Widget widget, String processId ) {
    progressHandler.register( widget, processId );
  }
  
  public void unregisterWidget( String processId ) {
    progressHandler.unregister( processId );
  }
  
  public long getBytesRead(String processId) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null ? uploadStorageItem.getBytesRead() : 0L;
  }
  
  public long getContentLength(String processId) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null ? uploadStorageItem.getContentLength() : 0L;
  }

  public Exception getException( String processId ) {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    return uploadStorageItem != null ? uploadStorageItem.getException() : null;
  }
  
  public File getUploadedFile( String processId ) throws Exception {
    final FileUploadStorageItem uploadStorageItem = fileUploadStorage.getUploadStorageItem( processId );
    File upload = null;
    if (uploadStorageItem != null) {
      upload = uploadStorageItem.getFile();
    }
    return upload;
  }
  
  /**
   * Treats the request as a post request which contains the file to be
   * uploaded. Uses the apache commons fileupload library to
   * extract the file from the request, attaches a {@link WidgetProgressHandler} to 
   * get notified about the progress and writes the file content
   * to the given {@link FileUploadStorageItem}
   * @param request Request object, must not be null
   * @param fileUploadStorageitem Object where the file content is set to.
   * If null, nothing happens.
   * @param uploadProcessId Each upload action has a unique process identifier to
   * match subsequent polling calls to get the progress correctly to the uploaded file.
   *
   */
  private void handleFileUpload( HttpServletRequest request,
                                 final FileUploadStorageItem fileUploadStorageitem, 
                                 final String uploadProcessId )
  {
    // Ignore upload requests which have no valid processId
    if (fileUploadStorageitem != null && uploadProcessId != null && !"".equals( uploadProcessId )) {
      
      // Reset storage item to clear values from last upload process
      fileUploadStorageitem.reset();
      
      // Create file upload factory and upload servlet
      // You could use new DiskFileItemFactory(threshold, location)
      // to configure a custom in-memory threshold and storage location.
      // By default the upload files are stored in the java.io.tmpdir
      final DiskFileItemFactory factory = new DiskFileItemFactory();
      final ServletFileUpload upload = new ServletFileUpload( factory );
      
      // apply configuration params
      applyConfiguration(upload);
      
      // Create a file upload progress listener
      final ProgressListener listener = new ProgressListener() {

        public void update( long aBytesRead,
                            long aContentLength,
                            int anItem  ) {
          fileUploadStorageitem.updateProgress( aBytesRead, aContentLength );
          progressHandler.updateProgress(fileUploadStorageitem,uploadProcessId);
        }
        
      };
      // Upload servlet allows to set upload listener
      upload.setProgressListener( listener );
      
      DiskFileItem fileItem = null;
      try {
        final List uploadedItems = upload.parseRequest( request );
        // Only one file upload at once is supported. If there are multiple files, take
        // the first one and ignore other
        if ( uploadedItems.size() > 0 ) {
          fileItem = ( DiskFileItem )uploadedItems.get( 0 );
          // Don't check for file size 0 because this prevents uploading new empty office xp documents
          // which have a file size of 0.
          if( !fileItem.isFormField() ) {
            fileUploadStorageitem.setFileItem( fileItem );
          }
        }
      } catch( final FileUploadException e ) {
        fileUploadStorageitem.setException(e);
      } catch( final Exception e ) {
        fileUploadStorageitem.setException(e);
      }
    }
  }

  /**
   * Applies custom configuration parameters specified by the
   * user.
   * @param upload The upload handler to which the config is applied.
   */
  private void applyConfiguration( ServletFileUpload upload ) {
    upload.setFileSizeMax( getConfiguration().getFileSizeMax() );
    upload.setSizeMax( getConfiguration().getSizeMax() );
  }

  /**
   * Builds a url which points to the service handler and encodes the given parameters
   * as url parameters. 
   */
  public String getUrl(String processId) {
    final StringBuffer url = new StringBuffer();
    url.append(URLHelper.getURLString(false));

    URLHelper.appendFirstParam(url, REQUEST_PARAM, getServiceHandlerId());
    URLHelper.appendParam(url, REQUEST_PROCESS_ID, processId);

    // convert to relative URL
    final int firstSlash = url.indexOf( "/" , url.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
    url.delete( 0, firstSlash ); // Result is sth like "/rap?custom_service_handler..."
    return RWT.getResponse().encodeURL(url.toString());
  }
  
  /**
   * Returns a configuration facade.
   */
  public IFileUploadConfiguration getConfiguration() {
    return uploadConfiguration;
  }

  public void cancel( String processId ) {
  }

}
