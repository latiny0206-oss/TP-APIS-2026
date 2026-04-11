package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.service.FotoService;
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
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final FotoService fotoService;

    @GetMapping
    public ResponseEntity<List<Foto>> findAll() {
        return ResponseEntity.ok(fotoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Foto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fotoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Foto> create(@Valid @RequestBody Foto foto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fotoService.create(foto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Foto> update(@PathVariable Long id, @Valid @RequestBody Foto foto) {
        return ResponseEntity.ok(fotoService.update(id, foto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

