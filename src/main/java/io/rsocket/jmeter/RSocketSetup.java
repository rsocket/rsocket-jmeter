package io.rsocket.jmeter;

import io.rsocket.core.RSocketClient;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import java.io.Serializable;
import java.time.Duration;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

public class RSocketSetup extends AbstractTestElement
    implements TestStateListener, LoopIterationListener, NoThreadClone, Serializable {

  private static final long serialVersionUID = 1L;

  private static final String HOST = "RSocketSetup.host"; // $NON-NLS-1$

  private static final String PORT = "RSocketSetup.port"; // $NON-NLS-1$

  private static final String TRANSPORT = "RSocketSetup.transport"; // $NON-NLS-1$

  private static final String TRANSPORT_WS_PATH = "RSocketSetup.transport.ws.path"; // $NON-NLS-1$

  public static final String VAR_CLIENT = "RSocketSetup.client"; // $NON-NLS-1$

  private transient RSocketClient rSocketClient;

  @Override
  public void testStarted() {
    testStarted("local");
  }

  @Override
  public void testStarted(String host) {
    ClientTransport clientTransport;
    switch (getTransport().toUpperCase()) {
      case "TCP":
        clientTransport = TcpClientTransport.create(getHost(), getPort());
        break;
      case "WS":
        clientTransport = WebsocketClientTransport
            .create(HttpClient.create().host(getHost()).port(getPort()),
                getTransportWebsocketPath());
        break;
      default:
        throw new IllegalArgumentException(
            "Unsupported ClientTransport[" + getTransport().toUpperCase() + "]");
    }

    this.rSocketClient = RSocketConnector
        .create()
        .payloadDecoder(PayloadDecoder.ZERO_COPY)
        .reconnect(Retry.backoff(10, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(5)))
        .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())
        .dataMimeType(WellKnownMimeType.APPLICATION_OCTET_STREAM.getString())
        .connect(clientTransport)
        .as(RSocketClient::from);
  }

  @Override
  public void testEnded() {
    testEnded("local");
  }

  @Override
  public void testEnded(String host) {
    rSocketClient.dispose();
  }


  @Override
  public void iterationStart(LoopIterationEvent iterEvent) {
    JMeterVariables variables = JMeterContextService.getContext().getVariables();

    variables.putObject(VAR_CLIENT, rSocketClient);
  }

  public void setTransport(String transport) {
    setProperty(TRANSPORT, transport);
  }

  public String getTransport() {
    return getPropertyAsString(TRANSPORT);
  }

  public void setTransportWebsocketPath(String transport) {
    setProperty(TRANSPORT_WS_PATH, transport);
  }

  public String getTransportWebsocketPath() {
    return getPropertyAsString(TRANSPORT_WS_PATH);
  }

  public void setHost(String host) {
    setProperty(HOST, host);
  }

  public String getHost() {
    return getPropertyAsString(HOST);
  }

  public void setPort(int port) {
    setProperty(PORT, port);
  }

  public int getPort() {
    return getPropertyAsInt(PORT);
  }
}
