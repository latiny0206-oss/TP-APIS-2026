package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Descuento;
import java.math.BigDecimal;
import java.util.List;

public interface DescuentoService {
    List<Descuento> findAll();
    Descuento findById(Long id);
    Descuento create(Descuento descuento);
    Descuento update(Long id, Descuento descuento);
    void delete(Long id);
    BigDecimal calcularDescuento(Long id, BigDecimal monto);
    boolean estaVigente(Long id);
}

