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
    PredefinedVersion firstDefinedEnumValue = PredefinedVersion.values()[0];
    assertThat(PredefinedVersion.LATEST.getVersionAsString(), equalTo(firstDefinedEnumValue.getVersionAsString()));
  }

  @Test
  public void testCompareTo() {
    assertThat(Version.VERSION_COMPARATOR.compare(PredefinedVersion.V3_7_5, PredefinedVersion.V3_7_5), equalTo(0));
    assertThat(Version.VERSION_COMPARATOR.compare(PredefinedVersion.V3_7_7, PredefinedVersion.V3_7_5), equalTo(1));
    assertThat(Version.VERSION_COMPARATOR.compare(PredefinedVersion.V3_6_13, PredefinedVersion.V3_7_7), equalTo(-1));
  }
}