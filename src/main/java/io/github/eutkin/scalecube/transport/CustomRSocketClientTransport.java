package io.github.eutkin.scalecube.transport;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.scalecube.net.Address;
import io.scalecube.services.transport.api.ClientChannel;
import io.scalecube.services.transport.api.ClientTransport;
import io.scalecube.services.transport.api.ServiceMessageCodec;
import io.scalecube.services.transport.rsocket.RSocketClientChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CustomRSocketClientTransport implements ClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomRSocketClientTransport.class);

  private final ThreadLocal<Map<Address, Mono<RSocket>>> rsockets =
      ThreadLocal.withInitial(ConcurrentHashMap::new);

  private final ServiceMessageCodec codec;
  private final ClientTransportFactory clientTransportFactory;

  /**
   * Constructor for this transport.
   *  @param codec message codec
   * @param clientTransportFactory
   */
  public CustomRSocketClientTransport(ServiceMessageCodec codec,
      ClientTransportFactory clientTransportFactory) {
    this.codec = codec;
    this.clientTransportFactory = clientTransportFactory;
  }

  @Override
  public ClientChannel create(Address address) {
    final Map<Address, Mono<RSocket>> monoMap = rsockets.get(); // keep reference for threadsafety
    Mono<RSocket> rsocket =
        monoMap.computeIfAbsent(address, address1 -> connect(address1, monoMap));
    return new RSocketClientChannel(rsocket, codec);
  }

  private Mono<RSocket> connect(Address address, Map<Address, Mono<RSocket>> monoMap) {
    Mono<RSocket> rsocketMono =
        RSocketFactory.connect()
            .frameDecoder(PayloadDecoder.ZERO_COPY)
            .transport(() -> clientTransportFactory.create(address))
            .start();

    return rsocketMono
        .doOnSuccess(
            rsocket -> {
              LOGGER.info("Connected successfully on {}", address);
              // setup shutdown hook
              rsocket
                  .onClose()
                  .doOnTerminate(
                      () -> {
                        monoMap.remove(address);
                        LOGGER.info("Connection closed on {}", address);
                      })
                  .subscribe(null, th -> LOGGER.warn("Exception on closing rsocket: {}", th.getMessage(), th));
            })
        .doOnError(
            throwable -> {
              LOGGER.warn("Connect failed on {}, cause: {}", address, throwable);
              monoMap.remove(address);
            })
        .cache();
  }
}
