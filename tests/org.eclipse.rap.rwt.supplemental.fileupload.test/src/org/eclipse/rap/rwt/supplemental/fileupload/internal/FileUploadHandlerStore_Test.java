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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.test.TestFileUploadReceiver;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FileUploadHandlerStore_Test {

  private FileUploadHandlerStore itemStore;
  private FileUploadHandler testHandler;

  @Before
  public void setUp() {
    Fixture.setUp();
    itemStore = FileUploadHandlerStore.getInstance();
    testHandler = new FileUploadHandler( new TestFileUploadReceiver() );
  }

  @After
  public void tearDown() {
    testHandler.dispose();
    testHandler = null;
    itemStore = null;
    Fixture.tearDown();
  }

  @Test
  public void testGetInstance() {
    assertNotNull( itemStore );
    assertSame( itemStore, FileUploadHandlerStore.getInstance() );
  }

  @Test
  public void testGetNotExistingHandler() {
    FileUploadHandler result = itemStore.getHandler( "testId" );

    assertNull( result );
  }

  @Test
  public void testRegisterAndGetHandler() {
    itemStore.registerHandler( "testId", testHandler );

    FileUploadHandler result = itemStore.getHandler( "testId" );

    assertSame( testHandler, result );
  }

  @Test
  public void testGetHandlerWithDifferentId() {
    itemStore.registerHandler( "testId", testHandler );

    FileUploadHandler result = itemStore.getHandler( "anotherId" );

    assertNull( result );
  }

  @Test
  public void testDeregisterHandler() {
    itemStore.registerHandler( "testId", testHandler );

    itemStore.deregisterHandler( "testId" );
    FileUploadHandler result = itemStore.getHandler( "testId" );

    assertNull( result );
  }

  @Test
  public void testCreateToken() {
    String token = FileUploadHandlerStore.createToken();

    assertNotNull( token );
    assertTrue( token.length() > 0 );
    assertFalse( token.equals( FileUploadHandlerStore.createToken() ) );
  }

}
