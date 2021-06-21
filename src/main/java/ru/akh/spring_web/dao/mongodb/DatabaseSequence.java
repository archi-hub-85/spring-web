package ru.akh.spring_web.dao.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Constants.CollectionNames.DATABASE_SEQUENCES)
public class DatabaseSequence {

    @Id
    private String id;

    private long seq;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "DatabaseSequence [id=" + id + ", seq=" + seq + "]";
    }

}
