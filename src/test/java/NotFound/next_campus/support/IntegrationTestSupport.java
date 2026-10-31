package NotFound.next_campus.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

@SpringBootTest
public abstract class IntegrationTestSupport {

    private static final int REDIS_PORT = 6379;

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("lostinha-test-db")
            .withUsername("tester")
            .withPassword("test");

    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(REDIS_PORT);

    static {
        // 두 컨테이너를 병렬로 기동
        Startables.deepStart(POSTGRES, REDIS).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // 호스트 포트는 랜덤으로 매핑되므로 로컬 docker-compose redis(6380)와 충돌하지 않는다.
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @AfterEach
    void flushRedis() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        }
    }
}
