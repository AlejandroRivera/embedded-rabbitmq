package io.arivera.oss.embedded.rabbitmq.util;

import org.junit.Test;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomPortSupplierTest {

  @Test
  public void testRandomPortIsReturned() throws IOException {
    int port = new RandomPortSupplier().get();
    assertThat(port, not(equalTo(0)));
  }

  @Test
  public void testPortIsAvailable() throws IOException {
    int port = new RandomPortSupplier().get();
    ServerSocket serverSocket = new ServerSocket(port);
    assertThat(serverSocket, notNullValue());
    serverSocket.close();
  }

  @Test(expected = IllegalStateException.class)
  public void testPortCantBeAssigned() throws IOException {
    ServerSocketFactory factory = mock(ServerSocketFactory.class);
    when(factory.createServerSocket(anyInt()))
        .thenThrow(new IOException("Fake"));
    new RandomPortSupplier(factory).get();

  }
}