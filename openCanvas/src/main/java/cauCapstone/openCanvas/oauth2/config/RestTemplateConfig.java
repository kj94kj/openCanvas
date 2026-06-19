package cauCapstone.openCanvas.oauth2.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//외부 HTTP API 호출에 사용할 RestTemplate을 Bean으로 등록한다.
//OAuth2 연결 해제 등 서버 간 API 요청에서 재사용할 수 있다.
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}