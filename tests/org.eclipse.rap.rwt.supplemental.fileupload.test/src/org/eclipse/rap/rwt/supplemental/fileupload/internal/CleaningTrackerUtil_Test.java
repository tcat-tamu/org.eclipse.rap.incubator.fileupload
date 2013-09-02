/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.supplemental.fileupload.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FileCleaningTracker;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CleaningTrackerUtil_Test {

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    CleaningTrackerUtil.stopCleaningTracker( RWT.getUISession() );
    Fixture.tearDown();
  }

  @Test
  public void testNoCleanerInstanceByDefault() {
    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

  @Test
  public void testCleanerIsCreated() {
    assertNotNull( CleaningTrackerUtil.getCleaningTracker( true ) );
  }

  @Test
  public void testCleanerIsBuffered() {
    FileCleaningTracker tracker = CleaningTrackerUtil.getCleaningTracker( true );

    assertSame( tracker, CleaningTrackerUtil.getCleaningTracker( true ) );
  }

  @Test
  public void testStopClearsBuffer() {
    CleaningTrackerUtil.getCleaningTracker( true );

    CleaningTrackerUtil.stopCleaningTracker( RWT.getUISession() );

    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

  @Test
  public void testStopCallsExitWhenFinished() {
    FileCleaningTracker tracker = mock( FileCleaningTracker.class );
    RWT.getUISession().setAttribute( CleaningTrackerUtil.TRACKER_ATTR, tracker );

    CleaningTrackerUtil.stopCleaningTracker( RWT.getUISession() );

    verify( tracker ).exitWhenFinished();
  }

  @Test
  public void testAutoStop() {
    CleaningTrackerUtil.getCleaningTracker( true );

    Fixture.disposeOfServiceContext();
    Fixture.createServiceContext();

    assertNull( CleaningTrackerUtil.getCleaningTracker( false ) );
  }

}
