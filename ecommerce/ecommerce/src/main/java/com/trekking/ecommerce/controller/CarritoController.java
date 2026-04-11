package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.service.CarritoService;
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
@RequestMapping("/api/carritos")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<List<Carrito>> findAll() {
        return ResponseEntity.ok(carritoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Carrito> create(@Valid @RequestBody Carrito carrito) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.create(carrito));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carrito> update(@PathVariable Long id, @Valid @RequestBody Carrito carrito) {
        return ResponseEntity.ok(carritoService.update(id, carrito));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carritoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItemCarrito> agregarItem(
            @PathVariable Long id,
            @RequestParam Long idVariante,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(carritoService.agregarItem(id, idVariante, cantidad));
    }

    @DeleteMapping("/{id}/items/{idItem}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id, @PathVariable Long idItem) {
        carritoService.eliminarItem(id, idItem);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/items/{idItem}")
    public ResponseEntity<ItemCarrito> actualizarItem(
            @PathVariable Long id,
            @PathVariable Long idItem,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(carritoService.actualizarItem(id, idItem, cantidad));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemCarrito>> obtenerItems(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.obtenerItems(id));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calcularTotal(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.calcularTotal(id));
    }

    @PostMapping("/{id}/vaciar")
    public ResponseEntity<Void> vaciar(@PathVariable Long id) {
        carritoService.vaciarCarrito(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<Orden> realizarCompra(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.realizarCompra(id));
    }
}

