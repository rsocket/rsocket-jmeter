package io.rsocket.jmeter;

import java.util.concurrent.atomic.LongAdder;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactiveResultCollector extends ResultCollector {

    private static final Logger log = LoggerFactory.getLogger(ReactiveResultCollector.class);

    private static final long serialVersionUID = 1L;

    private static final String RAMP_DOWN_TIME = "ramp.down.time";
    private static final int RAMP_DOWN_TIME_DEFAULT = 0; // milliseconds

    final LongAdder adder = new LongAdder();

    public ReactiveResultCollector() {
        this(new Summariser("summariser"));
    }

    public ReactiveResultCollector(Summariser summariser) {
        super(summariser);
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        ReactiveSampleResult result = (ReactiveSampleResult) event.getResult();
        adder.add(1);
        result.getExecutionResult()
              .subscribe(
                  __ -> {},
                  t -> {
                      try {
                          if (result.isValid()) {
                              super.sampleOccurred(event);
                          }
                          else {
                              log.debug("Invalid sample occurred");
                          }
                      } finally {
                          adder.add(-1);
                      }
                  },
                  () -> {
                      try {
                          super.sampleOccurred(event);
                      } finally {
                          adder.add(-1);
                      }
                  }
              );
    }

    @Override
    public void testEnded(String host) {
        long counter = adder.longValue();
        int rampDownTime = getPropertyAsInt(RAMP_DOWN_TIME, RAMP_DOWN_TIME_DEFAULT);
        while (counter > 0 && rampDownTime > 0) {
            rampDownTime -= 100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        super.testEnded(host);
    }

}
