package io.rsocket.jmeter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.threads.JMeterVariables;
import reactor.core.publisher.Mono;

public class ReactiveVariableHolder {

    private static final String KEY = "ReactiveVariableHolder.map";

    public static Mono<Map<String, Object>> variables() {
        return Mono.subscriberContext()
                   .filter(c -> c.hasKey(KEY))
                   .map(c -> c.get(KEY));
    }

    @SuppressWarnings("unchecked")
    public static <T> Mono<T> withVariables(Mono<T> in, JMeterVariables variables) {
        Map<String, Object> context;

        if (variables.getObject(KEY) == null) {
            context = new ConcurrentHashMap<>();
            variables.putObject(KEY, context);
        } else {
            context = (Map<String, Object>) variables.getObject(KEY);
        }

        return in.subscriberContext(c -> c.hasKey(KEY) ? c : c.put(KEY, context));
    }
}
