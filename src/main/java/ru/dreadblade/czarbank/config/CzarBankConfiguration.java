package ru.dreadblade.czarbank.config;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import static freemarker.template.Configuration.VERSION_2_3_31;

@Configuration
public class CzarBankConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerClassLoaderConfig() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(VERSION_2_3_31);

        TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "/templates/email");
        configuration.setTemplateLoader(templateLoader);

        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);

        return freeMarkerConfigurer;
    }
}
