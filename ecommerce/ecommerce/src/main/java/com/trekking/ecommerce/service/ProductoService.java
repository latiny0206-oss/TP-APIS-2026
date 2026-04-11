package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Producto;
import java.util.List;

public interface ProductoService {
    List<Producto> findAll();
    Producto findById(Long id);
    Producto create(Producto producto);
    Producto update(Long id, Producto producto);
    void delete(Long id);
    boolean estaDisponible(Long id);
}

