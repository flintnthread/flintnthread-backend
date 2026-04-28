package com.ecommerce.authdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ImageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String invoiceLocation = Path.of(System.getProperty("user.dir"), "invoices")
                .toUri()
                .toString();

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:C:/ecommerce-images/");

        registry.addResourceHandler("/invoices/**")
                .addResourceLocations(invoiceLocation);
    }
}

