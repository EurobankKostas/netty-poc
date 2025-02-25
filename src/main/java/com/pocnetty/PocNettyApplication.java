package com.pocnetty;

import com.pocnetty.infrastructure.MatchingEngineClient;
import com.pocnetty.infrastructure.MatchingEngineServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@SpringBootApplication
@Slf4j
@ConditionalOnProperty(name = "app.runServer", havingValue = "true", matchIfMissing = true)
public class PocNettyApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(PocNettyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int port = 8081;
        new Thread(() -> {
            try {
                new MatchingEngineServer(port).run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).start();
        Thread.sleep(1000);
        new MatchingEngineClient("localhost", port).run();
    }
}
