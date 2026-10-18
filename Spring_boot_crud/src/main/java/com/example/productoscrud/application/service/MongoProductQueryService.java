package com.example.productoscrud.application.service;

import com.example.productoscrud.application.model.FilteredProducts;
import com.example.productoscrud.application.ports.MongoProductQueryPort;
import java.math.BigDecimal;

/** Caso de uso de las cuatro consultas agregadas exclusivas de MongoDB. */
public class MongoProductQueryService {
    private final MongoProductQueryPort queryPort;

    public MongoProductQueryService(MongoProductQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    public FilteredProducts filtrarPorNombre(String texto) {
        return queryPort.filtrarPorNombre(texto);
    }

    public FilteredProducts filtrarPorDescripcion(String texto) {
        return queryPort.filtrarPorDescripcion(texto);
    }

    public FilteredProducts filtrarPorPrecioMinimo(BigDecimal minimo) {
        return queryPort.filtrarPorPrecioMinimo(minimo);
    }

    public FilteredProducts filtrarPorRangoDePrecio(BigDecimal minimo, BigDecimal maximo) {
        return queryPort.filtrarPorRangoDePrecio(minimo, maximo);
    }
}
