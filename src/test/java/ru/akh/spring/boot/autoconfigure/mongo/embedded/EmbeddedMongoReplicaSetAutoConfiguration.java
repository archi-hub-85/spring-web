package ru.akh.spring.boot.autoconfigure.mongo.embedded;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoClientDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.log.LogMessage;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.util.unit.DataSize;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongoCmdOptions;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;
import ru.akh.spring.boot.autoconfigure.mongo.embedded.EmbeddedMongoReplicaSetAutoConfiguration.EmbeddedMongoClientDependsOnBeanFactoryPostProcessor;
import ru.akh.spring.boot.autoconfigure.mongo.embedded.EmbeddedMongoReplicaSetProperties.Member;

/**
 * @see <a href=
 *      "https://docs.mongodb.com/manual/tutorial/deploy-replica-set-for-testing/">Deploy
 *      a Replica Set for Testing and Development</a>
 *
 * @see de.flapdoodle.embed.mongo.tests.MongosSystemForTestFactory#initializeReplicaSet()
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ MongoProperties.class, EmbeddedMongoProperties.class,
        EmbeddedMongoReplicaSetProperties.class })
@AutoConfigureBefore(MongoAutoConfiguration.class)
@ConditionalOnClass({ MongoClientSettings.class, MongodStarter.class })
@Import({ EmbeddedMongoClientDependsOnBeanFactoryPostProcessor.class })
public class EmbeddedMongoReplicaSetAutoConfiguration {

    private static final Log logger = LogFactory.getLog(EmbeddedMongoReplicaSetAutoConfiguration.class);

    private final MongoProperties properties;
    private final EmbeddedMongoProperties embeddedProperties;
    private final EmbeddedMongoReplicaSetProperties embeddedReplicaSetProperties;

    public EmbeddedMongoReplicaSetAutoConfiguration(MongoProperties properties,
            EmbeddedMongoProperties embeddedProperties,
            EmbeddedMongoReplicaSetProperties embeddedReplicaSetProperties) {
        this.properties = properties;
        this.embeddedProperties = embeddedProperties;
        this.embeddedReplicaSetProperties = embeddedReplicaSetProperties;
    }

    @Bean
    public MongodExecutable embeddedMongoServer() {
        // empty bean to prevent creation by EmbeddedMongoAutoConfiguration
        return null;
    }

    @Bean
    public IMongodConfig embeddedMongoConfiguration() {
        // empty bean to prevent creation by EmbeddedMongoAutoConfiguration
        return null;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MongoServersBean embeddedMongoServers(IRuntimeConfig runtimeConfig, ApplicationContext context)
            throws IOException {
        List<IMongodConfig> mongodConfigs = embeddedMongoConfigurations();

        Integer configuredPort = properties.getPort();
        if (configuredPort == null || configuredPort == 0) {
            setEmbeddedPort(context, mongodConfigs.get(0).net().getPort());
        }

        List<MongodExecutable> servers = new ArrayList<>(mongodConfigs.size());
        for (IMongodConfig mongodConfig : mongodConfigs) {
            MongodStarter mongodStarter = getMongodStarter(runtimeConfig);
            MongodExecutable server = mongodStarter.prepare(mongodConfig);
            servers.add(server);
        }

        return new MongoServersBean(servers, mongodConfigs);
    }

    private MongodStarter getMongodStarter(IRuntimeConfig runtimeConfig) {
        if (runtimeConfig == null) {
            return MongodStarter.getDefaultInstance();
        }
        return MongodStarter.getInstance(runtimeConfig);
    }

    static class MongoServersBean {

        private final List<MongodExecutable> servers;
        private final List<IMongodConfig> serverConfigs;

        public MongoServersBean(List<MongodExecutable> servers, List<IMongodConfig> serverConfigs) {
            this.servers = servers;
            this.serverConfigs = serverConfigs;
        }

        public void start() throws Exception {
            logger.info("Starting servers...");
            try {
                for (MongodExecutable server : servers) {
                    server.start();
                }

                Thread.sleep(1000);

                initiateReplicaSet(serverConfigs);
                logger.info("Servers successfully started");
            } catch (Exception e) {
                logger.warn("Cannon start servers", e);
                stop();
                throw e;
            }
        }

        // TODO Once the primary server couldn't stop -> had to kill the process through
        // Task Manager
        public void stop() {
            logger.info("Stopping servers...");
            for (int i = servers.size() - 1; i >= 0; i--) {
                MongodExecutable server = servers.get(i);
                try {
                    server.stop();
                    if (logger.isInfoEnabled()) {
                        logger.info(LogMessage.format("Server #%s stopped", i));
                    }
                } catch (Exception e) {
                    logger.warn(LogMessage.format("Cannon stop server #%s", i), e);
                }
            }
        }

    }

    private List<IMongodConfig> embeddedMongoConfigurations() throws IOException {
        List<Member> members = embeddedReplicaSetProperties.getMembers();
        List<IMongodConfig> configs = new ArrayList<>(members.size());

        IFeatureAwareVersion version = determineVersion();

        int configuredPort = ObjectUtils.defaultIfNull(this.properties.getPort(), 0);
        boolean configuredPortFree = (configuredPort > 0) && members.stream()
                .noneMatch(member -> member.getPort() == configuredPort);

        EmbeddedMongoProperties.Storage storage = Objects.requireNonNull(embeddedProperties.getStorage(),
                "spring.mongodb.embedded.storage must not be null");
        String replSetName = Objects.requireNonNull(storage.getReplSetName(),
                "spring.mongodb.embedded.storage.repl-set-name must not be null");
        DataSize defaultOplogSize = storage.getOplogSize();

        for (EmbeddedMongoReplicaSetProperties.Member member : members) {
            MongodConfigBuilder builder = new MongodConfigBuilder().version(version);
            IMongoCmdOptions cmdOptions = new MongoCmdOptionsBuilder().useNoJournal(false).build();
            builder.cmdOptions(cmdOptions);

            DataSize oplogSize = ObjectUtils.firstNonNull(member.getOplogSize(), defaultOplogSize);
            int oplogSizeMB = (oplogSize != null) ? (int) oplogSize.toMegabytes() : 0;
            builder.replication(new Storage(member.getDatabaseDir(), replSetName, oplogSizeMB));

            String host = ObjectUtils.firstNonNull(member.getHost(), properties.getHost());
            InetAddress hostAddress = (host == null) ? InetAddress.getLoopbackAddress() : InetAddress.getByName(host);
            int port = member.getPort();
            if (port == 0) {
                if (configuredPortFree) {
                    port = configuredPort;
                    configuredPortFree = false;
                } else {
                    port = Network.getFreeServerPort(hostAddress);
                }
            }
            builder.net(new Net(hostAddress.getHostAddress(), port, hostAddress instanceof Inet6Address));

            configs.add(builder.build());
        }

        return configs;
    }

    private IFeatureAwareVersion determineVersion() {
        if (embeddedProperties.getFeatures() == null) {
            for (Version version : Version.values()) {
                if (version.asInDownloadPath().equals(embeddedProperties.getVersion())) {
                    return version;
                }
            }
            return Versions.withFeatures(new GenericVersion(embeddedProperties.getVersion()));
        }
        return Versions.withFeatures(new GenericVersion(embeddedProperties.getVersion()),
                embeddedProperties.getFeatures().toArray(new Feature[0]));
    }

    private void setEmbeddedPort(ApplicationContext context, int port) {
        setPortProperty(context, port);
    }

    private void setPortProperty(ApplicationContext currentContext, int port) {
        if (currentContext instanceof ConfigurableApplicationContext) {
            MutablePropertySources sources = ((ConfigurableApplicationContext) currentContext).getEnvironment()
                    .getPropertySources();
            getMongoPorts(sources).put("local.mongo.port", port);
        }
        if (currentContext.getParent() != null) {
            setPortProperty(currentContext.getParent(), port);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMongoPorts(MutablePropertySources sources) {
        PropertySource<?> propertySource = sources.get("mongo.ports");
        if (propertySource == null) {
            propertySource = new MapPropertySource("mongo.ports", new HashMap<>());
            sources.addFirst(propertySource);
        }
        return (Map<String, Object>) propertySource.getSource();
    }

    private static void initiateReplicaSet(List<IMongodConfig> mongoConfigList)
            throws Exception {
        IMongodConfig mainConfig = mongoConfigList.get(0);
        ServerAddress serverAddress = new ServerAddress(mainConfig.net().getServerAddress().getHostName(),
                mainConfig.net().getPort());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(cluster -> cluster.hosts(Collections.singletonList(serverAddress)))
                .applyToClusterSettings(cluster -> cluster.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase adminDB = mongoClient.getDatabase("admin");

            // Build BSON object replica set settings
            DBObject replicaSetSetting = new BasicDBObject();
            replicaSetSetting.put("_id", mainConfig.replication().getReplSetName());
            BasicDBList members = new BasicDBList();
            int count = 0;
            for (IMongodConfig mongoConfig : mongoConfigList) {
                DBObject host = new BasicDBObject();
                host.put("_id", count++);
                try {
                    host.put("host", mongoConfig.net().getServerAddress().getHostName()
                            + ":" + mongoConfig.net().getPort());
                } catch (UnknownHostException e) {
                    throw new IOException("Cannot init replica set settings", e);
                }
                members.add(host);
            }
            replicaSetSetting.put("members", members);

            Document commandResult = adminDB.runCommand(new BasicDBObject("replSetInitiate", replicaSetSetting));
            if (logger.isDebugEnabled()) {
                logger.debug(LogMessage.format("replSetInitiate: %s", commandResult));
            }

            boolean replicaSetStarted = false;
            for (int tryCount = 0; !replicaSetStarted && tryCount < 20; tryCount++) {
                logger.debug("Waiting for 1 second...");
                Thread.sleep(1000);

                commandResult = adminDB.runCommand(new BasicDBObject("replSetGetStatus", 1));
                if (logger.isDebugEnabled()) {
                    logger.debug(LogMessage.format("replSetGetStatus: %s", commandResult));
                }
                replicaSetStarted = isReplicaSetStarted(commandResult) && hasPrimaryMember(commandResult);
            }
            if (!replicaSetStarted) {
                throw new IOException("Cannot start replica set");
            }

            // TODO wait for completion of replication to prevent error 10107 (NotMaster)
            boolean masterReady = false;
            for (int tryCount = 0; !masterReady && tryCount < 10; tryCount++) {
                logger.debug("Waiting for 1 second...");
                Thread.sleep(1000);

                commandResult = adminDB.runCommand(new BasicDBObject("isMaster", 1));
                if (logger.isDebugEnabled()) {
                    logger.debug(LogMessage.format("isMaster: %s", commandResult));
                }
                masterReady = isMaster(commandResult);
            }
            if (!masterReady) {
                throw new IOException("Master isn't ready");
            }
        }
    }

    private static boolean isReplicaSetStarted(Document setting) {
        // 1 - PRIMARY, 2 - SECONDARY, 7 - ARBITER
        List<Integer> allowedStates = Arrays.asList(1, 2, 7);

        List<Document> members = setting.getList("members", Document.class);
        return (members != null)
                && members.stream().allMatch(member -> allowedStates.contains(member.getInteger("state")));
    }

    private static boolean hasPrimaryMember(Document setting) {
        List<Document> members = setting.getList("members", Document.class);
        return (members != null) && members.stream().anyMatch(member -> member.getInteger("state") == 1);
    }

    private static boolean isMaster(Document isMaster) {
        return Boolean.TRUE.equals(isMaster.getBoolean("ismaster"));
    }

    /**
     * Post processor to ensure that {@link com.mongodb.client.MongoClient} beans
     * depend on any {@link MongoServersBean} beans.
     */
    @ConditionalOnClass({ com.mongodb.client.MongoClient.class, MongoClientFactoryBean.class })
    static class EmbeddedMongoClientDependsOnBeanFactoryPostProcessor
            extends MongoClientDependsOnBeanFactoryPostProcessor {

        EmbeddedMongoClientDependsOnBeanFactoryPostProcessor() {
            super(MongoServersBean.class);
        }

    }

}
