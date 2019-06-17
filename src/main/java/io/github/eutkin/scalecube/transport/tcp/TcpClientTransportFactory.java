package io.github.eutkin.scalecube.transport.tcp;

import io.github.eutkin.scalecube.transport.ClientTransportFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.scalecube.net.Address;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;

class TcpClientTransportFactory implements ClientTransportFactory {

  private final LoopResources loopResources;

  public TcpClientTransportFactory(LoopResources loopResources) {
    this.loopResources = loopResources;
  }

  @Override
  public TcpClientTransport create(Address address) {
    TcpClient tcpClient = TcpClient.newConnection() // create non-pooled
        .runOn(loopResources)
        .host(address.host())
        .port(address.port());
    return TcpClientTransport.create(tcpClient);
  }
}
