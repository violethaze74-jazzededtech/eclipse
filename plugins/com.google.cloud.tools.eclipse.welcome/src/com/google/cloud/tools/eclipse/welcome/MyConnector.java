package com.google.cloud.tools.eclipse.welcome;

import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

public class MyConnector extends AbstractRepositoryConnector {

  @Override
  public AbstractTaskDataHandler getTaskDataHandler() {
    return new AbstractTaskDataHandler() {

      @Override
      public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
          Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
        System.out.println("postTaskData");
        // TODO Auto-generated method stub
        return new RepositoryResponse(ResponseKind.TASK_CREATED, "9999");
      }

      @Override
      public boolean initializeTaskData(TaskRepository repository, TaskData data,
          ITaskMapping initializationData, IProgressMonitor monitor) throws CoreException {
        data.setVersion("my awesome version");
        System.out.println("initializeTaskData");
        System.out.println("data.getRepoUrl: " + data.getRepositoryUrl());
        // TODO Auto-generated method stub
        return true;
      }

      @Override
      public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
        System.out.println("getAttributeMapper");
        // TODO Auto-generated method stub
        TaskAttributeMapper attributeMapper = new TaskAttributeMapper(repository);
        return attributeMapper;
      }
    };
  }

  @Override
  public boolean canCreateNewTask(TaskRepository arg0) {
    System.out.println("canCreateNewTask");
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean canCreateTaskFromKey(TaskRepository arg0) {
    System.out.println("canCreateTaskFromKey");
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ITaskMapping getTaskMapping(TaskData arg0) {
    System.out.println("canCreateTaskFromKey");
    return new TaskMapper(arg0);
  }


  @Override
  public String getConnectorKind() {
    System.out.println("getConnectorKind");
    // TODO Auto-generated method stub
    return "myAwesomeConnectorKind";
  }

  @Override
  public String getLabel() {
    System.out.println("getLabel");
   // TODO Auto-generated method stub
    return "myAwesomeConnectorLabel";
  }

  @Override
  public String getRepositoryUrlFromTaskUrl(String arg0) {
    System.out.println("getRepositoryUrlFromTaskUrl");
   // TODO Auto-generated method stub
    return "https://github.com/GoogleCloudPlatform/google-cloud-eclipse";
  }

  @Override
  public String getTaskIdFromTaskUrl(String arg0) {
    System.out.println("getTaskIdFromTaskUrl");
   // TODO Auto-generated method stub
    return "myAwesomeTaskId";
  }

  @Override
  public String getTaskUrl(String arg0, String arg1) {
    System.out.println("getTaskUrl");
 // TODO Auto-generated method stub
    return "https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues";
  }

  @Override
  public boolean hasTaskChanged(TaskRepository arg0, ITask arg1, TaskData arg2) {
    System.out.println("hasTaskChanged");
 // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateTaskFromTaskData(TaskRepository arg0, ITask arg1, TaskData arg2) {
    System.out.println("updateTaskFromTaskData");
  // TODO Auto-generated method stub

  }

  @Override
  public TaskData getTaskData(TaskRepository arg0, String arg1,
      IProgressMonitor arg2) throws CoreException {
    System.out.println("getTaskData");
 // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStatus performQuery(TaskRepository arg0, IRepositoryQuery arg1,
      TaskDataCollector arg2, ISynchronizationSession arg3,
      IProgressMonitor arg4) {
    System.out.println("performQuery");
 // TODO Auto-generated method stub
    return Status.OK_STATUS;
  }

  @Override
  public void updateRepositoryConfiguration(TaskRepository arg0,
      IProgressMonitor arg1) throws CoreException {
    System.out.println("updateRepositoryConfiguration");
 // TODO Auto-generated method stub

  }

}
