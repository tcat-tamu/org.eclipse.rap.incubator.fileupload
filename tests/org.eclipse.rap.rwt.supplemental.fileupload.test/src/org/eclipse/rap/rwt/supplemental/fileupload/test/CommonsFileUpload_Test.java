package org.eclipse.rap.rwt.supplemental.fileupload.test;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


/*
 * Tests to understand and verify how commons.fileupload works
 */
public class CommonsFileUpload_Test extends TestCase {

  private File tempDirectory;

  protected void setUp() throws Exception {
    tempDirectory = FileUploadTestUtil.createTempDirectory();
  }

  protected void tearDown() throws Exception {
    FileUploadTestUtil.deleteRecursively( tempDirectory );
  }

  public void testUploadEmptyFileWithZeroThreshold() throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory( 0, tempDirectory );
    ServletFileUpload upload = new ServletFileUpload( factory );
    HttpServletRequest request
      = FileUploadTestUtil.createFakeUploadRequest( "", "text/empty", "empty.txt" );

    List items = upload.parseRequest( request );

    assertEquals( 1, items.size() );
    DiskFileItem fileItem = ( DiskFileItem )items.get( 0 );
    assertEquals( "empty.txt", fileItem.getName() );
    assertEquals( "text/empty", fileItem.getContentType() );
    assertEquals( 0L, fileItem.getSize() );
    // Content and threshold is zero, empty file is not written
    assertFalse( fileItem.getStoreLocation().exists() );
  }

  public void testUploadSmallerThanThreshold() throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory( 100, tempDirectory );
    ServletFileUpload upload = new ServletFileUpload( factory );
    HttpServletRequest request
      = FileUploadTestUtil.createFakeUploadRequest( "Hello World!\n", "text/plain", "hello.txt" );

    List items = upload.parseRequest( request );

    assertEquals( 1, items.size() );
    DiskFileItem fileItem = ( DiskFileItem )items.get( 0 );
    assertEquals( "hello.txt", fileItem.getName() );
    assertEquals( "text/plain", fileItem.getContentType() );
    assertEquals( "Hello World!\n", fileItem.getString() );
    // Content is smaller than threshold, therefore file is not written
    assertFalse( fileItem.getStoreLocation().exists() );
  }

  public void testUploadLongFile() throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory( 100, tempDirectory );
    ServletFileUpload upload = new ServletFileUpload( factory );
    StringBuffer content = new StringBuffer( 600 );
    for( int i = 0; i < 100; i++ ) {
      content.append( "Hello\n" );
    }
    HttpServletRequest request
      = FileUploadTestUtil.createFakeUploadRequest( content.toString(), "text/plain", "long.txt" );

    List items = upload.parseRequest( request );
    DiskFileItem fileItem = ( DiskFileItem )items.get( 0 );

    assertEquals( "long.txt", fileItem.getName() );
    assertEquals( "text/plain", fileItem.getContentType() );
    assertEquals( 600, fileItem.getSize() );
    assertTrue( fileItem.getStoreLocation().exists() );
  }
}
