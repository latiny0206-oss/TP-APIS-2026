package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.service.VarianteProductoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/variantes")
@RequiredArgsConstructor
public class VarianteProductoController {

    private final VarianteProductoService varianteProductoService;

    @GetMapping
    public ResponseEntity<List<VarianteProducto>> findAll() {
        return ResponseEntity.ok(varianteProductoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteProducto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(varianteProductoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<VarianteProducto> create(@Valid @RequestBody VarianteProducto variante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varianteProductoService.create(variante));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VarianteProducto> update(@PathVariable Long id, @Valid @RequestBody VarianteProducto variante) {
        return ResponseEntity.ok(varianteProductoService.update(id, variante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        varianteProductoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/precio")
    public ResponseEntity<BigDecimal> getPrecio(@PathVariable Long id) {
        return ResponseEntity.ok(varianteProductoService.getPrecio(id));
    }

    @GetMapping("/{id}/stock/disponible")
    public ResponseEntity<Boolean> tieneStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return ResponseEntity.ok(varianteProductoService.tieneStock(id, cantidad));
    }

    @PostMapping("/{id}/stock/descontar")
    public ResponseEntity<VarianteProducto> descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        return ResponseEntity.ok(varianteProductoService.descontarStock(id, cantidad));
    }
}

