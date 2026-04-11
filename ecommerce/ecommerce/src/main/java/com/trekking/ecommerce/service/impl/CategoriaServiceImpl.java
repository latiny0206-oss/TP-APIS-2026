package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Categoria;
import com.trekking.ecommerce.repository.CategoriaRepository;
import com.trekking.ecommerce.service.CategoriaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + id));
    }

    @Override
    public Categoria create(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria update(Long id, Categoria categoria) {
        Categoria actual = findById(id);
        actual.setNombre(categoria.getNombre());
        actual.setDescripcion(categoria.getDescripcion());
        return categoriaRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        categoriaRepository.deleteById(id);
    }
}

