package org.springframework.boot.web.embedded.netty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

public class RSocketNettyWebServerFactory extends NettyReactiveWebServerFactory {

  private List<NettyServerCustomizer> serverCustomizers = new ArrayList<>();

  private Duration lifecycleTimeout;

  private boolean useForwardHeaders;

  private final TcpServer tcpServer;

  public RSocketNettyWebServerFactory(TcpServer tcpServer) {
    this.tcpServer = tcpServer;
  }

  @Override
  public WebServer getWebServer(HttpHandler httpHandler) {
    ReactorHttpHandlerAdapter handlerAdapter = new ReactorHttpHandlerAdapter(
        httpHandler);
    HttpServer httpServer = createHttpServer();
    return new NettyWebServer(httpServer, handlerAdapter, this.lifecycleTimeout);
  }

  private HttpServer createHttpServer() {
    HttpServer server = HttpServer.from(tcpServer);
    if (getSsl() != null && getSsl().isEnabled()) {
      SslServerCustomizer sslServerCustomizer = new SslServerCustomizer(getSsl(),
          getHttp2(), getSslStoreProvider());
      server = sslServerCustomizer.apply(server);
    }
    if (getCompression() != null && getCompression().getEnabled()) {
        CompressionCustomizer compressionCustomizer = new CompressionCustomizer(
          getCompression());
      server = compressionCustomizer.apply(server);
    }
    server = server.protocol(listProtocols()).forwarded(this.useForwardHeaders);
    return applyCustomizers(server);
  }

  @Override
  public void setLifecycleTimeout(Duration lifecycleTimeout) {
    this.lifecycleTimeout = lifecycleTimeout;
  }

  @Override
  public void setUseForwardHeaders(boolean useForwardHeaders) {
    this.useForwardHeaders = useForwardHeaders;
  }

  @Override
  public List<NettyServerCustomizer> getServerCustomizers() {
    return serverCustomizers;
  }

  @Override
  public void setServerCustomizers(
      Collection<? extends NettyServerCustomizer> serverCustomizers) {
    Assert.notNull(serverCustomizers, "ServerCustomizers must not be null");
    this.serverCustomizers = new ArrayList<>(serverCustomizers);
  }

  private HttpServer applyCustomizers(HttpServer server) {
    for (NettyServerCustomizer customizer : this.serverCustomizers) {
      server = customizer.apply(server);
    }
    return server;
  }

  private HttpProtocol[] listProtocols() {
    if (getHttp2() != null && getHttp2().isEnabled()) {
      if (getSsl() != null && getSsl().isEnabled()) {
        return new HttpProtocol[] { HttpProtocol.H2, HttpProtocol.HTTP11 };
      }
      else {
        return new HttpProtocol[] { HttpProtocol.H2C, HttpProtocol.HTTP11 };
      }
    }
    return new HttpProtocol[] { HttpProtocol.HTTP11 };
  }
}
