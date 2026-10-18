package com.example.productoscrud.infrastructure.web.controllers;

import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.domain.model.Product;
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
import org.springframework.web.bind.annotation.RestController;

/** API CRUD conectada al adaptador MySQL. */
@RestController
@RequestMapping({"/api/sql/productos", "/productos"})
@CrossOrigin(origins = "*")
public class SqlProductController {
    private final ProductService productService;

    public SqlProductController(@Qualifier("sqlProductService") ProductService productService) {
        this.productService = productService;
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
}
