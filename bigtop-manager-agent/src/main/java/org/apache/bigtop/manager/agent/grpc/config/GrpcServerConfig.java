package org.apache.bigtop.manager.agent.grpc.config;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.apache.bigtop.manager.agent.grpc.interceptor.TaskInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public GrpcServerConfigurer configurer() {
        return serverBuilder -> serverBuilder.intercept(new TaskInterceptor());
    }
}
