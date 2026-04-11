package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.service.ItemOrdenService;
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
@RequestMapping("/api/items-orden")
@RequiredArgsConstructor
public class ItemOrdenController {

    private final ItemOrdenService itemOrdenService;

    @GetMapping
    public ResponseEntity<List<ItemOrden>> findAll() {
        return ResponseEntity.ok(itemOrdenService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemOrden> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemOrdenService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ItemOrden> create(@Valid @RequestBody ItemOrden itemOrden) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemOrdenService.create(itemOrden));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemOrden> update(@PathVariable Long id, @Valid @RequestBody ItemOrden itemOrden) {
        return ResponseEntity.ok(itemOrdenService.update(id, itemOrden));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemOrdenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

