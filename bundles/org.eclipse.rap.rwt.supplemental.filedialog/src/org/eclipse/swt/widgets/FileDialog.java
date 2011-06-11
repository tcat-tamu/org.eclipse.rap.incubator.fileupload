/*******************************************************************************
 * Copyright (c) 2000,2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Austin Riddle (Texas Center for Applied Technology) - RAP implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.internal.filedialog.ExtensionValidationStrategy;
import org.eclipse.swt.widgets.internal.filedialog.ProgressCollector;
import org.eclipse.swt.widgets.internal.filedialog.UploadPanel;
import org.eclipse.swt.widgets.internal.filedialog.ValidationHandler;

/**
 * Instances of this class allow the user to navigate the file system and select
 * a file name. The selected file will be uploaded to the server and the path
 * made available as the result of the dialog.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd><!--SAVE, OPEN,--> MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * The OPEN style is applied by default and setting any other styles has no
 * effect. <!--Note: Only one of the styles SAVE and OPEN may be specified.-->
 * </p>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#filedialog">FileDialog
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public class FileDialog extends Dialog {
  
  // RAP implementation fields taken from JFace
  private final static int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;
  private final static int BUTTON_WIDTH = 61;
  // SWT FileDialog API fields
  String[] filterNames = new String[ 0 ];
  String[] filterExtensions = new String[ 0 ];
  String[] fileNames = new String[ 0 ];
  String filterPath = "";
  String fileName;
  int filterIndex;
  boolean overwrite = false;
  // RAP implementation fields
  private Shell shell;
  private java.util.List uploadPanels;
  private Button okButton;
  private ProgressBar totalProgressBar;
  private Combo filterSelector;
  private Button addFileSelectorBtn;
  private Composite scrollChild;
  private Composite uploadsWrapper;
  private ScrolledComposite uploadScroller;
  private boolean autoUpload;
  private boolean uploadLocked;
  private ProgressCollector progressCollector;
  private ValidationHandler validationHandler;
  private Image addImage;

  /**
   * Constructs a new instance of this class given only its parent.
   * 
   * @param parent a shell which will be the parent of the new instance
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   */
  public FileDialog( Shell parent ) {
    this( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
  }

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   * 
   * @param parent a shell which will be the parent of the new instance
   * @param style the style of dialog to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   */
  public FileDialog( Shell parent, int style ) {
    super( parent, style );
    shell = parent; // HACK save this for later
    checkSubclass();
  }

  /**
   * Returns the path of the first file that was selected in the dialog relative
   * to the filter path, or an empty string if no such file has been selected.
   * 
   * @return the relative path of the file
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns a (possibly empty) array with the paths of all files that were
   * selected in the dialog relative to the filter path.
   * 
   * @return the relative paths of the files
   */
  public String[] getFileNames() {
    return fileNames;
  }

  /**
   * Returns the file extensions which the dialog will use to filter the files
   * it shows.
   * 
   * @return the file extensions filter
   */
  public String[] getFilterExtensions() {
    return filterExtensions;
  }

  /**
   * Get the 0-based index of the file extension filter which was selected by
   * the user, or -1 if no filter was selected.
   * <p>
   * This is an index into the FilterExtensions array and the FilterNames array.
   * </p>
   * 
   * @return index the file extension filter index
   * @see #getFilterExtensions
   * @see #getFilterNames
   */
  public int getFilterIndex() {
    return filterIndex;
  }

  /**
   * Returns the names that describe the filter extensions which the dialog will
   * use to filter the files it shows.
   * 
   * @return the list of filter names
   */
  public String[] getFilterNames() {
    return filterNames;
  }

  /**
   * Returns the directory path that the dialog will use, or an empty string if
   * this is not set. File names in this path will appear in the dialog,
   * filtered according to the filter extensions.
   * 
   * @return the directory path string
   * @see #setFilterExtensions
   */
  public String getFilterPath() {
    return filterPath;
  }

  /**
   * Returns the flag that the dialog will use to determine whether to prompt
   * the user for file overwrite if the selected file already exists.
   * 
   * @return true if the dialog will prompt for file overwrite, false otherwise
   */
  public boolean getOverwrite() {
    return overwrite;
  }

  /**
   * Set the initial filename which the dialog will select by default when
   * opened to the argument, which may be null. The name will be prefixed with
   * the filter path when one is supplied.
   * 
   * @param string the file name
   */
  public void setFileName( String string ) {
    fileName = string;
  }

  /**
   * Set the file extensions which the dialog will use to filter the files it
   * shows to the argument, which may be null.
   * <p>
   * The strings are platform specific. For example, on some platforms, an
   * extension filter string is typically of the form "*.extension", where "*.*"
   * matches all files. For filters with multiple extensions, use semicolon as a
   * separator, e.g. "*.jpg;*.png".
   * </p>
   * 
   * @param extensions the file extension filter
   * @see #setFilterNames to specify the user-friendly names corresponding to
   *      the extensions
   */
  public void setFilterExtensions( String[] extensions ) {
    filterExtensions = extensions;
  }

  /**
   * Set the 0-based index of the file extension filter which the dialog will
   * use initially to filter the files it shows to the argument.
   * <p>
   * This is an index into the FilterExtensions array and the FilterNames array.
   * </p>
   * 
   * @param index the file extension filter index
   * @see #setFilterExtensions
   * @see #setFilterNames
   */
  public void setFilterIndex( int index ) {
    filterIndex = index;
  }

  /**
   * Sets the names that describe the filter extensions which the dialog will
   * use to filter the files it shows to the argument, which may be null.
   * <p>
   * Each name is a user-friendly short description shown for its corresponding
   * filter. The <code>names</code> array must be the same length as the
   * <code>extensions</code> array.
   * </p>
   * 
   * @param names the list of filter names, or null for no filter names
   * @see #setFilterExtensions
   */
  public void setFilterNames( String[] names ) {
    filterNames = names;
  }

  /**
   * Sets the directory path that the dialog will use to the argument, which may
   * be null. File names in this path will appear in the dialog, filtered
   * according to the filter extensions. If the string is null, then the
   * operating system's default filter path will be used.
   * <p>
   * Note that the path string is platform dependent. For convenience, either
   * '/' or '\' can be used as a path separator.
   * </p>
   * 
   * @param string the directory path
   * @see #setFilterExtensions
   */
  public void setFilterPath( String string ) {
    filterPath = string;
  }

  /**
   * Sets the flag that the dialog will use to determine whether to prompt the
   * user for file overwrite if the selected file already exists.
   * 
   * @param overwrite true if the dialog will prompt for file overwrite, false
   *          otherwise
   */
  public void setOverwrite( boolean overwrite ) {
    this.overwrite = overwrite;
  }

  /**
   * Sets the dialog to begin uploading immediately after files are selected.
   * 
   * @param autoUpload <code>true</code> to set the dialog to autoupload as
   *          files are selected, else <code>false</code>
   */
  public void setAutoUpload( boolean autoUpload ) {
    this.autoUpload = autoUpload;
  }

  /**
   * Returns the flag that the dialog will use to determine whether to
   * autoupload files.
   * 
   * @return <code>true</code> if the dialog is configured to auto upload files,
   *         else <code>false</code>
   */
  public boolean isAutoUpload() {
    return autoUpload;
  }

  public String open() {
    UICallBack.activate( FileDialog.class.getName() + hashCode() );
    initializeDefaults();
    createShell();
    createControls();
    layoutAndCenterShell();
    openShell();
    return fileName;
  }

  private void initializeDefaults() {
    uploadPanels = new ArrayList();
    uploadLocked = false;
    // [ar] - add a strategy for content type?
    ExtensionValidationStrategy validationStrategy = 
      new ExtensionValidationStrategy( filterExtensions, filterIndex );
    validationHandler = new ValidationHandler() {
      public void updateEnablement() {
        FileDialog.this.updateEnablement();
      }
    };
    validationHandler.addValidationStrategy( validationStrategy );
    progressCollector = new ProgressCollector( validationHandler );
    validationHandler.setNumUploads( uploadPanels.size() );
  }

  private void createShell() {
    // int style = SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL;
    shell = new Shell( shell, getStyle() ); // HACK this.shell is holding the
                                            // parent shell until now
    shell.setText( getText() );
    shell.addShellListener( new ShellAdapter() {

      public void shellClosed( final ShellEvent e ) {
        handleShellClose();
      }
    } );
    // shell.addControlListener( new ControlAdapter() {
    // public void controlResized( ControlEvent e ) {
    // Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    // Point initialPrefSize = (Point)shell.getData( "initialPrefSize" );
    // if (initialPrefSize == null) {
    // shell.setData( "initialPrefSize", prefSize );
    // }
    // if (!prefSize.equals( initialPrefSize )) {
    // layoutAndCenterShell();
    // shell.removeControlListener( this );
    // }
    // }
    // });
  }

  private void layoutAndCenterShell() {
    Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    Point size = shell.getSize();
    shell.setSize( prefSize );
    try {
      Field parentField = getClass().getSuperclass().getDeclaredField( "parent" );
      parentField.setAccessible( true );
      Rectangle parentSize = ( ( Shell )parentField.get( this ) ).getBounds();
      int locationX = ( parentSize.width - prefSize.x ) / 2 + parentSize.x;
      int locationY = ( parentSize.height - prefSize.y ) / 2 + parentSize.y;
      shell.setLocation( locationX, locationY );
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }

  private void openShell() {
    shell.open();
    Display display = shell.getDisplay();
    while( !shell.isDisposed() ) {
      if( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    shell = null;
  }

  private void createControls() {
    GridLayout mainLayout = new GridLayout();
    mainLayout.marginWidth = 10;
    mainLayout.marginHeight = 10;
    mainLayout.horizontalSpacing = 10;
    mainLayout.verticalSpacing = 10;
    shell.setLayout( mainLayout );
    createDialogArea( shell );
    createButtonArea( shell );
  }

  private boolean allowMultiple() {
    return ( getStyle() & ( SWT.MULTI ) ) != 0;
  }

  private Control createDialogArea( Composite parentComposite ) {
    // create a composite with standard margins and spacing
    Composite main = new Composite( parentComposite, SWT.NONE );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    GridLayout layout = new GridLayout( 1, true );
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    main.setLayout( layout );
    if( allowMultiple() ) {
      createMultiSelector( main );
    } else {
      createSingleSelector( main );
    }
    Composite footerComp = new Composite( main, SWT.NONE );
    GridLayout footerLayout = new GridLayout( 2, false );
    footerLayout.marginWidth = 0;
    footerLayout.marginHeight = 0;
    footerComp.setLayout( footerLayout );
    footerComp.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    if( allowMultiple() ) {
      footerLayout.numColumns = 3;
      createAddSelectorBtn( footerComp );
    }
    filterSelector = createFilterSelector( footerComp );
    filterSelector.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
    totalProgressBar = new ProgressBar( footerComp, SWT.HORIZONTAL | SWT.SMOOTH );
    totalProgressBar.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    totalProgressBar.setToolTipText( "Total upload progress" );
    totalProgressBar.setMaximum( 100 );
    progressCollector.setProgressBar( totalProgressBar );
    return main;
  }

  private void createSingleSelector( Composite main ) {
    UploadPanel uploadPanel = new UploadPanel( main, UploadPanel.FULL );
    uploadPanel.setValidationHandler( validationHandler );
    uploadPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    uploadPanel.setProgressCollector( progressCollector );
    uploadPanel.setAutoUpload( isAutoUpload() );
    uploadPanels.add( uploadPanel );
    validationHandler.setNumUploads( uploadPanels.size() );
  }

  private void createMultiSelector( Composite main ) {
    Group uploadArea = new Group( main, SWT.NONE );
    uploadArea.setLayout( new GridLayout( 1, true ) );
    uploadArea.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    uploadArea.setText( "Selected Files" );
    uploadScroller = new ScrolledComposite( uploadArea, SWT.V_SCROLL );
    uploadScroller.addControlListener( new ControlAdapter() {

      public void controlResized( ControlEvent e ) {
        resizeUploads();
      }
    } );
    GridData uploadScrollerLayoutData = new GridData( SWT.FILL, SWT.FILL, true, true );
    uploadScrollerLayoutData.minimumHeight = 112;
    uploadScroller.setLayoutData( uploadScrollerLayoutData );
    scrollChild = new Composite( uploadScroller, SWT.NONE );
    scrollChild.setLayout( new GridLayout( 1, true ) );
    uploadsWrapper = new Composite( scrollChild, SWT.NONE );
    GridLayout uploadWrapperLayout = new GridLayout( 1, true );
    uploadWrapperLayout.marginWidth = 0;
    uploadWrapperLayout.marginHeight = 0;
    uploadsWrapper.setLayout( uploadWrapperLayout );
    uploadsWrapper.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    addUploadPanel();
    uploadScroller.setContent( scrollChild );
  }

  private void createAddSelectorBtn( Composite parent ) {
    addFileSelectorBtn = new Button( parent, SWT.PUSH );
    if (addImage == null) {
      addImage = new Image(addFileSelectorBtn.getDisplay(), UploadPanel.class.getResourceAsStream( "add_obj.gif" ));
    }
    addFileSelectorBtn.setImage( addImage );
    addFileSelectorBtn.setToolTipText( "Add more files" );
    addFileSelectorBtn.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
    addFileSelectorBtn.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent e ) {
        UploadPanel newPanel = addUploadPanel();
        resizeUploads();
        uploadScroller.showControl( newPanel );
        progressCollector.updateTotalProgress();
      }
    } );
  }

  /**
   * Only use for the SWT.MULTI case.
   */
  private void resizeUploads() {
    int xHint = SWT.DEFAULT;
    int clientWidth = uploadScroller.getClientArea().width;
    if( clientWidth > 0 )
      xHint = clientWidth;
    Point wrapperSize = scrollChild.computeSize( xHint, SWT.DEFAULT );
    scrollChild.setSize( wrapperSize );
  }

  /**
   * Only use for the SWT.MULTI case.
   */
  private UploadPanel addUploadPanel() {
    final UploadPanel uploadPanel = new UploadPanel( uploadsWrapper, UploadPanel.COMPACT
                                                                     | UploadPanel.PROGRESS
                                                                     | UploadPanel.REMOVEABLE );
    uploadPanel.addDisposeListener( new DisposeListener() {

      public void widgetDisposed( DisposeEvent event ) {
        Display.getCurrent().asyncExec( new Runnable() {
          public void run() {
            if (!uploadsWrapper.isDisposed()) {
              uploadPanels.remove( uploadPanel );
              validationHandler.setNumUploads( uploadPanels.size() );
              resizeUploads();
              progressCollector.updateTotalProgress();
              updateEnablement();
            }
          }
        });
      }
    } );
    uploadPanel.setValidationHandler( validationHandler );
    uploadPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    uploadPanel.setProgressCollector( progressCollector );
    uploadPanel.setAutoUpload( isAutoUpload() );
    uploadPanels.add( uploadPanel );
    validationHandler.setNumUploads( uploadPanels.size() );
    resizeUploads();
    updateEnablement();
    return uploadPanel;
  }

  private Combo createFilterSelector( Composite headerComp ) {
    final Combo filterCombo = new Combo( headerComp, SWT.DROP_DOWN | SWT.READ_ONLY );
    filterCombo.setToolTipText( "Selected filename filter" );
    String[] names = getFilterNames();
    String[] exts = getFilterExtensions();
    if( ( names == null || names.length == 0 ) && ( exts == null || exts.length == 0 ) ) {
      names = new String[]{
        "All Files"
      };
      exts = new String[]{
        "*.*"
      };
    }
    for( int i = 0; i < exts.length; i++ ) {
      StringBuffer sb = new StringBuffer();
      if( names != null && i < names.length )
        sb.append( names[ i ] ).append( " " );
      sb.append( "(" );
      sb.append( exts[ i ] );
      sb.append( ")" );
      filterCombo.add( sb.toString() );
    }
    filterCombo.select( getFilterIndex() );
    filterCombo.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent e ) {
        setFilterIndex( filterCombo.getSelectionIndex() );
        for( int i = 0; i < uploadPanels.size(); i++ ) {
          UploadPanel panel = ( UploadPanel )uploadPanels.get( i );
          panel.validate();
        }
      }
    } );
    return filterCombo;
  }

  private void createButtonArea( final Composite parent ) {
    Composite buttonComposite = new Composite( parent, SWT.NONE );
    GridData layoutData = new GridData( SWT.RIGHT, SWT.CENTER, false, false );
    layoutData.horizontalSpan = 2;
    buttonComposite.setLayoutData( layoutData );
    GridLayout buttonCompLayout = new GridLayout( 2, false );
    buttonCompLayout.marginWidth = 0;
    buttonCompLayout.marginHeight = 0;
    buttonComposite.setLayout( buttonCompLayout );
    String okText = SWT.getMessage( "SWT_OK" );
    okButton = createButton( buttonComposite, okText );
    parent.getShell().setDefaultButton( okButton );
    okButton.forceFocus();
    String cancelText = SWT.getMessage( "SWT_Cancel" );
    Button cancelButton = createButton( buttonComposite, cancelText );
    okButton.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( final SelectionEvent e ) {
        startUploads();
      }
    } );
    cancelButton.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( final SelectionEvent e ) {
        shell.close();
      }
    } );
    updateEnablement();
  }

  private Button createButton( final Composite parent, final String text ) {
    Button result = new Button( parent, SWT.PUSH );
    result.setText( text );
    GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    Font dialogFont = result.getFont();
    float charWidth = Graphics.getAvgCharWidth( dialogFont );
    float width = charWidth * BUTTON_WIDTH + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2;
    int widthHint = ( int )( width / HORIZONTAL_DIALOG_UNIT_PER_CHAR );
    Point minSize = result.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
    data.widthHint = Math.max( widthHint, minSize.x );
    result.setLayoutData( data );
    return result;
  }

  private void startUploads() {
    java.util.List needsProcessing = new ArrayList();
    for( int i = 0; i < uploadPanels.size(); i++ ) {
      UploadPanel panel = ( UploadPanel )uploadPanels.get( i );
      if( !panel.isFinished() ) {
        needsProcessing.add( panel );
      }
    }
    uploadLocked = true;
    if( needsProcessing.size() > 0 ) {
      okButton.setText( "Uploading..." );
      okButton.setToolTipText( "Waiting for uploads to finish" );
      okButton.setEnabled( false );
      if( addFileSelectorBtn != null ) {
        addFileSelectorBtn.setEnabled( false );
      }
      for( int i = 0; i < needsProcessing.size(); i++ ) {
        UploadPanel panel = ( UploadPanel )needsProcessing.get( i );
        if( !panel.isStarted() ) {
          panel.startUpload();
        }
        panel.setEnabled( false );
      }
    }
    updateEnablement();
  }

  private void updateEnablement() {
    if( okButton != null && !okButton.isDisposed() ) {
      String okText = SWT.getMessage( "SWT_OK" );
      if( uploadPanels.size() == 0 ) {
        okButton.setText( okText );
        okButton.setEnabled( false );
      } else if( uploadPanels.size() > 0 ) {
        boolean enabled = true;
        if( uploadLocked ) {
          if( progressCollector.isFinished() ) {
            okButton.setText( okText );
            fileNames = new String[ uploadPanels.size() ];
            fileName = null;
            for( int i = 0; i < uploadPanels.size(); i++ ) {
              UploadPanel uploadPanel = ( UploadPanel )uploadPanels.get( i );
              File uploadedFile = uploadPanel.getUploadedFile();
              // TODO [rst] Understand if file can be null
              if( uploadedFile != null ) {
                fileNames[ i ] = uploadedFile.getAbsolutePath();
              }
              if( fileName == null || fileName.length() == 0 )
                fileName = fileNames[ 0 ];
            }
            shell.close();
          }
        } else {
          okButton.setText( okText );
          for( int i = 0; i < uploadPanels.size(); i++ ) {
            UploadPanel panel = ( UploadPanel )uploadPanels.get( i );
            if( panel.getSelectedFilename().length() == 0 ) {
              enabled = false;
            }
          }
          if( !enabled ) {
            okButton.setToolTipText( "Specify files in all empty selectors to continue." );
          } else {
            okButton.setToolTipText( "" );
          }
          okButton.setEnabled( enabled );
        }
      }
    }
  }

  private void cleanup() {
    UICallBack.deactivate( FileDialog.class.getName() + hashCode() );
    if (addImage != null) {
      addImage.dispose();
      addImage = null;
    }
  }

  private void handleShellClose() {
    cleanup();
  }
}
