package io.arivera.oss.embedded.rabbitmq;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PredefinedVersionTest {

  @Test
  public void version() throws Exception {
    assertThat(PredefinedVersion.V3_6_5.getVersionAsString(), equalTo("3.6.5"));
    assertThat(PredefinedVersion.V3_4_0.getVersionAsString(), equalTo("3.4.0"));
  }

  @Test
  public void latestEnumIsNewestVersion() throws Exception {
    assertThat(PredefinedVersion.LATEST.version, equalTo(PredefinedVersion.values()[0].getVersionAsString()));
  }
}