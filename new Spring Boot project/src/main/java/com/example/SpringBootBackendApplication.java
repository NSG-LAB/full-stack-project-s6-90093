package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
=======
import org.springframework.context.annotation.Bean;
>>>>>>> copilot/worktree-2026-04-06T05-00-30
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableWebMvc
<<<<<<< HEAD
@ConfigurationPropertiesScan
=======
>>>>>>> copilot/worktree-2026-04-06T05-00-30
public class SpringBootBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBackendApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
<<<<<<< HEAD
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }

            @Override
            public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
=======
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
>>>>>>> copilot/worktree-2026-04-06T05-00-30
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:uploads/");
            }
        };
    }
}