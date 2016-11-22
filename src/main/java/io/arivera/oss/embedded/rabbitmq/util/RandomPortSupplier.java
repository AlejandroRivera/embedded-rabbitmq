package io.arivera.oss.embedded.rabbitmq.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;

public class RandomPortSupplier {

  private static final Logger LOGGER = LoggerFactory.getLogger(RandomPortSupplier.class);

  private final ServerSocketFactory severSocketFactory;

  public RandomPortSupplier() {
    this(ServerSocketFactory.getDefault());
  }

  public RandomPortSupplier(ServerSocketFactory severSocketFactory) {
    this.severSocketFactory = severSocketFactory;
  }

  /**
   * @return an available port assigned at random by the OS.
   *
   * @throws IllegalStateException if the port cannot be determined.
   */
  public int get() throws IllegalStateException {
    ServerSocket socket = null;
    try {
      socket = this.severSocketFactory.createServerSocket(0);
      socket.setReuseAddress(false);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("Could not determine random port to assign.", e);
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          LOGGER.debug("Couldn't close socket that was temporarily opened to determine random port.", e);
        }
      }
    }
  }
}
