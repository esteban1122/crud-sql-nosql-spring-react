package com.example.productoscrud.infrastructure.persistence.mongo;

import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Documento de infraestructura: evita que el modelo de dominio dependa de MongoDB. */
@Document(collection = "products")
public class MongoProductDocument {
    @Id
    private Integer id;
    private String name;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;
    private String description;

    public MongoProductDocument() {
    }

    private MongoProductDocument(Integer id, String name, BigDecimal price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public static MongoProductDocument from(Product product) {
        return new MongoProductDocument(
            product.getId(), product.getName(), product.getPrice(), product.getDescription()
        );
    }

    public Product toDomain() {
        return new Product(id, name, price, description);
    }
}
