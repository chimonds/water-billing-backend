package ke.co.suncha.simba.admin.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Created by manyala on 5/26/15.
 */
@Configuration
public class TaskExecutorConfig {
//    @Bean
//    public ThreadPoolTaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
//        pool.setCorePoolSize(10);
//        pool.setMaxPoolSize(20);
//        pool.setWaitForTasksToCompleteOnShutdown(true);
//        return pool;
//    }
//
//    @Bean(destroyMethod = "shutdown")
//    public Executor taskScheduler() {
//        Executor executor= Executors.newScheduledThreadPool(5);
//        return executor;
//    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        //MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);

        return converter;
    }
}
