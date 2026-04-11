package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.service.ItemCarritoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/items-carrito")
@RequiredArgsConstructor
public class ItemCarritoController {

    private final ItemCarritoService itemCarritoService;

    @GetMapping
    public ResponseEntity<List<ItemCarrito>> findAll() {
        return ResponseEntity.ok(itemCarritoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCarrito> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemCarritoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ItemCarrito> create(@Valid @RequestBody ItemCarrito itemCarrito) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemCarritoService.create(itemCarrito));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> update(@PathVariable Long id, @Valid @RequestBody ItemCarrito itemCarrito) {
        return ResponseEntity.ok(itemCarritoService.update(id, itemCarrito));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemCarritoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

