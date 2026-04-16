package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.ItemOrdenResponse;
import com.trekking.ecommerce.dto.OrdenResponse;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.service.OrdenService;
import com.trekking.ecommerce.service.UsuarioService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> findAll() {
        List<Orden> ordenes = esAdmin()
                ? ordenService.findAll()
                : ordenService.findByUsuario(getUsuarioAutenticado().getId());
        return ResponseEntity.ok(ordenes.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> findById(@PathVariable Long id) {
        Orden orden = ordenService.findById(id);
        validarPropietario(orden.getUsuario().getId());
        return ResponseEntity.ok(toResponse(orden));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenResponse> confirmar(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toResponse(ordenService.confirmar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<OrdenResponse> cancelar(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(toResponse(ordenService.cancelar(id)));
    }

    @GetMapping("/{id}/monto-final")
    public ResponseEntity<BigDecimal> getMontoFinal(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(ordenService.getMontoFinal(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemOrdenResponse>> obtenerItems(@PathVariable Long id) {
        validarPropietario(ordenService.findById(id).getUsuario().getId());
        return ResponseEntity.ok(ordenService.obtenerItems(id).stream()
                .map(this::toItemResponse).toList());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<OrdenResponse>> historialPorUsuario(@PathVariable Long idUsuario) {
        validarPropietario(idUsuario);
        return ResponseEntity.ok(ordenService.findByUsuario(idUsuario).stream()
                .map(this::toResponse).toList());
    }

    // ─── Helpers de seguridad ────────────────────────────────────────────────

    private boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.findByUsername(auth.getName());
    }

    private void validarPropietario(Long propietarioId) {
        if (esAdmin()) return;
        if (!getUsuarioAutenticado().getId().equals(propietarioId)) {
            throw new AccessDeniedException("No tenés permiso para acceder a este recurso");
        }
    }

    // ─── Mapeo a DTOs ────────────────────────────────────────────────────────

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
