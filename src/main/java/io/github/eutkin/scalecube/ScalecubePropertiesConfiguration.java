package io.github.eutkin.scalecube;

import io.github.eutkin.scalecube.properties.ScalecubeProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootConfiguration
@EnableConfigurationProperties(ScalecubeProperties.class)
public class ScalecubePropertiesConfiguration {

}
