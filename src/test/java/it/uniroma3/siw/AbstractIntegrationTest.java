package it.uniroma3.siw;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base per i test di integrazione: avvia un PostgreSQL reale "usa e getta"
 * con Testcontainers invece di un H2 in-memory, cosi' i test girano sullo
 * stesso database della produzione. Il container e' static: uno solo per
 * JVM, condiviso da tutte le classi di test che estendono questa.
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		registry.add("spring.sql.init.mode", () -> "never");
	}
}
