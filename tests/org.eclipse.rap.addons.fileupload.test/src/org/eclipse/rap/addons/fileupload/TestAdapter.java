/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.fileupload;

import org.eclipse.rap.addons.fileupload.FileUploadHandler;


public final class TestAdapter {

  public static String getTokenFor( FileUploadHandler handler ) {
    return handler.getToken();
  }
}
