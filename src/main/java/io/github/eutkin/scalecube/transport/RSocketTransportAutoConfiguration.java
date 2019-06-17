package io.github.eutkin.scalecube.transport;

import io.rsocket.transport.netty.server.CloseableChannel;
import io.scalecube.services.transport.api.ClientTransport;
import io.scalecube.services.transport.api.ServerTransport;
import io.scalecube.services.transport.api.ServiceMessageCodec;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@ConditionalOnClass(RSocketServiceTransport.class)
public class RSocketTransportAutoConfiguration {



  @Bean
  public ServerTransport rSocketServerTransport(ServiceMessageCodec codec,
      io.rsocket.transport.ServerTransport<CloseableChannel> serverTransport) {
    return new CustomRSocketServerTransport(codec, serverTransport);
  }

  @Bean
  public ClientTransport rSocketClientTransport(
      ClientTransportFactory clientTransportFactory,
      ServiceMessageCodec codec
  ) {
    return new CustomRSocketClientTransport(codec, clientTransportFactory);
  }

}
