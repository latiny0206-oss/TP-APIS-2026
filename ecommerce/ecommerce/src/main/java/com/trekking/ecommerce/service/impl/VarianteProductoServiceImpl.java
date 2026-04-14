package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.VarianteProductoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ProductoService;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VarianteProductoServiceImpl implements VarianteProductoService {

    private final VarianteProductoRepository varianteProductoRepository;
    private final ProductoService productoService;

    @Override
    public List<VarianteProducto> findAll() {
        return varianteProductoRepository.findAll();
    }

    @Override
    public VarianteProducto findById(Long id) {
        return varianteProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarianteProducto", id));
    }

    @Override
    @Transactional
    public VarianteProducto create(VarianteProductoRequest request) {
        VarianteProducto variante = VarianteProducto.builder()
                .producto(productoService.findById(request.getProductoId()))
                .color(request.getColor())
                .talla(request.getTalla())
                .material(request.getMaterial())
                .peso(request.getPeso())
                .stock(request.getStock())
                .precio(request.getPrecio())
                .estacion(request.getEstacion())
                .build();
        return varianteProductoRepository.save(variante);
    }

    @Override
    @Transactional
    public VarianteProducto update(Long id, VarianteProductoRequest request) {
        VarianteProducto actual = findById(id);
        actual.setProducto(productoService.findById(request.getProductoId()));
        actual.setColor(request.getColor());
        actual.setTalla(request.getTalla());
        actual.setMaterial(request.getMaterial());
        actual.setPeso(request.getPeso());
        actual.setStock(request.getStock());
        actual.setPrecio(request.getPrecio());
        actual.setEstacion(request.getEstacion());
        return varianteProductoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        varianteProductoRepository.deleteById(id);
    }

    @Override
    public BigDecimal getPrecio(Long id) {
        return findById(id).getPrecio();
    }

    @Override
    public boolean tieneStock(Long id, Integer cantidad) {
        return findById(id).getStock() >= cantidad;
    }

    @Override
    @Transactional
    public VarianteProducto descontarStock(Long id, Integer cantidad) {
        VarianteProducto variante = findById(id);
        if (variante.getStock() < cantidad) {
            throw new BusinessRuleException("Stock insuficiente para variante id " + id
                    + ". Stock actual: " + variante.getStock() + ", solicitado: " + cantidad);
        }
        variante.setStock(variante.getStock() - cantidad);
        return varianteProductoRepository.save(variante);
    }
}
