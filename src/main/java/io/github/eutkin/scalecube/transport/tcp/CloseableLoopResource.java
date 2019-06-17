package io.github.eutkin.scalecube.transport.tcp;

import java.io.Closeable;
import lombok.experimental.Delegate;
import reactor.netty.resources.LoopResources;

class CloseableLoopResource implements LoopResources, Closeable {

  @Delegate
  private final LoopResources delegate;

  public CloseableLoopResource(LoopResources delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() {
    delegate.dispose();
  }

}
