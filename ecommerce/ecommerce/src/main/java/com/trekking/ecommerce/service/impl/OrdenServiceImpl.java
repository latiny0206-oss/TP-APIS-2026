package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.OrdenService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final DescuentoRepository descuentoRepository;

    @Override
    public List<Orden> findAll() {
        return ordenRepository.findAll();
    }

    @Override
    public Orden findById(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + id));
    }

    @Override
    public Orden create(Orden orden) {
        orden.setUsuario(usuarioRepository.findById(orden.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + orden.getUsuario().getId())));
        if (orden.getCarrito() != null) {
            orden.setCarrito(carritoRepository.findById(orden.getCarrito().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado: " + orden.getCarrito().getId())));
        }
        if (orden.getDescuento() != null) {
            orden.setDescuento(descuentoRepository.findById(orden.getDescuento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado: " + orden.getDescuento().getId())));
        }
        return ordenRepository.save(orden);
    }

    @Override
    public Orden update(Long id, Orden orden) {
        Orden actual = findById(id);
        actual.setUsuario(usuarioRepository.findById(orden.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + orden.getUsuario().getId())));
        if (orden.getCarrito() != null) {
            actual.setCarrito(carritoRepository.findById(orden.getCarrito().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado: " + orden.getCarrito().getId())));
        } else {
            actual.setCarrito(null);
        }
        if (orden.getDescuento() != null) {
            actual.setDescuento(descuentoRepository.findById(orden.getDescuento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado: " + orden.getDescuento().getId())));
        } else {
            actual.setDescuento(null);
        }
        actual.setFechaCreacion(orden.getFechaCreacion());
        actual.setMontoFinal(orden.getMontoFinal());
        actual.setEstado(orden.getEstado());
        return ordenRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        ordenRepository.deleteById(id);
    }

    @Override
    public Orden confirmar(Long id) {
        Orden orden = findById(id);
        orden.setEstado(EstadoOrden.CONFIRMADA);
        return ordenRepository.save(orden);
    }

    @Override
    public Orden cancelar(Long id) {
        Orden orden = findById(id);
        orden.setEstado(EstadoOrden.CANCELADA);
        return ordenRepository.save(orden);
    }

    @Override
    public BigDecimal getMontoFinal(Long id) {
        return findById(id).getMontoFinal();
    }

    @Override
    public List<ItemOrden> obtenerItems(Long id) {
        return itemOrdenRepository.findByOrdenId(id);
    }

    @Override
    public List<Orden> findByUsuario(Long idUsuario) {
        return ordenRepository.findByUsuarioId(idUsuario);
    }
}

