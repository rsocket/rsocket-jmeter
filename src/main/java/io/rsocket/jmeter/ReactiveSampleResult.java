package io.rsocket.jmeter;

import io.rsocket.Payload;
import org.apache.jmeter.samplers.SampleResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

@SuppressWarnings("unchecked")
public class ReactiveSampleResult extends SampleResult {

  private static final long serialVersionUID = 1L;

  private boolean isValid;
  private Mono<Void> executionResult;

  public void setExecutionResult(Publisher<?> executionResult) {
    this.executionResult =
        PublisherInstrumentation.instrument((Publisher<Payload>) executionResult, this)
            .subscribeWith(MonoProcessor.create());
  }

  public Mono<Void> getExecutionResult() {
    return this.executionResult;
  }

  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean valid) {
    isValid = valid;
  }
}
