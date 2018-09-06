package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.bin.ErlangShell;
import io.arivera.oss.embedded.rabbitmq.bin.ErlangShellException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ErlangVersionCheckerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  private ErlangShell shell;

  @Test
  public void parseR() {
    assertThat(ErlangVersionChecker.parse("R15B03-1"), equalTo(new int[] {15, 66, 3, 0, 0}));
    assertThat(ErlangVersionChecker.parse("R15B"), equalTo(new int[] {15, 66, 0, 0, 0}));
    assertThat(ErlangVersionChecker.parse("R11B-5"), equalTo(new int[] {11, 66, 0, 0, 0}));
  }

  @Test
  public void parseOtp() {
    assertThat(ErlangVersionChecker.parse("18.0"), equalTo(new int[] {18, 0, 0, 0, 0}));
    assertThat(ErlangVersionChecker.parse("18.2.1"), equalTo(new int[] {18, 2, 1, 0, 0}));
    assertThat(ErlangVersionChecker.parse("18.3"), equalTo(new int[] {18, 3, 0, 0, 0}));
    assertThat(ErlangVersionChecker.parse("19.3.6.4"), equalTo(new int[] {19, 3, 6, 4, 0}));
  }

  @Test
  public void minVersionNotMet() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("R11B");
    expectedException.expect(ErlangVersionException.class);
    expectedException.expectMessage(containsString("Minimum required Erlang version"));
    expectedException.expectMessage(containsString("R11B"));
    expectedException.expectMessage(containsString("18.2.1"));

    ErlangVersionChecker checker = new ErlangVersionChecker("18.2.1", shell);
    checker.check();
  }

  @Test
  public void versionCannotBeDetermined() throws ErlangShellException {
    ErlangShellException erlangShellException = new ErlangShellException("fake!");
    when(shell.getErlangVersion()).thenThrow(erlangShellException);
    expectedException.expect(ErlangVersionException.class);
    expectedException.expectMessage(containsString("Could not determine Erlang version"));
    expectedException.expectMessage(containsString("Ensure Erlang is correctly installed"));
    expectedException.expectCause(sameInstance(erlangShellException));

    ErlangVersionChecker checker = new ErlangVersionChecker(RandomStringUtils.random(3), shell);
    checker.check();
  }

  @Test
  public void noRequiredVersion() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("18.2.1");

    String minErlangVersion = null;
    ErlangVersionChecker checker = new ErlangVersionChecker(minErlangVersion, shell);
    checker.check();
  }

  @Test
  public void versionCheckPasses() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("18.1");

    String minErlangVersion = "R14B01-3";
    ErlangVersionChecker checker = new ErlangVersionChecker(minErlangVersion, shell);
    checker.check();
  }

  @Test
  public void versionCheckPasses2() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("18.1.0");
    new ErlangVersionChecker("18.0.1", shell).check();
  }

  @Test(expected = ErlangVersionException.class)
  public void versionCheckFails() throws ErlangShellException {
      when(shell.getErlangVersion()).thenReturn("21.0.1");
      new ErlangVersionChecker("21.1.0", shell).check();
  }

  @Test
  public void versionCannotBeParsedFromActual() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("ABC");
    String minErlangVersion = "R14B01-3";
    ErlangVersionChecker checker = new ErlangVersionChecker(minErlangVersion, shell);
    checker.check();
  }

  @Test
  public void versionCannotBeParsedFromExpected() throws ErlangShellException {
    when(shell.getErlangVersion()).thenReturn("18.2.1");
    String minErlangVersion = "ABC";
    ErlangVersionChecker checker = new ErlangVersionChecker(minErlangVersion, shell);
    checker.check();
  }
}