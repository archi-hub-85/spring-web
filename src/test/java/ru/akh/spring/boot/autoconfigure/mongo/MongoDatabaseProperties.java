package ru.akh.spring.boot.autoconfigure.mongo;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.core.io.Resource;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;

import ru.akh.spring.validation.constraints.ValidateClassExpression;

/**
 * Configuration of Mongo database.
 */
@ConfigurationProperties(prefix = "spring.data.mongodb")
@ConstructorBinding
@Validated
public class MongoDatabaseProperties {

    @Valid
    private final List<CollectionOptions> collections;

    public MongoDatabaseProperties(List<CollectionOptions> collections) {
        this.collections = collections;
    }

    /**
     * The variety of settings you can use when creating collections.
     */
    public List<CollectionOptions> getCollections() {
        return this.collections;
    }

    /**
     * @see org.springframework.data.mongodb.core.CollectionOptions
     */
    public static class CollectionOptions {

        @NotEmpty
        private final String name;

        private final Long maxDocuments;

        @DataSizeUnit(DataUnit.BYTES)
        private final DataSize size;

        private final Boolean capped;

        private final String collation;

        @Valid
        private final ValidationOptions validationOptions;

        public CollectionOptions(String name, Long maxDocuments, DataSize size, Boolean capped,
                String collation, ValidationOptions validationOptions) {
            this.name = name;
            this.maxDocuments = maxDocuments;
            this.size = size;
            this.capped = capped;
            this.collation = collation;
            this.validationOptions = validationOptions;
        }

        /**
         * Name of the collection. Must not be {@literal null} nor empty.
         */
        public String getName() {
            return name;
        }

        /**
         * The maximum number of documents in the collection. Can be {@literal null}.
         */
        public Long getMaxDocuments() {
            return maxDocuments;
        }

        /**
         * The collection size in bytes, this data space is preallocated. Can be
         * {@literal null}.
         */
        public DataSize getSize() {
            return size;
        }

        /**
         * {@code true} to create a "capped" collection (fixed size with auto-FIFO
         * behavior based on insertion order), {@code false} otherwise. Can be
         * {@literal null}.
         */
        public Boolean getCapped() {
            return capped;
        }

        /**
         * The collation to parse. Can be a simple string like {@code en_US} or a
         * {@link org.bson.Document#parse(String) parsable} document like
         * <code>&#123; 'locale' : '?0' &#125;</code> .
         */
        public String getCollation() {
            return collation;
        }

        /**
         * Validation options for documents being inserted or updated in a collection.
         * Can be {@literal null}.
         */
        public ValidationOptions getValidationOptions() {
            return validationOptions;
        }

        /**
         * @see org.springframework.data.mongodb.core.CollectionOptions.ValidationOptions
         */
        @ValidateClassExpression(value = "(#this.schema == null) != (#this.type == null)", message = "Schema or type must be defined.")
        public static class ValidationOptions {

            private final Resource schema;

            private final Class<?> type;

            private final ValidationLevel level;

            private final ValidationAction action;

            public ValidationOptions(Resource schema, Class<?> type, ValidationLevel level, ValidationAction action) {
                this.schema = schema;
                this.type = type;
                this.level = level;
                this.action = action;
            }

            /**
             * MongoDB-specific JSON schema object.
             */
            public Resource getSchema() {
                return schema;
            }

            /**
             * Fully qualified name of the domain type.
             * 
             * @see org.springframework.data.mongodb.core.MongoJsonSchemaCreator#createSchemaFor(Class)
             */
            public Class<?> getType() {
                return type;
            }

            /**
             * Determines how strictly MongoDB applies the validation rules to existing
             * documents during an insert or update. Can be {@literal null}.
             */
            public ValidationLevel getLevel() {
                return level;
            }

            /**
             * Determines whether to error on invalid documents or just warn about the
             * violations but allow invalid documents. Can be {@literal null}.
             */
            public ValidationAction getAction() {
                return action;
            }

        }

    }

}
