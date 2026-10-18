package com.example.productoscrud.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "counters")
public class MongoSequence {
    @Id
    private String id;
    private int sequence;

    public int getSequence() {
        return sequence;
    }
}
