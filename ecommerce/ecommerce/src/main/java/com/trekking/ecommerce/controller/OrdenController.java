package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.ItemOrdenResponse;
import com.trekking.ecommerce.dto.OrdenResponse;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.service.OrdenService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> findAll() {
        return ResponseEntity.ok(ordenService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(ordenService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(ordenService.confirmar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<OrdenResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(ordenService.cancelar(id)));
    }

    @GetMapping("/{id}/monto-final")
    public ResponseEntity<BigDecimal> getMontoFinal(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.getMontoFinal(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemOrdenResponse>> obtenerItems(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerItems(id).stream()
                .map(this::toItemResponse).toList());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<OrdenResponse>> historialPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(ordenService.findByUsuario(idUsuario).stream()
                .map(this::toResponse).toList());
    }

    private OrdenResponse toResponse(Orden o) {
        List<ItemOrdenResponse> items = ordenService.obtenerItems(o.getId()).stream()
                .map(this::toItemResponse).toList();
        return OrdenResponse.builder()
                .id(o.getId())
                .usuarioId(o.getUsuario().getId())
                .carritoId(o.getCarrito() != null ? o.getCarrito().getId() : null)
                .descuentoId(o.getDescuento() != null ? o.getDescuento().getId() : null)
                .fechaCreacion(o.getFechaCreacion())
                .montoFinal(o.getMontoFinal())
                .estado(o.getEstado())
                .items(items)
                .build();
    }

    private ItemOrdenResponse toItemResponse(ItemOrden item) {
        return ItemOrdenResponse.builder()
                .id(item.getId())
                .varianteId(item.getVariante().getId())
                .varianteColor(item.getVariante().getColor())
                .varianteTalla(item.getVariante().getTalla())
                .productoNombre(item.getVariante().getProducto().getNombre())
                .cantidad(item.getCantidad())
                .precioAlMomento(item.getPrecioAlMomento())
                .build();
    }
}
