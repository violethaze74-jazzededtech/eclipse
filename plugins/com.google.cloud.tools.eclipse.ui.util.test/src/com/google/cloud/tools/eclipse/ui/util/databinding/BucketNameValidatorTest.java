package com.google.cloud.tools.eclipse.ui.util.databinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.eclipse.core.runtime.IStatus;
import org.junit.Test;

public class BucketNameValidatorTest {

  private static final String LENGTH_63 = "123456789012345678901234567890123456789012345678901234567890123";
  private static final String LENGTH_64_WITH_DOT = "12345678901234567890123456789012345678901234567890123456789012.4";
  private static final String LENGTH_222 = "12345678901234567890123456789012345678901234567890."
                                           + "12345678901234567890123456789012345678901234567890."
                                           + "12345678901234567890123456789012345678901234567890."
                                           + "12345678901234567890123456789012345678901234567890."
                                           + "123456789012345678";

  @Test
  public void testValidation_nonStringInput() {
    assertThat(new BucketNameValidator().validate(new Object()).getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_emptyString() {
    assertThat(new BucketNameValidator().validate("").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_upperCaseLetter() {
    assertThat(new BucketNameValidator().validate("THISWOULDBEVALIDIFLOWERCASE").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_startWithDot() {
    assertThat(new BucketNameValidator().validate(".bucket").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_endWithDot() {
    assertThat(new BucketNameValidator().validate("bucket.").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_startWithHyphen() {
    assertThat(new BucketNameValidator().validate("-bucket").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_endWithHyphen() {
    assertThat(new BucketNameValidator().validate("bucket-").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_startWithUnderscore() {
    assertThat(new BucketNameValidator().validate("_bucket").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_endWithUnderscore() {
    assertThat(new BucketNameValidator().validate("bucket_").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_maxLengthWithoutDot() {
    assertThat(new BucketNameValidator().validate(LENGTH_63).getSeverity(), is(IStatus.OK));
  }

  @Test
  public void testValidation_tooLongNameWithoutDot() {
    assertThat(new BucketNameValidator().validate(LENGTH_63 + "4").getSeverity(), is(IStatus.ERROR));
  }
  
  @Test
  public void testValidation_validNameWithDot() {
    assertThat(new BucketNameValidator().validate(LENGTH_64_WITH_DOT).getSeverity(), is(IStatus.OK));
  }
  
  @Test
  public void testValidation_tooLongNameWithDot() {
    assertThat(new BucketNameValidator().validate(LENGTH_222 + "9").getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testValidation_maxLengthWithDot() {
    assertThat(new BucketNameValidator().validate(LENGTH_222).getSeverity(), is(IStatus.OK));
  }
  
  @Test
  public void testValidation_emptyComponent() {
    assertThat(new BucketNameValidator().validate("foo..bar").getSeverity(), is(IStatus.ERROR));
  }
}
