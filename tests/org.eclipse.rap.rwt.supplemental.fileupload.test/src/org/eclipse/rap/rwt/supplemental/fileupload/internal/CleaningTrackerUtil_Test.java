/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.apache.commons.io.FileCleaningTracker;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.testfixture.Fixture;


public class CleaningTrackerUtil_Test extends TestCase {

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakeNewRequest();
  }

  @Override
  protected void tearDown() throws Exception {
    CleaningTrackerUtil.stopCleaningTracker( RWT.getSessionStore() );
    Fixture.tearDown();
  }

  public void testNoCleanerInstanceByDefault() {
    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

  public void testCleanerIsCreated() {
    assertNotNull( CleaningTrackerUtil.getCleaningTracker( true ) );
  }

  public void testCleanerIsBuffered() throws Exception {
    FileCleaningTracker tracker = CleaningTrackerUtil.getCleaningTracker( true );

    assertSame( tracker, CleaningTrackerUtil.getCleaningTracker( true ) );
  }

  public void testStopClearsBuffer() throws Exception {
    CleaningTrackerUtil.getCleaningTracker( true );

    CleaningTrackerUtil.stopCleaningTracker( RWT.getSessionStore() );

    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

  public void testStopCallsExitWhenFinished() throws Exception {
    FileCleaningTracker tracker = mock( FileCleaningTracker.class );
    RWT.getSessionStore().setAttribute( CleaningTrackerUtil.TRACKER_ATTR, tracker );

    CleaningTrackerUtil.stopCleaningTracker( RWT.getSessionStore() );

    verify( tracker ).exitWhenFinished();
  }

  public void testAutoStop() throws Exception {
    CleaningTrackerUtil.getCleaningTracker( true );

    Fixture.disposeOfServiceContext();
    Fixture.createServiceContext();

    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

}
