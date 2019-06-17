package io.github.eutkin.scalecube.transport.tcp;

import io.github.eutkin.scalecube.properties.ScalecubeProperties;
import io.github.eutkin.scalecube.properties.ScalecubeProperties.Transport;
import io.github.eutkin.scalecube.transport.ClientTransportFactory;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.scalecube.services.transport.rsocket.DelegatedLoopResources;
import io.scalecube.services.transport.rsocket.RSocketTransportResources;
import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;

@SpringBootConfiguration
@Import({ReactiveWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class})
public class TcpConfiguration {


  @Bean
  public RSocketTransportResources rSocketTransportResources() {
    return new RSocketTransportResources();
  }

  @Bean(name = "client-loop-resource", destroyMethod = "close")
  @ConditionalOnBean(RSocketTransportResources.class)
  public CloseableLoopResource clientLoopResource(RSocketTransportResources transportResources) {
    LoopResources loopResources = transportResources
        .workerPool()
        .<LoopResources>map(DelegatedLoopResources::newClientLoopResources)
        .orElse(LoopResources.create("rsocket-worker"));
    return new CloseableLoopResource(loopResources);
  }

  @Bean(name = "server-loop-resource", destroyMethod = "close")
  @ConditionalOnBean(RSocketTransportResources.class)
  public CloseableLoopResource serverLoopResource(RSocketTransportResources transportResources) {
    LoopResources loopResources = transportResources
        .workerPool()
        .<LoopResources>map(DelegatedLoopResources::newServerLoopResources)
        .orElse(LoopResources.create("scalecube"));
    return new CloseableLoopResource(loopResources);
  }

  @Bean
  public TcpServer tcpServer(ScalecubeProperties properties,
      @Qualifier("server-loop-resource") LoopResources loopResources) {
    Integer port = properties.getTransportIfExist().flatMap(Transport::getPortIfExists).orElse(0);
    return TcpServer.create()
        .runOn(loopResources)
        .addressSupplier(() -> new InetSocketAddress(port));
  }

  @Bean
  public ServerTransport<CloseableChannel> tcpServerTransport(TcpServer tcpServer) {
    return TcpServerTransport.create(tcpServer);
  }

  @Bean
  public ClientTransportFactory clientTransportFactory(@Qualifier("client-loop-resource") LoopResources loopResources) {
    return new TcpClientTransportFactory(loopResources);
  }

}
