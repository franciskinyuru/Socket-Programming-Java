package socket.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3000); // Set the number of threads in the pool
        executor.setMaxPoolSize(3000); // Set the maximum number of threads in the pool
        executor.setQueueCapacity(3000); // Set the queue capacity for pending tasks
        executor.initialize();
        return executor;
    }
}
