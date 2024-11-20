package com.example.CheckrApplication.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    private ModelMapper modelMapper;

    public ModelMapperConfig(){
        this.modelMapper= new ModelMapper();
    }
    @Bean
    public ModelMapper modelMapper() {
        return this.modelMapper;
    }


}
