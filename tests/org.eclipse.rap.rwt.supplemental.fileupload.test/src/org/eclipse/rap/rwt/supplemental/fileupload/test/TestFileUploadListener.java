package org.eclipse.rap.rwt.supplemental.fileupload.test;

import org.eclipse.rap.rwt.supplemental.fileupload.FileUploadEvent;
import org.eclipse.rap.rwt.supplemental.fileupload.IFileUploadListener;


public class TestFileUploadListener implements IFileUploadListener {

  private FileUploadEvent lastEvent;
  protected StringBuffer log = new StringBuffer();

  public void uploadProgress( FileUploadEvent event ) {
    this.lastEvent = event;
    log.append( "progress." );
  }

  public void uploadFinished( FileUploadEvent event ) {
    this.lastEvent = event;
    log.append( "finished." );
  }

  public void uploadFailed( FileUploadEvent event ) {
    this.lastEvent = event;
    log.append( "failed." );
  }

  public long getMaxFileSize() {
    return -1;
  }

  public long getMaxRequestSize() {
    return -1;
  }

  public String getLog() {
    return log.toString();
  }

  public FileUploadEvent getLastEvent() {
    return lastEvent;
  }
}
