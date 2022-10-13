package com.google.cloud.tools.eclipse.welcome;

import org.eclipse.mylyn.internal.github.core.issue.IssueConnector;

public class MyIssueConnector extends IssueConnector {

  @Override
  public String getRepositoryUrlFromTaskUrl(String arg0) {
    System.out.println("getRepositoryUrlFromTaskUrl");
   // TODO Auto-generated method stub
    return "https://github.com/GoogleCloudPlatform/google-cloud-eclipse";
  }
  @Override
  public String getTaskUrl(String arg0, String arg1) {
    System.out.println("getTaskUrl");
 // TODO Auto-generated method stub
    return "https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues" + arg1;
  }
  @Override
  public String getConnectorKind() {
    System.out.println("getConnectorKind");
    // TODO Auto-generated method stub
    return "mygithub";
  }

}
