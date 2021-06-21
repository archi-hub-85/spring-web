package ru.akh.spring.boot.autoconfigure.mongo.embedded;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.mongodb.embedded.replica-set")
@Validated
public class EmbeddedMongoReplicaSetProperties {

    @Min(1)
    private int size;

    @Valid
    private List<Member> members;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Member> getMembers() {
        if (members == null) {
            members = new ArrayList<>(size);
        }
        if (members.size() < size) {
            for (int i = members.size(); i < size; i++) {
                members.add(new Member(null, 0, null, null));
            }
        }
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @ConstructorBinding
    public static class Member {

        private final String host;

        private final int port;

        @DataSizeUnit(DataUnit.MEGABYTES)
        private final DataSize oplogSize;

        private final String databaseDir;

        public Member(String host, int port, DataSize oplogSize, String databaseDir) {
            this.host = host;
            this.port = port;
            this.oplogSize = oplogSize;
            this.databaseDir = databaseDir;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public DataSize getOplogSize() {
            return this.oplogSize;
        }

        public String getDatabaseDir() {
            return this.databaseDir;
        }

    }

}
