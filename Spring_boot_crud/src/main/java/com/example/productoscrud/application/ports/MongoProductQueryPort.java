package com.example.productoscrud.application.ports;

import com.example.productoscrud.application.model.FilteredProducts;
import java.math.BigDecimal;

/**
 * Puerto para las consultas que MongoDB resuelve mediante aggregation pipelines.
 * No reemplaza el contrato CRUD ProductRepository: lo complementa solo para NoSQL.
 */
public interface MongoProductQueryPort {
    FilteredProducts filtrarPorNombre(String texto);
    FilteredProducts filtrarPorDescripcion(String texto);
    FilteredProducts filtrarPorPrecioMinimo(BigDecimal minimo);
    FilteredProducts filtrarPorRangoDePrecio(BigDecimal minimo, BigDecimal maximo);
}
