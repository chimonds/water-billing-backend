package ke.co.suncha.simba.admin.helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
//        pool.setCorePoolSize(10);
//        pool.setMaxPoolSize(20);
//        pool.setWaitForTasksToCompleteOnShutdown(true);
//        return pool;
//    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(6);
    }
}
