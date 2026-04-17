package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Foto;
import com.trekking.ecommerce.repository.FotoRepository;
import com.trekking.ecommerce.service.FotoService;
import com.trekking.ecommerce.service.ProductoService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FotoServiceImpl implements FotoService {

    private final FotoRepository fotoRepository;
    private final ProductoService productoService;

    @Override
    @Transactional(readOnly = true)
    public List<Foto> findAll() {
        return fotoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Foto> findByProducto(Long productoId) {
        productoService.findById(productoId);
        return fotoRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Foto findById(Long id) {
        return fotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto", id));
    }

    @Override
    @Transactional
    public Foto create(Long productoId, Integer orden, MultipartFile archivo) {
        Foto foto = Foto.builder()
                .producto(productoService.findById(productoId))
                .nombre(archivo.getOriginalFilename())
                .tipoContenido(archivo.getContentType())
                .orden(orden)
                .datos(leerBytes(archivo))
                .build();
        return fotoRepository.save(foto);
    }

    @Override
    @Transactional
    public Foto update(Long id, Long productoId, Integer orden, MultipartFile archivo) {
        Foto actual = findById(id);
        actual.setProducto(productoService.findById(productoId));
        actual.setOrden(orden);
        actual.setNombre(archivo.getOriginalFilename());
        actual.setTipoContenido(archivo.getContentType());
        actual.setDatos(leerBytes(archivo));
        return fotoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        fotoRepository.deleteById(id);
    }

    private byte[] leerBytes(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new BusinessRuleException("No se pudo leer el archivo: " + e.getMessage());
        }
    }
}
