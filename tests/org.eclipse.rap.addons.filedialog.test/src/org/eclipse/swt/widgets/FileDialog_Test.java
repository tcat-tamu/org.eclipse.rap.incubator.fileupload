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
package org.eclipse.swt.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.FileUploadRunnable;
import org.eclipse.swt.layout.GridData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings( "restriction" )
public class FileDialog_Test {

  private Display display;
  private Shell shell;
  private FileDialog dialog;
  private DialogCallback callback;
  private ThreadPoolExecutor singleThreadExecutor;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    singleThreadExecutor = mock( ThreadPoolExecutor.class );
    dialog = new TestFileDialog( shell, SWT.MULTI );
    callback = mock( DialogCallback.class );
    DialogUtil.open( dialog, callback );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testStyle() {
    dialog = new TestFileDialog( shell );
    int style = dialog.getStyle();

    assertFalse( ( style & SWT.MULTI ) != 0 );
    assertTrue( ( style & SWT.TITLE ) != 0 );
    assertTrue( ( style & SWT.APPLICATION_MODAL ) != 0 );
    assertTrue( ( style & SWT.BORDER ) != 0 );
  }

  @Test
  public void testStyle_multi() {
    dialog = new TestFileDialog( shell, SWT.MULTI );
    int style = dialog.getStyle();

    assertTrue( ( style & SWT.MULTI ) != 0 );
    assertTrue( ( style & SWT.TITLE ) != 0 );
    assertTrue( ( style & SWT.APPLICATION_MODAL ) != 0 );
    assertTrue( ( style & SWT.BORDER ) != 0 );
  }

  @Test
  public void testReturnCode_afterClose() {
    dialog.shell.close();

    verify( callback ).dialogClosed( SWT.CANCEL );
  }

  @Test
  public void testReturnCode_afterOkPressed() {
    getOKButton().notifyListeners( SWT.Selection, null );

    verify( callback ).dialogClosed( SWT.OK );
  }

  @Test
  public void testReturnCode_afterCancelPressed() {
    getCancelButton().notifyListeners( SWT.Selection, null );

    verify( callback ).dialogClosed( SWT.CANCEL );
  }

  @Test
  public void testOpen_activatesServerPush() {
    assertTrue( ServerPushManager.getInstance().isServerPushActive() );
  }

  @Test
  public void testClose_deactivatesServerPush() {
    dialog.shell.close();

    assertFalse( ServerPushManager.getInstance().isServerPushActive() );
  }

  @Test
  public void testClose_shutdownSingleThreadExecutor() {
    dialog.shell.close();

    verify( singleThreadExecutor ).shutdownNow();
  }

  @Test
  public void testClose_deletesUploadedFiles() {
    dialog.shell.close();

    assertTrue( ( ( TestFileDialog )dialog ).uploadedFilesDeleted );
  }

  @Test
  public void testCancel_deletesUploadedFiles() {
    getCancelButton().notifyListeners( SWT.Selection, null );

    assertTrue( ( ( TestFileDialog )dialog ).uploadedFilesDeleted );
  }

  @Test
  public void testOK_doesNotDeleteUploadedFiles() {
    getOKButton().notifyListeners( SWT.Selection, null );

    assertFalse( ( ( TestFileDialog )dialog ).uploadedFilesDeleted );
  }

  @Test
  public void testGetFileName_returnEmptyString() {
    dialog.shell.close();

    assertEquals( "", dialog.getFileName() );
  }

  @Test
  public void testGetFileNames_returnEmptyArray() {
    dialog.shell.close();

    assertEquals( 0, dialog.getFileNames().length );
  }

  @Test
  public void testFileUploadSelection_executesRunnable() {
    getFileUpload().notifyListeners( SWT.Selection, null );

    verify( singleThreadExecutor ).execute( any( FileUploadRunnable.class ) );
  }

  @Test
  public void testFileUploadSelection_hidesCurrentFileUpload_forMulti() {
    FileUpload fileUpload = getFileUpload();

    fileUpload.notifyListeners( SWT.Selection, null );

    assertFalse( fileUpload.isVisible() );
    assertTrue( ( ( GridData )fileUpload.getLayoutData() ).exclude );
  }

  @Test
  public void testFileUploadSelection_createsNewFileUpload_forMulti() {
    FileUpload fileUpload = getFileUpload();

    fileUpload.notifyListeners( SWT.Selection, null );

    assertNotSame( fileUpload, getFileUpload() );
  }

  @Test
  public void testFileUploadSelection_doesNotHideCurrentFileUpload_forSingle() {
    dialog = new TestFileDialog( shell );
    DialogUtil.open( dialog, callback );
    FileUpload fileUpload = getFileUpload();

    fileUpload.notifyListeners( SWT.Selection, null );

    assertTrue( fileUpload.isVisible() );
    assertFalse( ( ( GridData )fileUpload.getLayoutData() ).exclude );
  }

  @Test
  public void testFileUploadSelection_disablesCurrentFileUpload_forSingle() {
    dialog = new TestFileDialog( shell );
    DialogUtil.open( dialog, callback );
    FileUpload fileUpload = getFileUpload();

    fileUpload.notifyListeners( SWT.Selection, null );

    assertFalse( fileUpload.isEnabled() );
  }

  @Test
  public void testFileUploadSelection_doesNotCreateNewFileUpload_forSingle() {
    dialog = new TestFileDialog( shell );
    DialogUtil.open( dialog, callback );
    FileUpload fileUpload = getFileUpload();

    fileUpload.notifyListeners( SWT.Selection, null );

    assertSame( fileUpload, getFileUpload() );
  }

  private FileUpload getFileUpload() {
    Composite buttonsArea = ( Composite )dialog.shell.getChildren()[ 1 ];
    return ( FileUpload )buttonsArea.getChildren()[ 0 ];
  }

  private Button getOKButton() {
    Composite buttonsArea = ( Composite )dialog.shell.getChildren()[ 1 ];
    return ( Button )buttonsArea.getChildren()[ 2 ];
  }

  private Button getCancelButton() {
    Composite buttonsArea = ( Composite )dialog.shell.getChildren()[ 1 ];
    return ( Button )buttonsArea.getChildren()[ 3 ];
  }

  private class TestFileDialog extends FileDialog {

    private boolean uploadedFilesDeleted;

    public TestFileDialog( Shell shell ) {
      super( shell );
    }

    public TestFileDialog( Shell parent, int style ) {
      super( parent, style );
    }

    @Override
    ThreadPoolExecutor createSingleThreadExecutor() {
      return singleThreadExecutor;
    }

    @Override
    void deleteUploadedFiles() {
      uploadedFilesDeleted = true;
    }

  }

}
