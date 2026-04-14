package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.FotoRequest;
import com.trekking.ecommerce.dto.FotoResponse;
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
    public ResponseEntity<List<FotoResponse>> findAll() {
        return ResponseEntity.ok(fotoService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FotoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(fotoService.findById(id)));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<FotoResponse>> findByProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(fotoService.findByProducto(productoId).stream()
                .map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<FotoResponse> create(@Valid @RequestBody FotoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(fotoService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FotoResponse> update(@PathVariable Long id,
            @Valid @RequestBody FotoRequest request) {
        return ResponseEntity.ok(toResponse(fotoService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fotoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private FotoResponse toResponse(Foto f) {
        return FotoResponse.builder()
                .id(f.getId())
                .productoId(f.getProducto().getId())
                .nombre(f.getNombre())
                .orden(f.getOrden())
                .build();
    }
}
