package NotFound.next_campus.global.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private String filePath = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // /uploads/** -> 실제 폴더로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + filePath);
    }
}
