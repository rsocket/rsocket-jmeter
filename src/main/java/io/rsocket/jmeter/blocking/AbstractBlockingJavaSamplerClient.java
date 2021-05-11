//package io.rsocket.jmeter.blocking;
//
//import io.rsocket.jmeter.RSocketSetup;
//import io.rsocket.jmeter.ProtobufSampleResult;
//import java.util.Arrays;
//
//import com.google.protobuf.GeneratedMessageV3;
//import com.netifi.broker.BrokerClient;
//import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
//import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
//import org.apache.jmeter.samplers.SampleResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public abstract class AbstractBlockingJavaSamplerClient extends AbstractJavaSamplerClient {
//
//    @Override
//    public void setupTest(JavaSamplerContext context) {
//        Logger log = LoggerFactory.getLogger(this.getClass());
//
//        if (log.isDebugEnabled()) {
//            log.debug("Test setup is started");
//        }
//
//        BrokerClient brokerClient = (BrokerClient) context.getJMeterVariables().getObject(
//            RSocketSetup.VAR_CLIENT);
//
//        setupTestClient(brokerClient);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Retrieved broker client from context [{}]", brokerClient);
//        }
//    }
//
//    @Override
//    public SampleResult runTest(JavaSamplerContext context) {
//        Logger log = LoggerFactory.getLogger(this.getClass());
//
//        if (log.isDebugEnabled()) {
//            log.debug("Running Sample");
//        }
//
//        ProtobufSampleResult sample = new ProtobufSampleResult();
//        sample.sampleStart();
//
//        try {
//            GeneratedMessageV3 response = doCall(context, sample::connectEnd);
//
//            sample.latencyEnd();
//
//            if (response != null) {
//                sample.setMessage(response);
//            }
//
//            sample.setSuccessful(true);
//            sample.sampleEnd();
//        } catch (Throwable t) {
//            sample.latencyEnd();
//
//            if (log.isErrorEnabled()) {
//                log.error("Sample Result [{}] Finished with error", sample);
//                log.error("Error ", t);
//            }
//
//            sample.setDataType("text");
//            sample.setResponseData(Arrays.toString(t.getStackTrace())
//                                         .getBytes());
//            sample.setResponseMessage(t.getMessage());
//            sample.setSuccessful(false);
//            sample.sampleEnd();
//        }
//
//        return sample;
//    }
//
//    protected abstract void setupTestClient(BrokerClient brokerClient);
//
//    protected abstract GeneratedMessageV3 doCall(JavaSamplerContext context, Runnable beforeCall);
//}
