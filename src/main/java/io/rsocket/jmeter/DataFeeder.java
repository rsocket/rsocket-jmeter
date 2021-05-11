package io.rsocket.jmeter;

import io.rsocket.Payload;

public interface DataFeeder<T extends Payload> {

    T next();
}
