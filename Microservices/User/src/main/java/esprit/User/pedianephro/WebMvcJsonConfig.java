package esprit.User.pedianephro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcJsonConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    public WebMvcJsonConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        boolean jacksonAlreadyPresent = converters.stream()
                .anyMatch(MappingJackson2HttpMessageConverter.class::isInstance);

        if (!jacksonAlreadyPresent) {
            converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        }
    }
}
