package com.google.cloud.tools.eclipse.welcome;

import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;

public class MyRepositoryMigrator extends AbstractRepositoryMigrator {

  @Override
  public String getConnectorKind() {
    System.out.println("MyRepositoryMigrator.getConnectorKind");
    // TODO Auto-generated method stub
    return "myAwesomeConnectorKind";
  }

}
