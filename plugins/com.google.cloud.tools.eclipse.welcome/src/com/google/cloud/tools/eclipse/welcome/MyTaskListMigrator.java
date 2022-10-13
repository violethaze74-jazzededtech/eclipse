package com.google.cloud.tools.eclipse.welcome;

import java.util.Set;
import org.eclipse.mylyn.tasks.core.AbstractTaskListMigrator;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.w3c.dom.Element;

public class MyTaskListMigrator extends AbstractTaskListMigrator {

  @Override
  public String getConnectorKind() {
    System.out.println("MyTaskListMigrator.getConnectorKind");
    // TODO Auto-generated method stub
    return "myAwesomeConnectorKind";
  }

  @Override
  public Set<String> getQueryElementNames() {
    System.out.println("getQueryElementNames");
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTaskElementName() {
    System.out.println("getTaskElementName");
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void migrateQuery(IRepositoryQuery arg0, Element arg1) {
    System.out.println("migrateQuery");
    // TODO Auto-generated method stub

  }

  @Override
  public void migrateTask(ITask arg0, Element arg1) {
    System.out.println("migrateTask");
    // TODO Auto-generated method stub

  }

}
