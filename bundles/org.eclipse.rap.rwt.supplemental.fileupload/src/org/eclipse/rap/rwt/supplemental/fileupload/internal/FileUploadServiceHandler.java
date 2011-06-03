/*******************************************************************************
 * Copyright (c) 2002, 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Austin Riddle (Texas Center for Applied Technology) - migration to support
 *                  compatibility with varied upload widget implementations
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;


public final class FileUploadServiceHandler implements IServiceHandler {

  private static final String PARAMETER_TOKEN = "token";

  static final String SERVICE_HANDLER_ID = "org.eclipse.rap.fileupload";

  public void service() throws IOException, ServletException {
    HttpServletRequest request = RWT.getRequest();
    HttpServletResponse response = RWT.getResponse();
    // TODO [rst] Revise: does this double security make it any more secure?
    // Ignore requests to this service handler without a valid session for security reasons
    boolean hasSession = request.getSession( false ) != null;
    if( hasSession ) {
      String token = request.getParameter( PARAMETER_TOKEN );
      FileUploadHandler registeredHandler = FileUploadHandlerStore.getInstance().getHandler( token );
      if( registeredHandler == null ) {
        String message = "Invalid or missing token";
        response.sendError( HttpServletResponse.SC_FORBIDDEN, message );
      } else if( !"POST".equals( request.getMethod().toUpperCase() ) ) {
        String message = "Only POST requests allowed";
        response.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED, message );
      } else if( !ServletFileUpload.isMultipartContent( request ) ) {
        String message = "Content must be in multipart type";
        response.sendError( HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, message );
      } else {
        FileUploadProcessor processor = new FileUploadProcessor( registeredHandler );
        processor.handleFileUpload( request, response );
      }
    }
  }

  public static String getUrl( String token ) {
    StringBuffer url = new StringBuffer();
    url.append( RWT.getRequest().getContextPath() );
    url.append( RWT.getRequest().getServletPath() );
    url.append( "?" );
    url.append( IServiceHandler.REQUEST_PARAM ).append( "=" ).append( SERVICE_HANDLER_ID );
    url.append( "&" );
    url.append( PARAMETER_TOKEN ).append( "=" ).append( token );
    int relativeIndex = url.lastIndexOf( "/" );
    if( relativeIndex > -1 ) {
      url.delete( 0, relativeIndex + 1 );
    }
    return RWT.getResponse().encodeURL( url.toString() );
  }
}
