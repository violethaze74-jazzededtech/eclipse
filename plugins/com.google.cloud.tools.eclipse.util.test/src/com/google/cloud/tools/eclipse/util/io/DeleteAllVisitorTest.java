/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.util.io;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.junit.Test;

public class DeleteAllVisitorTest {

  @Test
  public void test_nonEmptyDirectory() throws IOException {
    File tempDirectory = com.google.common.io.Files.createTempDir();
    Files.createTempFile(tempDirectory.toPath(), "deleteallvisitortest", null);
    Path childDirectory = Files.createTempDirectory(tempDirectory.toPath(), "deleteallvisitortest", new FileAttribute<?>[0]);
    Files.createTempFile(childDirectory, "deleteallvisitortest", null);

    Files.walkFileTree(tempDirectory.toPath(), new DeleteAllVisitor());

    assertFalse(tempDirectory.exists());
  }

}
