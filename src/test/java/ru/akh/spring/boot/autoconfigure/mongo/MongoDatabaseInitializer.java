package ru.akh.spring.boot.autoconfigure.mongo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.core.log.LogMessage;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.util.FileCopyUtils;

import com.mongodb.client.MongoDatabase;

/**
 * Initialize a {@link MongoDatabase} based on a matching
 * {@link MongoDatabaseProperties} config.
 */
public class MongoDatabaseInitializer {

    private static final Log logger = LogFactory.getLog(MongoDatabaseInitializer.class);

    private final MongoTemplate template;
    private final MongoDatabaseProperties properties;

    public MongoDatabaseInitializer(MongoTemplate template, MongoDatabaseProperties properties) {
        this.template = template;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        properties.getCollections().forEach(this::ensureCollection);
    }

    private void ensureCollection(MongoDatabaseProperties.CollectionOptions options) {
        CollectionOptions collectionOptions = CollectionOptions.empty();
        if (options.getMaxDocuments() != null) {
            collectionOptions = collectionOptions.maxDocuments(options.getMaxDocuments());
        }
        if (options.getSize() != null) {
            collectionOptions = collectionOptions.size(options.getSize().toBytes());
        }
        if (Boolean.TRUE.equals(options.getCapped())) {
            collectionOptions = collectionOptions.capped();
        }
        if (options.getCollation() != null) {
            collectionOptions = collectionOptions.collation(Collation.parse(options.getCollation()));
        }

        MongoDatabaseProperties.CollectionOptions.ValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions != null) {
            String jsonSchema;
            try (Reader reader = new InputStreamReader(validationOptions.getSchema().getInputStream(),
                    StandardCharsets.UTF_8)) {
                jsonSchema = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            MongoJsonSchema schema = MongoJsonSchema.of(Document.parse(jsonSchema));
            collectionOptions.validation(new CollectionOptions.ValidationOptions(Validator.schema(schema),
                    validationOptions.getLevel(), validationOptions.getAction()));
        }

        String collectionName = options.getName();
        template.createCollection(collectionName, collectionOptions);
        if (logger.isDebugEnabled()) {
            logger.debug(LogMessage.format("Collection %s created", collectionName));
        }
    }

}
