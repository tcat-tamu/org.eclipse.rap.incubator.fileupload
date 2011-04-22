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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.SessionSingletonBase;


public class FileUploadStorage extends SessionSingletonBase {

  private Map items;
  
  public FileUploadStorage() {
    items = new HashMap();
  }
  
  /**
   * Sets a {@link FileUploadStorageItem} or removes an existing one, if the item is null.
   */
  public void setUploadStorageItem( String key, FileUploadStorageItem item ) {
    if( item == null ) {
      items.remove( key );
    } else {
      items.put( key, item );
    }
  }
  
  /**
   * Returns a {@link FileUploadStorageItem} for the given key or null, if not existant
   * in map.
   */
  public FileUploadStorageItem getUploadStorageItem( String key ) {
    return ( FileUploadStorageItem )items.get( key );
  }
  
}
