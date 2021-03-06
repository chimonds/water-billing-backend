package ke.co.suncha.simba;

import com.mangofactory.swagger.plugin.EnableSwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//Sprint Boot Auto Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "ke.co.suncha.simba")
@EnableJpaRepositories("ke.co.suncha.simba")
@EnableSwagger
@EnableScheduling
@EnableTransactionManagement
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
