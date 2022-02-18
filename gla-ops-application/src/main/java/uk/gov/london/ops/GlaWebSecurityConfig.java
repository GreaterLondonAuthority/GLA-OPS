/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uk.gov.london.ops.framework.security.OPSAuthenticationEntryPoint;
import uk.gov.london.ops.user.UserServiceImpl;

import javax.servlet.http.HttpServletResponse;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@ServletComponentScan(basePackages =  {"uk.gov.london.ops.framework.filter"})
public class GlaWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserServiceImpl userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // TODO : this is temp, we should create a separate task (UI and backend) for enabling this
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/sysops/**").access("@userServiceImpl.checkActuatorEndpointAccess(authentication)")
//                .antMatchers("/swagger-ui.html").hasRole(OPS_ADMIN.replace("ROLE_", "")) //ROLE_ pre-pended by spring auth
                .antMatchers("/swagger-ui.html").authenticated()
                // .antMatchers("/api/v1/support/sql/update/**").access("hasIpAddress('127.0.0.1') or true")
                .antMatchers("/**").permitAll();


        // only used by the spring security form page
        http.formLogin()
                // this will disable redirect after login success or failure
                .successHandler((request, response, authentication) -> {/*nothing*/})
                .failureHandler((request, response, e) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                //currently only using spring security login screen for swagger
                .defaultSuccessUrl("/swagger-ui.html", true)
                .permitAll();

        http.httpBasic();

        http.exceptionHandling()
                // this avoids the redirect to the login screen when auth fails for APIs
                .defaultAuthenticationEntryPointFor(opsAuthenticationEntryPoint(), new AntPathRequestMatcher("/api/v1/**"));

        http.logout()
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .permitAll();
    }

    @Bean
    public AuthenticationEntryPoint opsAuthenticationEntryPoint() {
        return new OPSAuthenticationEntryPoint();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
