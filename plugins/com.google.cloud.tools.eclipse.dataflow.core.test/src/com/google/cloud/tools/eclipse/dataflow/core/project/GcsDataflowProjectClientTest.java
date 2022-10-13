/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.eclipse.dataflow.core.project;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link GcsDataflowProjectClient}.
 */
@RunWith(JUnit4.class)
public class GcsDataflowProjectClientTest {

  @Test
  public void testToGcsLocationUriWithFullUriReturnsUri() {
    String location = "gs://foo-bar/baz";
    assertEquals(location, GcsDataflowProjectClient.toGcsLocationUri(location));
  }

  @Test
  public void testToGcsLocationUri_caseInsensitivePrefixDetection() {
    assertEquals("gs://foo/bar", GcsDataflowProjectClient.toGcsLocationUri("GS://foo/bar"));
    assertEquals("gs://foo/bar", GcsDataflowProjectClient.toGcsLocationUri("gS://foo/bar"));
    assertEquals("gs://foo/bar", GcsDataflowProjectClient.toGcsLocationUri("Gs://foo/bar"));
  }

  @Test
  public void testToGcsLocationUriWithNullReturnsNull() {
    assertEquals(null, GcsDataflowProjectClient.toGcsLocationUri(null));
  }

  @Test
  public void testToGcsLocationUriWithEmptyInputReturnsEmpty() {
    assertEquals("", GcsDataflowProjectClient.toGcsLocationUri(""));
  }

  @Test
  public void testToGcsLocationUriWithoutPrefixReturnsWithPrefix() {
    String location = "foo-bar/baz";
    assertEquals("gs://" + location, GcsDataflowProjectClient.toGcsLocationUri(location));
  }

  @Test
  public void testToGcsLocationUriWithBucketNameOnlyReturnsWithPrefix() {
    String location = "foo-bar";
    assertEquals("gs://foo-bar", GcsDataflowProjectClient.toGcsLocationUri(location));
  }

  @Test
  public void testToGcsBucketName_withoutObject() {
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("my-bucket"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("my-bucket/object"));
  }

  @Test
  public void testToGcsBucketName_withObject() {
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("my-bucket/object"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("gs://my-bucket/object"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("GS://my-bucket/object"));
  }

  @Test
  public void testToGcsBucketName_stripsLeadingForwardSlashes() {
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("my-bucket"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("/my-bucket"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("///my-bucket/"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("///my-bucket/object"));

    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("gs://my-bucket"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("gs:///my-bucket"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("gs://///my-bucket/"));
    assertEquals("my-bucket", GcsDataflowProjectClient.toGcsBucketName("gs://///my-bucket/object"));
  }
}

