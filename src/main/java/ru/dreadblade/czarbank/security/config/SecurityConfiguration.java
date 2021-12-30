package ru.dreadblade.czarbank.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.dreadblade.czarbank.security.exception.FilterChainExceptionHandler;
import ru.dreadblade.czarbank.security.filter.JsonWebTokenAuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final JsonWebTokenAuthorizationFilter authorizationFilter;
    private final FilterChainExceptionHandler filterChainExceptionHandler;

    @Autowired
    public SecurityConfiguration(JsonWebTokenAuthorizationFilter authorizationFilter, FilterChainExceptionHandler filterChainExceptionHandler) {
        this.authorizationFilter = authorizationFilter;
        this.filterChainExceptionHandler = filterChainExceptionHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filterChainExceptionHandler, JsonWebTokenAuthorizationFilter.class)
                .csrf().ignoringAntMatchers("/api/**")
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic().disable()
                .formLogin().disable()
                .rememberMe().disable()
                .logout().disable();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
