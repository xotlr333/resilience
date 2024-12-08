// File: membership/point/src/main/java/com/telecom/membership/point/config/R2dbcConfig.java
package com.telecom.membership.point.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableTransactionManagement
public class R2dbcConfig {

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${spring.r2dbc.url}") String url,
            @Value("${spring.r2dbc.username}") String username,
            @Value("${spring.r2dbc.password}") String password) throws URISyntaxException {

        String cleanUrl = url.replace("r2dbc:postgresql://", "");
        URI uri = new URI("postgresql://" + cleanUrl);

        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(uri.getHost())
                        .port(uri.getPort())
                        .database(uri.getPath().substring(1))
                        .username(username)
                        .password(password)
                        .build()
        );
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}