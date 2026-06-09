package cineconecta.cineconecta.config; // Add this line at the top

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        // You can configure ObjectMapper here if needed
        ObjectMapper mapper = new ObjectMapper();
        // mapper.enable(SerializationFeature.INDENT_OUTPUT); // Example: pretty print JSON
        return mapper;
    }
}