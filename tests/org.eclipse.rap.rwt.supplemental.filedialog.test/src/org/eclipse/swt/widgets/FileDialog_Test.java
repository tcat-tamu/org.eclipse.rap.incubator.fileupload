package org.eclipse.swt.widgets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FileDialog_Test {

  private Display display;
  private Shell shell;
  private FileDialog fileDialog;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
    fileDialog = new FileDialog( shell );
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testReturnCodeAfterClose() {
    DialogCallback callback = mock( DialogCallback.class );
    DialogUtil.open( fileDialog, callback );

    fileDialog.shell.close();

    verify( callback ).dialogClosed( SWT.CANCEL );
  }

  @Test
  public void testReturnCodeAfterOkPressed() {
    DialogCallback callback = mock( DialogCallback.class );
    DialogUtil.open( fileDialog, callback );

    fileDialog.okPressed();
    fileDialog.shell.close();

    verify( callback ).dialogClosed( SWT.OK );
  }

}
