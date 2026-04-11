package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.CarritoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final DescuentoRepository descuentoRepository;
    private final OrdenRepository ordenRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado: " + id));
    }

    @Override
    public Carrito create(Carrito carrito) {
        carrito.setUsuario(usuarioRepository.findById(carrito.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + carrito.getUsuario().getId())));
        if (carrito.getDescuento() != null) {
            carrito.setDescuento(descuentoRepository.findById(carrito.getDescuento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado: " + carrito.getDescuento().getId())));
        }
        return carritoRepository.save(carrito);
    }

    @Override
    public Carrito update(Long id, Carrito carrito) {
        Carrito actual = findById(id);
        actual.setUsuario(usuarioRepository.findById(carrito.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + carrito.getUsuario().getId())));
        if (carrito.getDescuento() != null) {
            actual.setDescuento(descuentoRepository.findById(carrito.getDescuento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado: " + carrito.getDescuento().getId())));
        } else {
            actual.setDescuento(null);
        }
        actual.setEstado(carrito.getEstado());
        actual.setMontoTotal(carrito.getMontoTotal());
        return carritoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public ItemCarrito agregarItem(Long idCarrito, Long idVariante, Integer cantidad) {
        Carrito carrito = findById(idCarrito);
        VarianteProducto variante = varianteProductoRepository.findById(idVariante)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + idVariante));

        ItemCarrito item = itemCarritoRepository.findByCarritoIdAndVarianteId(idCarrito, idVariante)
                .orElse(ItemCarrito.builder()
                        .carrito(carrito)
                        .variante(variante)
                        .cantidad(0)
                        .precioUnitario(variante.getPrecio())
                        .build());

        item.setCantidad(item.getCantidad() + cantidad);
        item.setPrecioUnitario(variante.getPrecio());
        ItemCarrito saved = itemCarritoRepository.save(item);

        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
        return saved;
    }

    @Override
    public void eliminarItem(Long idCarrito, Long idItemCarrito) {
        findById(idCarrito);
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new IllegalArgumentException("Item de carrito no encontrado: " + idItemCarrito));
        if (!item.getCarrito().getId().equals(idCarrito)) {
            throw new IllegalArgumentException("El item no pertenece al carrito indicado");
        }
        itemCarritoRepository.delete(item);
        Carrito carrito = findById(idCarrito);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
    }

    @Override
    public ItemCarrito actualizarItem(Long idCarrito, Long idItemCarrito, Integer cantidad) {
        findById(idCarrito);
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new IllegalArgumentException("Item de carrito no encontrado: " + idItemCarrito));
        if (!item.getCarrito().getId().equals(idCarrito)) {
            throw new IllegalArgumentException("El item no pertenece al carrito indicado");
        }
        item.setCantidad(cantidad);
        ItemCarrito saved = itemCarritoRepository.save(item);
        Carrito carrito = findById(idCarrito);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
        return saved;
    }

    @Override
    public BigDecimal calcularTotal(Long idCarrito) {
        BigDecimal subtotal = obtenerItems(idCarrito).stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Carrito carrito = findById(idCarrito);
        if (carrito.getDescuento() == null) {
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        Descuento descuento = carrito.getDescuento();
        if (descuento == null || !estaVigente(descuento)) {
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal descuentoAplicado;
        if (descuento.getTipo() == TipoDescuento.FIJO) {
            descuentoAplicado = descuento.getValor().min(subtotal);
        } else {
            BigDecimal porcentaje = descuento.getPorcentaje() != null
                    ? BigDecimal.valueOf(descuento.getPorcentaje())
                    : descuento.getValor();
            descuentoAplicado = subtotal.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return subtotal.subtract(descuentoAplicado).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void vaciarCarrito(Long idCarrito) {
        Carrito carrito = findById(idCarrito);
        List<ItemCarrito> items = obtenerItems(idCarrito);
        itemCarritoRepository.deleteAll(items);
        carrito.setMontoTotal(BigDecimal.ZERO);
        carrito.setEstado(EstadoCarrito.VACIO);
        carritoRepository.save(carrito);
    }

    @Override
    public List<ItemCarrito> obtenerItems(Long idCarrito) {
        findById(idCarrito);
        return itemCarritoRepository.findByCarritoId(idCarrito);
    }

    @Override
    public Orden realizarCompra(Long idCarrito) {
        Carrito carrito = findById(idCarrito);
        List<ItemCarrito> items = obtenerItems(idCarrito);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No se puede generar una orden con carrito vacio");
        }

        BigDecimal total = calcularTotal(idCarrito);
        Orden orden = Orden.builder()
                .usuario(carrito.getUsuario())
                .carrito(carrito)
                .descuento(carrito.getDescuento())
                .fechaCreacion(LocalDateTime.now())
                .montoFinal(total)
                .estado(EstadoOrden.PENDIENTE)
                .build();
        Orden ordenGuardada = ordenRepository.save(orden);

        List<ItemOrden> itemsOrden = items.stream()
                .map(item -> ItemOrden.builder()
                        .orden(ordenGuardada)
                        .variante(item.getVariante())
                        .cantidad(item.getCantidad())
                        .precioAlMomento(item.getPrecioUnitario())
                        .build())
                .toList();
        itemOrdenRepository.saveAll(itemsOrden);

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carrito.setMontoTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
        itemCarritoRepository.deleteAll(items);

        return ordenGuardada;
    }

    private boolean estaVigente(Descuento descuento) {
        LocalDate hoy = LocalDate.now();
        return descuento.getEstado().name().equals("ACTIVO")
                && !hoy.isBefore(descuento.getFechaInicio())
                && !hoy.isAfter(descuento.getFechaFin());
    }
}

