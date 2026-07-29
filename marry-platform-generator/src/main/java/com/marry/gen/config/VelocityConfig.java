package com.marry.gen.config;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class VelocityConfig {

    @Bean
    public VelocityEngine velocityEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "class");
        props.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("resource.default_encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        props.setProperty("velocimacro.library.path", "");
        props.setProperty("template.runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);
        return engine;
    }
}