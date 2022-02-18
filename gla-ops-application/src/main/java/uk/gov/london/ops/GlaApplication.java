/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.support.StandardServletEnvironment;
import uk.gov.london.ops.framework.calendar.AcademicCalendar;
import uk.gov.london.ops.framework.calendar.FinancialCalendar;
import uk.gov.london.ops.framework.filter.BeanPropertyFilter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import(SwaggerConfig.class)
public class GlaApplication extends SpringBootServletInitializer {

    @Autowired
    JdbcTemplate template;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    BeanPropertyFilter beanPropertyFilter;

    @PostConstruct
    public void setup() {
        FilterProvider filters = new SimpleFilterProvider().addFilter("roleBasedFilter", beanPropertyFilter);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setFilterProvider(filters);
    }

    @Bean
    public JdbcLockRegistry getLockRegistry() {
        DataSource ds = template.getDataSource();   // There must be a better way to do this
        DefaultLockRepository repo = new DefaultLockRepository(ds);
        repo.afterPropertiesSet();
        return new JdbcLockRegistry(repo);
    }

    @Bean
    public static ConfigurableEnvironment configurableEnvironment() {
        return new StandardServletEnvironment();
    }

    @Bean(name = "financialCalendar")
    public FinancialCalendar financialCalendar() {
        return new FinancialCalendar();
    }

    @Bean(name = "academicCalendar")
    public AcademicCalendar academicCalendar() {
        return new AcademicCalendar();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        setRegisterErrorPageFilter(false);
        return application.sources(GlaApplication.class);
    }

    public static void main(String[] args) {
        if ((args.length == 2) && (args[0].equals("-hash"))) {
            passwordHash(args[1]);
        } else if ((args.length == 1) && args[0].equals("-version")) {
            showVersion();
        } else {
            SpringApplication.run(GlaApplication.class, args);
        }
    }

    private static void showVersion() {
        Properties config = new Properties();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:application.properties");
            config.load(resources[0].getInputStream());

            System.out.println("GLA OPS " + config.getProperty("app.release") + "." + config.getProperty("app.build"));
        } catch (IOException e) {
            System.err.println("Unable to load application properties: " + e.getMessage());
        }
    }

    /**
     * Generate a password hash that can be stored in the Users table.
     */
    private static void passwordHash(String password) {
        String passwordHash = new BCryptPasswordEncoder().encode(password);
        System.out.println("RawHash:" + passwordHash);
        String passwordBase64 = Base64.getEncoder().encodeToString(passwordHash.getBytes(StandardCharsets.UTF_8));
        System.out.println("Base64EncodedHash:" + passwordBase64);
    }

}
