/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.addons.fileupload.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;


public class CleaningTracker extends FileCleaningTracker {

  private final List<String> filesToDelete;
  private final List<String> deleteFailures;

  public CleaningTracker() {
    filesToDelete = new ArrayList<String>();
    deleteFailures = new ArrayList<String>();
  }

  @Override
  public void track( File file, Object marker ) {
    filesToDelete.add( file.getAbsolutePath() );
  }

  @Override
  public void track( File file, Object marker, FileDeleteStrategy deleteStrategy ) {
    filesToDelete.add( file.getAbsolutePath() );
  }

  @Override
  public void track( String path, Object marker ) {
    filesToDelete.add( path );
  }

  @Override
  public void track( String path, Object marker, FileDeleteStrategy deleteStrategy ) {
    filesToDelete.add( path );
  }

  @Override
  public int getTrackCount() {
    return filesToDelete.size();
  }

  @Override
  public List<String> getDeleteFailures() {
    return deleteFailures;
  }

  @Override
  public void exitWhenFinished() {
    deleteTemporaryFiles();
  }

  public void deleteTemporaryFiles() {
    for( String path : filesToDelete ) {
      File file = new File( path );
      if( !file.delete() ) {
        deleteFailures.add( path );
      }
    }
    filesToDelete.clear();
  }

}
