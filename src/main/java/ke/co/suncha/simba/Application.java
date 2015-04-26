package ke.co.suncha.simba;

import com.mangofactory.swagger.plugin.EnableSwagger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
 * This is the main Spring Boot application class. It configures Spring Boot, JPA, Swagger
 */
//Sprint Boot Auto Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "ke.co.suncha.simba")
@EnableJpaRepositories("ke.co.suncha.simba")
@EnableSwagger
// auto generation of API docs
public class Application extends SpringBootServletInitializer {


    private static final Class<Application> applicationClass = Application.class;

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(applicationClass);
    }
}
