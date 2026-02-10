package com.nexashop.api.config;

import com.nexashop.api.util.UploadUtil;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadBaseDir;

    public WebConfig(@Value("${app.upload.dir:}") String uploadBaseDir) {
        this.uploadBaseDir = uploadBaseDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = UploadUtil.resolveBaseDir(uploadBaseDir);
        String location = uploadDir.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
