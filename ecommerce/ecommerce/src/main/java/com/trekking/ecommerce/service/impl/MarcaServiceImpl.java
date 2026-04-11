package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.repository.MarcaRepository;
import com.trekking.ecommerce.service.MarcaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    @Override
    public List<Marca> findAll() {
        return marcaRepository.findAll();
    }

    @Override
    public Marca findById(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
    }

    @Override
    public Marca create(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Override
    public Marca update(Long id, Marca marca) {
        Marca actual = findById(id);
        actual.setNombre(marca.getNombre());
        actual.setDescripcion(marca.getDescripcion());
        return marcaRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        marcaRepository.deleteById(id);
    }
}

