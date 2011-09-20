package org.eclipse.rap.examples.demo.fileupload;

import org.eclipse.rap.examples.IExampleContribution;
import org.eclipse.rap.examples.IExamplePage;


final class FileUploadExampleContribution implements IExampleContribution {

  public String getId() {
    return "file-upload";
  }

  public String getTitle() {
    return "File Upload";
  }

  public IExamplePage createPage() {
    return new FileUploadExamplePage();
  }
}
