/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.appengine.validation;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.junit.Assert;
import org.junit.Test;

public class AbstractQuickAssistProcessorTest {

  @Test
  public void testComputeApplicationQuickAssistProposals() {
    AbstractQuickAssistProcessor processor = new ApplicationQuickAssistProcessor();
    ICompletionProposal[] fixes = processor.computeQuickAssistProposals(null);
    assertEquals(1, fixes.length);
    assertEquals(ApplicationSourceQuickFix.class.getName(), fixes[0].getClass().getName());
  }
  
  @Test
  public void testComputeVersionQuickAssistProposals() {
    AbstractQuickAssistProcessor processor = new VersionQuickAssistProcessor();
    ICompletionProposal[] fixes = processor.computeQuickAssistProposals(null);
    assertEquals(1, fixes.length);
    assertEquals(VersionSourceQuickFix.class.getName(), fixes[0].getClass().getName());
  }
  
  @Test
  public void testComputeUpgradeRuntimeQuickAssistProposals() {
    AbstractQuickAssistProcessor processor = new UpgradeRuntimeQuickAssistProcessor();
    
    Assert.assertFalse(processor.canAssist(null));
    Annotation annotation = new Annotation(true);
    Assert.assertTrue(processor.canFix(annotation));
    annotation.markDeleted(true);
    Assert.assertFalse(processor.canFix(annotation));    
    
    ICompletionProposal[] fixes = processor.computeQuickAssistProposals(null);
    assertEquals(1, fixes.length);
    assertEquals(UpgradeRuntimeSourceQuickFix.class.getName(), fixes[0].getClass().getName());
  }
  
}