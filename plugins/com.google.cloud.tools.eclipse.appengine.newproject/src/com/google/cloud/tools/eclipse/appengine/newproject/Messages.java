package com.google.cloud.tools.eclipse.appengine.newproject;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "com.google.cloud.tools.eclipse.appengine.newproject.messages"; //$NON-NLS-1$
  public static String AppEngineStandardWizardPage_librariesGroupLabel;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
