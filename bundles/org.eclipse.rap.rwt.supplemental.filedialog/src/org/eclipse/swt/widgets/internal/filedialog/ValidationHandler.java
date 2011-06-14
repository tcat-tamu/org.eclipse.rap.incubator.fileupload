/*******************************************************************************
 * Copyright (c) 2010, 2011 Texas Center for Applied Technology
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

import java.util.ArrayList;
import java.util.List;

public abstract class ValidationHandler {

  private List strategies;
  private int numUploads;

  public ValidationHandler() {
    strategies = new ArrayList();
  }

  public void addValidationStrategy( ValidationStrategy stratagy ) {
    strategies.add( stratagy );
  }

  public void removeValidationStrategy( ValidationStrategy stratagy ) {
    strategies.remove( stratagy );
  }

  public boolean validate( String text ) {
    boolean validated = true;
    for( int i = 0; i < strategies.size(); i++ ) {
      if( !( ( ValidationStrategy )strategies.get( i ) ).validate( text ) ) {
        validated = false;
      }
    }
    return validated;
  }

  public int getNumUploads() {
    return numUploads;
  }

  public void setNumUploads( int numUploads ) {
    this.numUploads = numUploads;
  }

  public abstract void updateEnablement();

}