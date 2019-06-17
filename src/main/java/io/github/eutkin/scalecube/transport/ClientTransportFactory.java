package io.github.eutkin.scalecube.transport;

import io.rsocket.transport.ClientTransport;
import io.scalecube.net.Address;

public interface ClientTransportFactory {

  ClientTransport create(Address address);

}
