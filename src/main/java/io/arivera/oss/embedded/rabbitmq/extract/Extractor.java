package io.arivera.oss.embedded.rabbitmq.extract;

public interface Extractor extends Runnable {

  @Override
  void run() throws ExtractionException;

  abstract class Decorator implements Extractor {

    protected final Extractor innerExtractor;

    public Decorator(Extractor innerExtractor) {
      this.innerExtractor = innerExtractor;
    }
  }
}
