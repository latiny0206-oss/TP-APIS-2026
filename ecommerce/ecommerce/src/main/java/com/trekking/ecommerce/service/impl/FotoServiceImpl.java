package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.repository.FotoRepository;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.service.FotoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FotoServiceImpl implements FotoService {

    private final FotoRepository fotoRepository;
    private final ProductoRepository productoRepository;

    @Override
    public List<Foto> findAll() {
        return fotoRepository.findAll();
    }

    @Override
    public Foto findById(Long id) {
        return fotoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foto no encontrada: " + id));
    }

    @Override
    public Foto create(Foto foto) {
        foto.setProducto(productoRepository.findById(foto.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + foto.getProducto().getId())));
        return fotoRepository.save(foto);
    }

    @Override
    public Foto update(Long id, Foto foto) {
        Foto actual = findById(id);
        actual.setProducto(productoRepository.findById(foto.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + foto.getProducto().getId())));
        actual.setNombre(foto.getNombre());
        actual.setOrden(foto.getOrden());
        return fotoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        fotoRepository.deleteById(id);
    }
}

