package io.arivera.oss.embedded.rabbitmq;

import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PublishingProcessListener extends ProcessListener {

  private final List<Subscriber> subscribers;

  public PublishingProcessListener(Subscriber... subscribers) {
    this.subscribers = new ArrayList<>(Arrays.asList(subscribers));
  }

  @Override
  public void afterFinish(Process process, ProcessResult result) {
    super.afterFinish(process, result);
    for (Subscriber subscriber : subscribers) {
        subscriber.processFinished(result.getExitValue());
    }
  }

  public void addSubscriber(Subscriber subscriber) {
    this.subscribers.add(subscriber);
  }

  interface Subscriber {

    void processFinished(int exitValue);

  }
}
