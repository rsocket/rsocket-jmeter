//package io.rsocket.jmeter;
//
//import io.rsocket.Payload;
//import io.rsocket.core.RSocketClient;
//import java.util.HashMap;
//import java.util.Map;
//import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
//import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
//import org.apache.jmeter.samplers.SampleResult;
//import org.apache.jmeter.threads.JMeterVariables;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import reactor.core.publisher.Mono;
//import reactor.core.publisher.MonoProcessor;
//
//public abstract class AbstractReactiveJavaSamplerClient<T extends Payload> extends AbstractJavaSamplerClient {
//
//    private static final String CHAIN = "reactive.chain";
//
//    @Override
//    public void setupTest(JavaSamplerContext context) {
//        Logger log = LoggerFactory.getLogger(this.getClass());
//
//        if (log.isDebugEnabled()) {
//            log.debug("Test setup is started");
//        }
//
//        RSocketClient rSocketClient = (RSocketClient) context.getJMeterVariables().getObject(
//            RSocketSetup.VAR_CLIENT);
//
//        setupTestClient(rSocketClient);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Retrieved broker client from context [{}]", rSocketClient);
//        }
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public SampleResult runTest(JavaSamplerContext context) {
//        Logger log = LoggerFactory.getLogger(this.getClass());
//
//        JMeterVariables variables = context.getJMeterVariables();
//        int iteration = variables.getIteration();
//
//        String key = CHAIN + "." + iteration;
//
//        Mono<Void> chain = (Mono<Void>) variables.getObject(key);
//
//        chain = chain == null ? Mono.empty() : chain;
//
//        if (log.isDebugEnabled()) {
//            log.debug("Running Sample");
//            log.debug("Iteration ID [{}]", iteration);
//            log.debug("Iteration Key [{}]", key);
//        }
//
//        ReactiveSampleResult result = new ReactiveSampleResult();
//        PublisherInstrumentation<T> instrumentation = new PublisherInstrumentation<>(result);
//        Map<String, Object> params = extractParameters(new HashMap<>(), context);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Extracted Params [{}]", params);
//        }
//
//        MonoProcessor<Void> processor = MonoProcessor.create();
//        Mono<Void> chained = chain
//            .then(Mono.defer(() -> {
//                if (log.isDebugEnabled()) {
//                    log.debug("Executing Sample");
//                }
//
//                result.sampleStart();
//                return prepareTestRun(params, instrumentation);
//            }))
//            .transform(mono -> ReactiveVariableHolder.withVariables(mono, variables));
//
//        result.setExecutionResult(processor);
//        variables.putObject(key, chained.subscribeWith(processor));
//
//        return result;
//    }
//
//    protected Map<String, Object> extractParameters(Map<String, Object> params, JavaSamplerContext context) {
//        return params;
//    }
//
//    protected abstract Mono<Void> prepareTestRun(Map<String, Object> parameters, PublisherInstrumentation<T> instrumentation);
//
//    protected abstract void setupTestClient(RSocketClient rSocketClient);
//}
