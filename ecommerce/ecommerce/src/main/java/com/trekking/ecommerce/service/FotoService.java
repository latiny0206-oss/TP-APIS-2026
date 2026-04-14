package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.FotoRequest;
import com.trekking.ecommerce.model.Foto;
import java.util.List;

public interface FotoService {
    List<Foto> findAll();
    List<Foto> findByProducto(Long productoId);
    Foto findById(Long id);
    Foto create(FotoRequest request);
    Foto update(Long id, FotoRequest request);
    void delete(Long id);
}
