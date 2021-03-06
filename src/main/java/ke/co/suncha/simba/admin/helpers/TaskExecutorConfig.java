package ke.co.suncha.simba.admin.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by manyala on 5/26/15.
 */
@Configuration
public class TaskExecutorConfig {
//    @Bean
//    public ThreadPoolTaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
//        pool.setCorePoolSize(2);
//        pool.setMaxPoolSize(5);
//        pool.setWaitForTasksToCompleteOnShutdown(true);
//        return pool;
//    }
//
    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        Executor executor= Executors.newScheduledThreadPool(2);
        return executor;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        //mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
        //mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        return converter;
    }
}
