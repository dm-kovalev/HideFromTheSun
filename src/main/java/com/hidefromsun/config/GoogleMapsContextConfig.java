package com.hidefromsun.config;

import com.google.maps.GeoApiContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleMapsContextConfig {

    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder().apiKey("").build();
    }
}
