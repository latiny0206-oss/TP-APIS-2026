package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.FotoRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.repository.FotoRepository;
import com.trekking.ecommerce.service.FotoService;
import com.trekking.ecommerce.service.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FotoServiceImpl implements FotoService {

    private final FotoRepository fotoRepository;
    private final ProductoService productoService;

    @Override
    public List<Foto> findAll() {
        return fotoRepository.findAll();
    }

    @Override
    public List<Foto> findByProducto(Long productoId) {
        productoService.findById(productoId);
        return fotoRepository.findByProductoId(productoId);
    }

    @Override
    public Foto findById(Long id) {
        return fotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto", id));
    }

    @Override
    @Transactional
    public Foto create(FotoRequest request) {
        Foto foto = Foto.builder()
                .producto(productoService.findById(request.getProductoId()))
                .nombre(request.getNombre())
                .orden(request.getOrden())
                .build();
        return fotoRepository.save(foto);
    }

    @Override
    @Transactional
    public Foto update(Long id, FotoRequest request) {
        Foto actual = findById(id);
        actual.setProducto(productoService.findById(request.getProductoId()));
        actual.setNombre(request.getNombre());
        actual.setOrden(request.getOrden());
        return fotoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        fotoRepository.deleteById(id);
    }
}
