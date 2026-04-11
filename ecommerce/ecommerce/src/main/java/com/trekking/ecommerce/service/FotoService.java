package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Foto;
import java.util.List;

public interface FotoService {
    List<Foto> findAll();
    Foto findById(Long id);
    Foto create(Foto foto);
    Foto update(Long id, Foto foto);
    void delete(Long id);
}

