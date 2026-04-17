package com.trekking.ecommerce.service;

import com.trekking.ecommerce.model.Foto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FotoService {
    List<Foto> findAll();
    List<Foto> findByProducto(Long productoId);
    Foto findById(Long id);
    Foto create(Long productoId, Integer orden, MultipartFile archivo);
    Foto update(Long id, Long productoId, Integer orden, MultipartFile archivo);
    void delete(Long id);
}
