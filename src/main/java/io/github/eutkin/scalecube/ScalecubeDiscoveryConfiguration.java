package io.github.eutkin.scalecube;

import io.github.eutkin.scalecube.properties.ScalecubeProperties;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class ScalecubeDiscoveryConfiguration {

    private ScalecubeProperties properties;

    @Bean
    @ConditionalOnClass(ScalecubeServiceDiscovery.class)
    public DiscoveryInitializer discoveryInitializer() {
        return endpoint -> {
            ScalecubeServiceDiscovery serviceDiscovery = new ScalecubeServiceDiscovery(endpoint);
            if (!properties.getDiscoveryIfExists().isPresent()) {
                return serviceDiscovery;
            }
            ScalecubeProperties.Discovery config = properties.getDiscoveryIfExists().get();
            return serviceDiscovery.options(options ->
                    options
                            .port(config.getPort())
                            .connectTimeout(config.getConnectTimeout())
                            .messageCodec(config.getMessageCodec())
                            .maxFrameLength(config.getMaxFrameLength())
                            .syncInterval(config.getSyncInterval())
                            .syncTimeout(config.getSyncTimeout())
                            .syncGroup(config.getSyncGroup())
                            .suspicionMult(config.getSuspicionMult())
                            .metadataTimeout(config.getMetadataTimeout())
                            .pingInterval(config.getPingInterval())
                            .pingTimeout(config.getPingTimeout())
                            .pingReqMembers(config.getPingReqMembers())
                            .gossipInterval(config.getGossipInterval())
                            .gossipFanout(config.getGossipFanout())
                            .gossipRepeatMult(config.getGossipRepeatMult())
                            .memberHost(config.getMemberHost())
                            .memberPort(config.getMemberPort())
                            .addMetadata(config.getMetadata())
                            .seedMembers(config.getSeedMembers())
            );
        };
    }

    @Autowired
    public void setProperties(ScalecubeProperties properties) {
        this.properties = properties;
    }
}
