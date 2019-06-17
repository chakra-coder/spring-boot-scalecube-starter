package io.github.eutkin.scalecube.transport;


import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.scalecube.net.Address;
import io.scalecube.services.methods.ServiceMethodRegistry;
import io.scalecube.services.transport.api.ServerTransport;
import io.scalecube.services.transport.api.ServiceMessageCodec;
import io.scalecube.services.transport.rsocket.RSocketServiceAcceptor;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CustomRSocketServerTransport implements ServerTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CustomRSocketServerTransport.class);

  private final ServiceMessageCodec codec;
  private final io.rsocket.transport.ServerTransport<CloseableChannel> serverTransport;

  private CloseableChannel server; // calculated

  public CustomRSocketServerTransport(ServiceMessageCodec codec,
      io.rsocket.transport.ServerTransport<CloseableChannel> serverTransport) {
    this.codec = codec;
    this.serverTransport = serverTransport;
  }

  @Override
  public Address address() {
    InetSocketAddress address = server.address();
    return Address.create(address.getHostString(), address.getPort());
  }

  @Override
  public Mono<ServerTransport> bind(int port, ServiceMethodRegistry methodRegistry) {
    return Mono.defer(() ->
        RSocketFactory.receive()
            .frameDecoder(PayloadDecoder.ZERO_COPY)
            .acceptor(new RSocketServiceAcceptor(codec, methodRegistry))
            .transport(() -> serverTransport)
            .start()
            .doOnSuccess(channel -> this.server = channel)
            .thenReturn(this)
    );
  }

  @Override
  public Mono<Void> stop() {
    return Mono.defer(
        () ->
            Optional.ofNullable(server)
                .map(
                    server -> {
                      server.dispose();
                      return server
                          .onClose()
                          .doOnError(e -> LOGGER.warn("Failed to close server: " + e));
                    })
                .orElse(Mono.empty()));
  }
}
