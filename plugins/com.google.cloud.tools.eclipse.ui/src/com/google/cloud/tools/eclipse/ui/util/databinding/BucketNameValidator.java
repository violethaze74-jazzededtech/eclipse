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

package com.google.cloud.tools.eclipse.ui.util.databinding;

import com.google.cloud.tools.eclipse.ui.util.Messages;
import com.google.common.net.InternetDomainName;
import java.util.regex.Pattern;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

/**
 * Implements a simplified (more permissive) bucket name validation for Google Cloud Storage.
 * <p>
 * The following rules are verified:
 * <ul>
 * <li>Use lowercase letters, numbers, hyphens (-), and underscores (_). You can also use a dot (.)
 * to form a valid top-level domain (e.g., example.com). Format: You must start and end the name
 * with a number or letter. Bucket names must contain 3 to 63 characters. Names containing dots can
 * contain up to 222 characters, but each dot-separated component can be no longer than 63
 * characters.</li>
 * <li>Cloud Storage considers bucket names that contain dots to be domain names, and as such these
 * bucket names must:
 * <ul>
 * <li>Be syntactically valid DNS names (for example, <code>bucket..example.com</code> is not valid
 * because it contains two dots in a row)</li>
 * <li>Has a <em>public suffix</em> (although GCS states it should ends with a TLD, due to the
 * domain verification requirements</li>
 * </ul></li>
 * </ul>
 * The actual rules that govern the bucket naming are more complex. See the complete list of bucket
 * name requirements for more information: https://cloud.google.com/storage/docs/naming
 */
// todo logic really belongs in appengine-plugins-core
public class BucketNameValidator implements IValidator {
  private static final Pattern CLOUD_STORAGE_BUCKET_NAME_PATTERN =
      Pattern.compile("^[a-z0-9][a-z0-9_.-]{1,220}[a-z0-9]$"); //$NON-NLS-1$

  @Override
  public IStatus validate(Object input) {
    if (!(input instanceof String)) {
      return ValidationStatus.error(Messages.getString("bucket.name.not.string")); //$NON-NLS-1$
    }
    String value = (String) input;
    if (value.isEmpty()) {
      return ValidationStatus.ok();
    } else if (!CLOUD_STORAGE_BUCKET_NAME_PATTERN.matcher(value).matches()) {
      return ValidationStatus.error(Messages.getString("bucket.name.invalid", value)); //$NON-NLS-1$
    } else {
      return allComponentsLengthAreValid(value);
    }
  }

  private IStatus allComponentsLengthAreValid(String value) {
    String[] components = value.split("\\.");
    for (String component : components) {
      if (component.length() == 0 || component.length() > 63) {
        return ValidationStatus.error(Messages.getString("bucket.name.invalid", value));
      }
    }
    // if contains dots then must be a valid domain name
    if (components.length > 1 && !InternetDomainName.isValid(value)) {
      return ValidationStatus.error(Messages.getString("bucket.name.invalid", value));
    }
    return ValidationStatus.ok();
  }
}
