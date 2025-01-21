package org.apache.bigtop.manager.agent.grpc.config;

import org.apache.bigtop.manager.agent.grpc.interceptor.TaskInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

@Configuration
public class GrpcServerConfig {

    @Bean
    public GrpcServerConfigurer configurer() {
        return serverBuilder -> serverBuilder.intercept(new TaskInterceptor());
    }
}
