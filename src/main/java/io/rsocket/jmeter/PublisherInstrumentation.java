package io.rsocket.jmeter;

import io.netty.buffer.ByteBuf;
import io.rsocket.Payload;
import java.util.Arrays;
import java.util.function.Function;
import org.apache.jmeter.samplers.SampleResult;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Fuseable.QueueSubscription;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

public class PublisherInstrumentation<T extends Payload> {

  public static Mono<Void> instrument(Publisher<Payload> publisher, ReactiveSampleResult sampleResult) {
    return Mono.fromDirect(Operators
        .<Payload, Void>liftPublisher((__, actual) -> new SampleResultSubscriber(actual, sampleResult))
        .apply(publisher));
  }


  static final class SampleResultSubscriber implements CoreSubscriber<Payload>,
      QueueSubscription<Void> {

    private static final Logger log = LoggerFactory.getLogger(PublisherInstrumentation.class);

    final CoreSubscriber<? super Void> actual;
    final ReactiveSampleResult sampleResult;

    Subscription s;

    byte[] data = new byte[0];

    SampleResultSubscriber(CoreSubscriber<? super Void> actual,
        ReactiveSampleResult sampleResult) {
      this.actual = actual;
      this.sampleResult = sampleResult;

      sampleResult.sampleStart();
    }

    @Override
    public void onSubscribe(Subscription s) {
      if (Operators.validate(this.s, s)) {
        this.s = s;

        if (log.isDebugEnabled()) {
          log.debug("Subscribed. Mark sample result [{}] as connected", sampleResult);
        }

        this.actual.onSubscribe(this);

        this.sampleResult.connectEnd();
      }
    }

    @Override
    public void onNext(Payload payload) {
      final ByteBuf nextDataByteBuf = payload.sliceData();
      final byte[] currentData = this.data;
      final byte[] nextData = new byte[nextDataByteBuf.readableBytes() + currentData.length];

      System.arraycopy(currentData, 0, nextData, 0, currentData.length);
      nextDataByteBuf.readBytes(nextData, currentData.length, nextDataByteBuf.readableBytes());

      this.data = nextData;

      payload.release();
    }

    @Override
    public void onError(Throwable t) {
      if (log.isDebugEnabled()) {
        log.debug("Terminated[{}]. Latency end for sample result [{}]", t, sampleResult);
      }
      final ReactiveSampleResult sampleResult = this.sampleResult;

      sampleResult.latencyEnd();

      this.actual.onError(t);

      if (log.isErrorEnabled()) {
        log.error("Sample Result [{}] Finished with error: \n{}", sampleResult, t);
      }

      sampleResult.setDataType(SampleResult.TEXT);
      sampleResult.setResponseData(Arrays.toString(t.getStackTrace()).getBytes());
      sampleResult.setResponseMessage(t.getMessage());
      sampleResult.setSuccessful(false);
      sampleResult.setErrorCount(1);

      sampleResult.sampleEnd();
    }

    @Override
    public void onComplete() {
      if (log.isDebugEnabled()) {
        log.debug("Terminated. Latency end for sample result [{}]", sampleResult);
      }
      final ReactiveSampleResult sampleResult = this.sampleResult;
      sampleResult.latencyEnd();

      this.actual.onComplete();

      if (log.isDebugEnabled()) {
        log.debug("Finished successfully. Sample result [{}]", this.sampleResult);
      }

      sampleResult.setDataType(SampleResult.BINARY);
      sampleResult.setResponseData(this.data);
      sampleResult.setBodySize((long) data.length);
      sampleResult.setSuccessful(true);

      sampleResult.sampleEnd();
    }

    @Override
    public void request(long n) {
      this.s.request(Long.MAX_VALUE);
    }

    @Override
    public void cancel() {
      this.s.cancel();
    }

    @Override
    public int requestFusion(int requestedMode) {
      return Fuseable.NONE;
    }

    @Override
    public Void poll() {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public void clear() {

    }
  }
}
