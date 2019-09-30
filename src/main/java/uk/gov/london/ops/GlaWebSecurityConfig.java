/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import uk.gov.london.ops.service.UserService;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@ServletComponentScan(basePackages =  {"uk.gov.london.ops.web.filter"})
public class GlaWebSecurityConfig extends WebSecurityConfigurerAdapter {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    /**
     * How long to delay (in ms) before sending response to a failed logon request.
     */
    @Value("${failed-logon-delay-ms}")
    int failedLogonDelayMs;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // TODO : this is temp, we should create a separate task (UI and backend) for enabling this
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/sysops/**").hasAnyRole("OPS_ADMIN","TECH_ADMIN")
//                .antMatchers("/api/v1/support/sql/update/**").access("hasIpAddress('127.0.0.1') or true")
                .antMatchers("/**").permitAll();


        http.formLogin()
                // this will disable redirect after login success or failure
                .successHandler((request, response, authentication) -> {/*nothing*/})
                .failureHandler((request, response, e) -> {
                    delayBeforeSendingAuthenticationFailure();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .permitAll();

        http.httpBasic();

        http.exceptionHandling()
                // this avoids the redirect to the login screen when auth fails
                .authenticationEntryPoint((request, response, e) -> {
                    delayBeforeSendingAuthenticationFailure();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                });


        http.logout()
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .permitAll();
    }

    private void delayBeforeSendingAuthenticationFailure() {
        try {
            log.debug("Sleeping for {} ms after failed logon attempt", failedLogonDelayMs);
            Thread.sleep(failedLogonDelayMs);
        }
        catch (InterruptedException ie) {
        }
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
