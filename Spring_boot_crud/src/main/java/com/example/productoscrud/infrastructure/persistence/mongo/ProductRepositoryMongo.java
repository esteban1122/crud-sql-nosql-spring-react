package com.example.productoscrud.infrastructure.persistence.mongo;

import com.example.productoscrud.application.model.FilteredProducts;
import com.example.productoscrud.application.ports.MongoProductQueryPort;
import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptador NoSQL que implementa el mismo CRUD abstracto que la persistencia MySQL. */
@Repository("mongoProductRepository")
public class ProductRepositoryMongo extends ProductRepository implements MongoProductQueryPort {
    private static final String COUNTER_ID = "products_sequence";

    private final MongoTemplate mongoTemplate;

    public ProductRepositoryMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Product> listar() {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "id"));
        return mongoTemplate.find(query, MongoProductDocument.class)
            .stream().map(MongoProductDocument::toDomain).toList();
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        MongoProductDocument document = mongoTemplate.findById(id, MongoProductDocument.class);
        return Optional.ofNullable(document).map(MongoProductDocument::toDomain);
    }

    @Override
    public Product crear(Product product) {
        Product productConId = new Product(
            siguienteId(), product.getName(), product.getPrice(), product.getDescription()
        );
        mongoTemplate.insert(MongoProductDocument.from(productConId));
        return productConId;
    }

    @Override
    public boolean actualizar(int id, Product product) {
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update()
            .set("name", product.getName())
            .set("price", product.getPrice())
            .set("description", product.getDescription());
        return mongoTemplate.updateFirst(query, update, MongoProductDocument.class).getModifiedCount() > 0;
    }

    @Override
    public boolean eliminar(int id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.remove(query, MongoProductDocument.class).getDeletedCount() > 0;
    }

    @Override
    public FilteredProducts filtrarPorNombre(String texto) {
        String patron = Pattern.quote(texto.trim());
        List<Map<String, Object>> pipeline = List.of(
            etapa("$match", Map.of("name", Map.of("$regex", patron, "$options", "i"))),
            etapa("$sort", Map.of("_id", 1))
        );
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("name").regex(patron, "i")),
            Aggregation.sort(Sort.Direction.ASC, "_id")
        );
        return ejecutar(aggregation, pipeline);
    }

    @Override
    public FilteredProducts filtrarPorDescripcion(String texto) {
        String patron = Pattern.quote(texto.trim());
        List<Map<String, Object>> pipeline = List.of(
            etapa("$match", Map.of("description", Map.of("$regex", patron, "$options", "i"))),
            etapa("$sort", Map.of("_id", 1))
        );
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("description").regex(patron, "i")),
            Aggregation.sort(Sort.Direction.ASC, "_id")
        );
        return ejecutar(aggregation, pipeline);
    }

    @Override
    public FilteredProducts filtrarPorPrecioMinimo(BigDecimal minimo) {
        List<Map<String, Object>> pipeline = List.of(
            etapa("$match", Map.of("price", Map.of("$gte", minimo))),
            etapa("$sort", Map.of("price", 1, "_id", 1))
        );
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("price").gte(minimo)),
            Aggregation.sort(Sort.by(Sort.Order.asc("price"), Sort.Order.asc("_id")))
        );
        return ejecutar(aggregation, pipeline);
    }

    @Override
    public FilteredProducts filtrarPorRangoDePrecio(BigDecimal minimo, BigDecimal maximo) {
        List<Map<String, Object>> pipeline = List.of(
            etapa("$match", Map.of("price", Map.of("$gte", minimo, "$lte", maximo))),
            etapa("$sort", Map.of("price", 1, "_id", 1))
        );
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("price").gte(minimo).lte(maximo)),
            Aggregation.sort(Sort.by(Sort.Order.asc("price"), Sort.Order.asc("_id")))
        );
        return ejecutar(aggregation, pipeline);
    }

    private FilteredProducts ejecutar(
        Aggregation aggregation,
        List<Map<String, Object>> pipeline
    ) {
        List<Product> productos = mongoTemplate.aggregate(
            aggregation, MongoProductDocument.class, MongoProductDocument.class
        ).getMappedResults().stream().map(MongoProductDocument::toDomain).toList();
        return new FilteredProducts(productos, pipeline);
    }

    private int siguienteId() {
        MongoSequence sequence = mongoTemplate.findAndModify(
            Query.query(Criteria.where("_id").is(COUNTER_ID)),
            new Update().inc("sequence", 1),
            FindAndModifyOptions.options().upsert(true).returnNew(true),
            MongoSequence.class
        );
        if (sequence == null) {
            throw new IllegalStateException("No fue posible generar el identificador de MongoDB");
        }
        return sequence.getSequence();
    }

    private Map<String, Object> etapa(String operador, Object contenido) {
        Map<String, Object> etapa = new LinkedHashMap<>();
        etapa.put(operador, contenido);
        return etapa;
    }
}
