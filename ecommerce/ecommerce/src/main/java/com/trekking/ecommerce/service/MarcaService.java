package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Marca;
import java.util.List;

public interface MarcaService {
    List<Marca> findAll();
    Marca findById(Long id);
    Marca create(Marca marca);
    Marca update(Long id, Marca marca);
    void delete(Long id);
}

