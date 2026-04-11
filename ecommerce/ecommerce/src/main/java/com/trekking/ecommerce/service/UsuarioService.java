package com.trekking.ecommerce.service;

import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> findAll();
    UsuarioResponse findById(Long id);
    UsuarioResponse create(UsuarioRequest request);
    UsuarioResponse update(Long id, UsuarioRequest request);
    void delete(Long id);
}

