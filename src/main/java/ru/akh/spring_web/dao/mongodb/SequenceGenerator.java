package ru.akh.spring_web.dao.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Profile("mongodb")
public class SequenceGenerator {

    @Autowired
    private MongoTemplate template;

    public long generateSequence(String seqName) {
        DatabaseSequence counter = template.findAndModify(Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1), FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class);
        return counter.getSeq();
    }

}
