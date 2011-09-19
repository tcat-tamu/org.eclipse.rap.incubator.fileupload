package org.eclipse.rap.rwt.supplemental.fileupload.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.supplemental.fileupload.TestAdapter;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.TestRequest;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;


public final class FileUploadTestUtil {

  private FileUploadTestUtil() {
    // prevent instantiation
  }

  public static File createTempDirectory() {
    File result;
    try {
      result = File.createTempFile( "temp-", "-dir" );
    } catch( IOException exception ) {
      throw new RuntimeException( "Could not create temp file", exception );
    }
    if( !result.delete() ) {
      throw new RuntimeException( "Could not delete temp file: " + result.getAbsolutePath() );
    }
    if( !result.mkdir() ) {
      throw new RuntimeException( "Could not create temp directory: " + result.getAbsolutePath() );
    }
    return result;
  }

  public static void deleteRecursively( File file ) {
    if( file.exists() ) {
      File[] files = file.listFiles();
      if( files != null ) {
        for( int i = 0; i < files.length; i++ ) {
          deleteRecursively( files[ i ] );
        }
      }
      boolean deleted = file.delete();
      if( !deleted ) {
        throw new RuntimeException( "Could not delete file or directory: " + file.getAbsolutePath() );
      }
    }
  }

  public static void fakeUploadRequest( FileUploadHandler handler,
                                        String content,
                                        String contentType,
                                        String fileName )
  {
    String token = TestAdapter.getTokenFor( handler );
    fakeUploadRequest( token, content, contentType, fileName );
  }

  public static void fakeUploadRequest( String token,
                                        String content,
                                        String contentType,
                                        String fileName )
  {
    Fixture.fakeNewRequest();
    TestRequest request = ( TestRequest )RWT.getRequest();
    request.setMethod( "POST" );
    request.setParameter( IServiceHandler.REQUEST_PARAM, "org.eclipse.rap.fileupload" );
    String boundary = "-----4711-----";
    String body = createMultipartBody( content, contentType, fileName, boundary );
    if( token != null ) {
      Fixture.fakeRequestParam( "token", token );
    }
    request.setBody( body );
    request.setContentType( "multipart/form-data; boundary=" + boundary );
  }

  public static TestRequest createFakeUploadRequest( String content,
                                                     String contentType,
                                                     String fileName )
  {
    TestRequest request = new TestRequest();
    String boundary = "-----4711-----";
    String body = createMultipartBody( content, contentType, fileName, boundary );
    request.setMethod( "POST" );
    request.setBody( body );
    request.setContentType( "multipart/form-data; boundary=" + boundary );
    return request;
  }

  public static String createMultipartBody( String content,
                                            String contentType,
                                            String fileName,
                                            String boundary )
  {
    StringBuffer buffer = new StringBuffer();
    String newline = "\r\n";
    buffer.append( "--" );
    buffer.append( boundary );
    buffer.append( newline );
    buffer.append( "Content-Disposition: form-data; name=\"file\"; filename=\"" );
    buffer.append( fileName );
    buffer.append( "\"" );
    buffer.append( newline );
    if( contentType != null ) {
      buffer.append( "Content-Type: " );
      buffer.append( contentType );
      buffer.append( newline );
    }
    buffer.append( newline );
    buffer.append( content );
    buffer.append( newline );
    buffer.append( "--" );
    buffer.append( boundary );
    buffer.append( "--" );
    buffer.append( newline );
    return buffer.toString();
  }

  public static String getFileContents( File testFile ) {
    String result;
    // TODO [rst] Buffer can get very big with the wrong file
    byte[] buffer = new byte[ ( int )testFile.length() ];
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream( new FileInputStream( testFile ) );
      bis.read( buffer );
      result = new String( buffer );
    } catch( Exception exception ) {
      throw new RuntimeException( exception );
    } finally {
      if( bis != null ) {
        try {
          bis.close();
        } catch( Exception e ) {
          // ignore
        }
      }
    }
    return result;
  }
}
