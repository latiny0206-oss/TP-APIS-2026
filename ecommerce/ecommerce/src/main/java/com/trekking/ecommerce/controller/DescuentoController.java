package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.service.DescuentoService;
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
@RequestMapping("/api/descuentos")
@RequiredArgsConstructor
public class DescuentoController {

    private final DescuentoService descuentoService;

    @GetMapping
    public ResponseEntity<List<Descuento>> findAll() {
        return ResponseEntity.ok(descuentoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Descuento> findById(@PathVariable Long id) {
        return ResponseEntity.ok(descuentoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Descuento> create(@Valid @RequestBody Descuento descuento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(descuentoService.create(descuento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Descuento> update(@PathVariable Long id, @Valid @RequestBody Descuento descuento) {
        return ResponseEntity.ok(descuentoService.update(id, descuento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        descuentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vigente")
    public ResponseEntity<Boolean> estaVigente(@PathVariable Long id) {
        return ResponseEntity.ok(descuentoService.estaVigente(id));
    }

    @GetMapping("/{id}/calcular")
    public ResponseEntity<BigDecimal> calcular(@PathVariable Long id, @RequestParam BigDecimal monto) {
        return ResponseEntity.ok(descuentoService.calcularDescuento(id, monto));
    }
}

