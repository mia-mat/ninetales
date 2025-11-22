package ws.mia.ninetales.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfiguration {

	public static final String USERS_COLLECTION_NAME;
	static {
		String env = System.getenv("NINETALES_USERS_COLLECTION_NAME");
		USERS_COLLECTION_NAME = env != null	? env : "users";
	}

	@Bean
	public MongoClient mongoClient() {
		String uri = System.getenv("NINETALES_MONGO_URI");
		if (uri == null || uri.isBlank()) {
			throw new IllegalStateException("NINETALES_MONGO_URI environment variable must be set!");
		}

		ConnectionString connectionString = new ConnectionString(uri);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.applyToConnectionPoolSettings(builder -> builder
						.maxSize(100)
						.minSize(5)
						.maxWaitTime(5, TimeUnit.SECONDS))
				.build();

		return MongoClients.create(settings);
	}

	@Bean
	public String mongoDatabaseName() {
		String uri = System.getenv("NINETALES_MONGO_URI");
		ConnectionString connectionString = new ConnectionString(uri);
		String db = connectionString.getDatabase();
		if (db == null) {
			throw new IllegalStateException("No database name specified in NINETALES_MONGO_URI");
		}
		return db;
	}

	@Bean
	public MongoDatabase mongoDatabase(MongoClient mongoClient, String mongoDatabaseName) {
		return mongoClient.getDatabase(mongoDatabaseName);
	}

	@Bean
	public MongoCollection<Document> routesCollection(MongoDatabase database) {
		return database.getCollection(USERS_COLLECTION_NAME);
	}

}
