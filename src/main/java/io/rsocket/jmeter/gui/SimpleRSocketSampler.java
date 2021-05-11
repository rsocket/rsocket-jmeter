package io.rsocket.jmeter.gui;

import static io.rsocket.jmeter.RSocketSetup.VAR_CLIENT;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.core.RSocketClient;
import io.rsocket.frame.FrameType;
import io.rsocket.jmeter.RSocketSamplerBase;
import io.rsocket.jmeter.ReactiveSampleResult;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.metadata.TaggingMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.ByteBufPayload;
import java.util.Collections;
import org.apache.jmeter.testelement.property.JMeterProperty;
import reactor.core.publisher.Mono;

public class SimpleRSocketSampler extends RSocketSamplerBase {

  @Override
  protected ReactiveSampleResult sample(String route, String requestType) {
    final RSocketClient rSocketClient = (RSocketClient) getThreadContext().getVariables()
        .getObject(VAR_CLIENT);

    final ReactiveSampleResult sampleResult = new ReactiveSampleResult();

    Mono<Payload> source = Mono.fromCallable(() -> {
      final String data = getData();
      final CompositeByteBuf metadata = ByteBufAllocator.DEFAULT.compositeBuffer();
      CompositeMetadataCodec.encodeAndAddMetadata(metadata, ByteBufAllocator.DEFAULT,
          WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
          TaggingMetadataCodec
              .createTaggingContent(ByteBufAllocator.DEFAULT, Collections.singleton(getRoute())));
      for (JMeterProperty metadatum : getMetadata()) {
        CompositeMetadataCodec
            .encodeAndAddMetadataWithCompression(metadata, ByteBufAllocator.DEFAULT,
                metadatum.getName(),
                ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, metadatum.getStringValue()));
      }
      return ByteBufPayload
          .create(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, data), metadata);
    });

    switch (FrameType.valueOf(getRequestType())) {
      case REQUEST_FNF:
        sampleResult.setExecutionResult(
            rSocketClient
                .fireAndForget(source));
        break;
      case REQUEST_RESPONSE:
        sampleResult.setExecutionResult(
            rSocketClient
                .requestResponse(source));
        break;
      case REQUEST_STREAM:
        sampleResult.setExecutionResult(
            rSocketClient
                .requestStream(source));
        break;
      case REQUEST_CHANNEL:
        sampleResult.setExecutionResult(
            rSocketClient
                .requestChannel(source));
        break;
      case METADATA_PUSH:
        sampleResult.setExecutionResult(
            rSocketClient
                .requestChannel(source));
        break;
    }

    // TODO: has to be nonblocking
    sampleResult.getExecutionResult().block();

    return sampleResult;
  }
}
