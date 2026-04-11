package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Categoria;
import java.util.List;

public interface CategoriaService {
    List<Categoria> findAll();
    Categoria findById(Long id);
    Categoria create(Categoria categoria);
    Categoria update(Long id, Categoria categoria);
    void delete(Long id);
}

