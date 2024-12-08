// File: membership/async/src/main/java/com/telecom/membership/async/config/OpenApiConfig.java
package com.telecom.membership.point.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger OpenAPI 설정을 위한 Configuration 클래스입니다.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    /**
     * OpenAPI 설정을 구성합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("멤버십 포인트 적립 API")
                        .description("수신된 포인트 적립 이벤트를 처리하는 API입니다.")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
