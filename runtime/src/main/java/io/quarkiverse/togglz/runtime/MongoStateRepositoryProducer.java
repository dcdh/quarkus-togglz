package io.quarkiverse.togglz.runtime;

import java.util.Optional;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;
import org.togglz.core.repository.StateRepository;
import org.togglz.mongodb.MongoStateRepository;

import com.mongodb.client.MongoClient;

import io.quarkus.arc.DefaultBean;

public class MongoStateRepositoryProducer {
    private static final Logger LOGGER = Logger.getLogger(MongoStateRepositoryProducer.class);

    @Produces
    @DefaultBean
    @Singleton
    public StateRepository stateRepositoryProducer(final MongoClient mongoClient) {
        final String dbName = Optional.ofNullable(mongoClient.listDatabaseNames().first())
                .orElseThrow(() -> new IllegalStateException("Should not be here"));
        LOGGER.infov("Creating MongoDB State Repository using dbname {0}", dbName);
        return MongoStateRepository.newBuilder(mongoClient, dbName)
                .build();
    }
}
