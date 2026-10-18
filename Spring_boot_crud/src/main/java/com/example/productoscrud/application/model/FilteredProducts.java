package com.example.productoscrud.application.model;

import com.example.productoscrud.domain.model.Product;
import java.util.List;
import java.util.Map;

/** Resultado de un filtro Mongo y el pipeline que lo produjo. */
public record FilteredProducts(
    List<Product> productos,
    List<Map<String, Object>> pipeline
) {
}
