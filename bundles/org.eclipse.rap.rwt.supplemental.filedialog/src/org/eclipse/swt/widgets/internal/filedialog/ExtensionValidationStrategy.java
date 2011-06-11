/*******************************************************************************
 * Copyright (c) 2010,2011 Texas Center for Applied Technology
 * Texas Engineering Experiment Station
 * The Texas A&M University System
 * All rights reserved. 
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Austin Riddle (Texas Center for Applied Technology) - 
 *       initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets.internal.filedialog;

public class ExtensionValidationStrategy implements ValidationStrategy {
  
  private String[] filterExtensions;
  private int filterIndex;
  
  public ExtensionValidationStrategy(String[] filterExtensions, int filterIndex) {
    this.filterExtensions = filterExtensions;
    this.filterIndex = filterIndex;
  }
  
  public boolean validate( String filename ) {
    boolean valid = true;
    if( filterExtensions != null && filename.length() > 0 && filterIndex < filterExtensions.length )
    {
      valid = false;
      String filter = filterExtensions[ filterIndex ];
      if( filter != null ) {
        String[] types = filter.split( ";" );
        for( int j = 0; j < types.length; j++ ) {
          String ext = types[ j ].replaceAll( "\\*", "" );
          if( ext.equals( "." ) || filename.endsWith( ext ) ) {
            valid = true;
          }
        }
      }
    }
    return valid;
  }
}