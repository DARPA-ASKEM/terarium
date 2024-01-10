package software.uncharted.terarium.hmiserver.service.neo4j;

import javax.annotation.PostConstruct;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Neo4jService {

	@Value("${spring.data.neo4j.uri}")
	private String uri;

	@Value("${spring.data.neo4j.authentication.username}")
	private String username;

	@Value("${spring.data.neo4j.authentication.password}")
	private String password;

	private Driver driver;

	@PostConstruct
	public void init() {
		log.info("Connecting to Neo4j at {}", uri);
		driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
	}

	public Session getSession() {
		return driver.session();
	}
}
