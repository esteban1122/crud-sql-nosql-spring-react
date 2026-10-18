package com.example.productoscrud.infrastructure.web.controllers;

import com.example.productoscrud.application.model.FilteredProducts;
import com.example.productoscrud.application.service.MongoProductQueryService;
import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API CRUD y filtros aggregate conectada exclusivamente a MongoDB. */
@RestController
@RequestMapping("/api/mongo/productos")
@CrossOrigin(origins = "*")
public class MongoProductController {
    private final ProductService productService;
    private final MongoProductQueryService queryService;

    public MongoProductController(
        @Qualifier("mongoProductService") ProductService productService,
        MongoProductQueryService queryService
    ) {
        this.productService = productService;
        this.queryService = queryService;
    }

    @GetMapping({"", "/"})
    public List<Product> listar() {
        return productService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> buscarPorId(@PathVariable int id) {
        return productService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> crear(@RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.crear(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripción inválidos");
        }
        return productService.actualizar(id, product)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        return productService.eliminar(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    @GetMapping("/filtros/nombre")
    public ResponseEntity<?> filtrarPorNombre(@RequestParam String texto) {
        if (texto.isBlank()) {
            return campoInvalido("texto");
        }
        return ResponseEntity.ok(queryService.filtrarPorNombre(texto));
    }

    @GetMapping("/filtros/descripcion")
    public ResponseEntity<?> filtrarPorDescripcion(@RequestParam String texto) {
        if (texto.isBlank()) {
            return campoInvalido("texto");
        }
        return ResponseEntity.ok(queryService.filtrarPorDescripcion(texto));
    }

    @GetMapping("/filtros/precio-minimo")
    public ResponseEntity<?> filtrarPorPrecioMinimo(@RequestParam BigDecimal minimo) {
        if (minimo.signum() < 0) {
            return campoInvalido("minimo");
        }
        return ResponseEntity.ok(queryService.filtrarPorPrecioMinimo(minimo));
    }

    @GetMapping("/filtros/rango-precio")
    public ResponseEntity<?> filtrarPorRangoDePrecio(
        @RequestParam BigDecimal minimo,
        @RequestParam BigDecimal maximo
    ) {
        if (minimo.signum() < 0 || maximo.signum() < 0 || minimo.compareTo(maximo) > 0) {
            return ResponseEntity.badRequest()
                .body("El rango de precios debe ser válido: mínimo menor o igual al máximo");
        }
        return ResponseEntity.ok(queryService.filtrarPorRangoDePrecio(minimo, maximo));
    }

    private ResponseEntity<String> campoInvalido(String campo) {
        return ResponseEntity.badRequest().body("El campo '" + campo + "' es obligatorio y debe ser válido");
    }
}
