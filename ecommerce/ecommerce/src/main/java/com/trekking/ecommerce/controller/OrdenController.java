package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.service.OrdenService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping
    public ResponseEntity<List<Orden>> findAll() {
        return ResponseEntity.ok(ordenService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orden> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Orden> create(@Valid @RequestBody Orden orden) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.create(orden));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orden> update(@PathVariable Long id, @Valid @RequestBody Orden orden) {
        return ResponseEntity.ok(ordenService.update(id, orden));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Orden> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.confirmar(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Orden> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.cancelar(id));
    }

    @GetMapping("/{id}/monto-final")
    public ResponseEntity<BigDecimal> getMontoFinal(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.getMontoFinal(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemOrden>> obtenerItems(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerItems(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Orden>> historialPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ordenService.findByUsuario(idUsuario));
    }
}

