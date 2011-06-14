/*****************************************************************************************
 * Copyright (c) 2010, 2011 Texas Center for Applied Technology
 * Texas Engineering Experiment Station
 * The Texas A&M University System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Austin Riddle (Texas Center for Applied Technology) - initial API and implementation
 *    EclipseSource - ongoing development
 *****************************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.io.File;

import org.eclipse.rap.rwt.supplemental.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.supplemental.fileupload.IFileUploadListener;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

public class UploadPanel extends Composite implements IFileUploadListener {

  public static final int COMPACT = 1;
  public static final int FULL = 2;
  public static final int REMOVEABLE = 4;
  public static final int PROGRESS = 8;
  private final int panelStyle;
  private final FileUploadHandler handler;
  private ValidationHandler validationHandler;
  private ProgressCollector progressCollector;
  private FileUpload browseBtn;
  private Text fileText;
  private ProgressBar progressBar;
  private Label progressLabel;
  private Button removeBtn;
  private boolean inProgress;
  private File uploadedFile;
  private boolean autoUpload;
  private Image deleteImage;

  public UploadPanel( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    panelStyle = style;
    FileUploadReceiver receiver = new DiskFileUploadReceiver();
    handler = new FileUploadHandler( receiver );
    createChildren();
  }

  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    browseBtn.addSelectionListener( listener );
  }

  public void setValidationHandler( ValidationHandler validationHandler ) {
    this.validationHandler = validationHandler;
  }

  public void setEnabled( boolean enabled ) {
    checkWidget();
    super.setEnabled( enabled );
    browseBtn.setEnabled( enabled );
    fileText.setEnabled( enabled );
    if( removeBtn != null ) {
      removeBtn.setEnabled( enabled );
    }
  }

  public boolean isFinished() {
    return false;
  }

  public String getSelectedFilename() {
    checkWidget();
    return fileText.getText();
  }

  public File getUploadedFile() {
    return uploadedFile;
  }

  public void startUpload() {
    checkWidget();
    inProgress = true;
    String url = handler.getUploadUrl();
    handler.addUploadListener( this );
    browseBtn.submit( url );
  }

  public void dispose() {
    handler.removeUploadListener( this );
    handler.dispose();
    super.dispose();
  }

  public void setProgressCollector( ProgressCollector progressCollector ) {
    this.progressCollector = progressCollector;
  }

  public void setAutoUpload( boolean autoUpload ) {
    this.autoUpload = autoUpload;
  }

  public boolean isStarted() {
    return inProgress;
  }

  static int checkStyle( int style ) {
    int mask = COMPACT | FULL | REMOVEABLE | PROGRESS;
    return style & mask;
  }

  private boolean hasStyle( int testStyle ) {
    return ( panelStyle & ( testStyle ) ) != 0;
  }

  private void createChildren() {
    GridLayout layout = new GridLayout( 5, false );
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    setLayout( layout );
    browseBtn = new FileUpload( this, SWT.NONE );
    browseBtn.setText( "Browse" );
    browseBtn.setToolTipText( "Browse to select a single file" );
    browseBtn.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event ) {
        String filename = browseBtn.getFileName();
        fileText.setText( filename );
        validate();
        if( autoUpload ) {
          startUpload();
        }
      }
    } );
    fileText = new Text( this, SWT.BORDER );
    fileText.setToolTipText( "Selected file" );
    fileText.setEditable( false );
    if( hasStyle( PROGRESS ) ) {
      progressBar = new ProgressBar( this, SWT.HORIZONTAL | SWT.SMOOTH );
      progressBar.setToolTipText( "Upload progress" );
      progressBar.setMinimum( 0 );
      progressBar.setMaximum( 100 );
      progressLabel = new Label( this, SWT.NONE );
      progressLabel.setText( progressBar.getSelection() + "%" );
    }
    if( hasStyle( REMOVEABLE ) ) {
      removeBtn = new Button( this, SWT.PUSH );
      Image removeIcon = Display.getCurrent().getSystemImage( SWT.ICON_CANCEL );
      removeBtn.setImage( removeIcon );
      if( deleteImage == null ) {
        deleteImage = Graphics.getImage( "resources/delete_obj.gif", getClass().getClassLoader() );
      }
      removeBtn.setImage( deleteImage );
      removeBtn.setToolTipText( "Remove item" );
      removeBtn.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          if( progressCollector != null ) {
            progressCollector.updateProgress( handler, 0 );
          }
          dispose();
        }
      } );
    }
    layoutChildren();
  }

  private void layoutChildren() {
    checkWidget();
    if( hasStyle( COMPACT ) ) {
      browseBtn.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
      GridData textLayoutData = new GridData( SWT.FILL, SWT.FILL, true, false );
      textLayoutData.minimumWidth = 186;
      fileText.setLayoutData( textLayoutData );
      if( progressBar != null ) {
        GridData progressLayoutData = new GridData( SWT.FILL, SWT.FILL, false, false );
        progressLayoutData.minimumWidth = 48;
        progressLayoutData.widthHint = 128;
        progressBar.setLayoutData( progressLayoutData );
        GridData lblLayoutData = new GridData( SWT.FILL, SWT.FILL, false, false );
        float avgCharWidth = Graphics.getAvgCharWidth( progressLabel.getFont() );
        lblLayoutData.minimumWidth = ( int )avgCharWidth * 6;
        lblLayoutData.widthHint = ( int )avgCharWidth * 6;
        progressLabel.setLayoutData( lblLayoutData );
      }
      if( removeBtn != null ) {
        removeBtn.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
      }
    } else {
      browseBtn.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
      GridData textLayoutData = new GridData( SWT.FILL, SWT.FILL, true, false );
      textLayoutData.minimumWidth = 186;
      textLayoutData.horizontalSpan = 4;
      fileText.setLayoutData( textLayoutData );
      if( progressBar != null ) {
        GridData progressLayoutData = new GridData( SWT.FILL, SWT.FILL, true, false );
        progressLayoutData.horizontalSpan = 4;
        progressBar.setLayoutData( progressLayoutData );
        GridData lblLayoutData = new GridData( SWT.FILL, SWT.FILL, false, false );
        float avgCharWidth = Graphics.getAvgCharWidth( progressLabel.getFont() );
        lblLayoutData.minimumWidth = ( int )avgCharWidth * 6;
        lblLayoutData.widthHint = ( int )avgCharWidth * 6;
        progressLabel.setLayoutData( lblLayoutData );
      }
      if( removeBtn != null ) {
        removeBtn.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
      }
    }
  }

  public void validate() {
    if( validationHandler == null || validationHandler.validate( fileText.getText() ) ) {
      fileText.setToolTipText( "Selected file" );
      // TODO replace this with something from theming
      fileText.setBackground( null );
    } else {
      fileText.setToolTipText( "Warning: Selected file does not match filter" );
      // TODO replace this with something from theming
      fileText.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_YELLOW ) );
      validationHandler.updateEnablement();
    }
  }

  public void uploadProgress( final FileUploadEvent uploadEvent ) {
    // checkWidget();
    browseBtn.getDisplay().asyncExec( new Runnable() {
      public void run() {
        double fraction = uploadEvent.getBytesRead() / ( double )uploadEvent.getContentLength();
        int percent = ( int )Math.floor( fraction * 100 );
        if( progressBar != null && !progressBar.isDisposed() ) {
          progressBar.setSelection( percent );
          progressBar.setToolTipText( "Upload progress: " + percent + "%" );
          progressLabel.setText( percent + "%" );
        }
        // allow the uploadFinished call to notify collector of 100% progress since
        //the file is actually written then
        if( progressCollector != null && percent < 100 ) {
          progressCollector.updateProgress( handler, percent );
        }
      }
    } );
  }

  public void uploadFinished( final FileUploadEvent uploadEvent ) {
    // checkWidget();
    DiskFileUploadReceiver receiver = ( DiskFileUploadReceiver )handler.getReceiver();
    uploadedFile = receiver.getTargetFile();
    browseBtn.getDisplay().asyncExec( new Runnable() {
      public void run() {
        int percent = 100;
        if( progressBar != null && !progressBar.isDisposed() ) {
          progressBar.setSelection( percent );
          progressBar.setToolTipText( "Upload progress: " + percent + "%" );
          progressLabel.setText( percent + "%" );
        }
        if( progressCollector != null ) {
          progressCollector.updateProgress( handler, percent );
        }
      }
    } );
  }

  public void uploadFailed( final FileUploadEvent uploadEvent ) {
    // checkWidget();
    browseBtn.getDisplay().asyncExec( new Runnable() {
      public void run() {
        if( progressBar != null && !progressBar.isDisposed() ) {
          progressBar.setState( SWT.ERROR );
          progressBar.setToolTipText( uploadEvent.getException().getMessage() );
        }
      }
    } );
  }
}