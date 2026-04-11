package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VarianteProductoServiceImpl implements VarianteProductoService {

    private final VarianteProductoRepository varianteProductoRepository;
    private final ProductoRepository productoRepository;

    @Override
    public List<VarianteProducto> findAll() {
        return varianteProductoRepository.findAll();
    }

    @Override
    public VarianteProducto findById(Long id) {
        return varianteProductoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + id));
    }

    @Override
    public VarianteProducto create(VarianteProducto variante) {
        variante.setProducto(productoRepository.findById(variante.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + variante.getProducto().getId())));
        return varianteProductoRepository.save(variante);
    }

    @Override
    public VarianteProducto update(Long id, VarianteProducto variante) {
        VarianteProducto actual = findById(id);
        actual.setProducto(productoRepository.findById(variante.getProducto().getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + variante.getProducto().getId())));
        actual.setColor(variante.getColor());
        actual.setTalla(variante.getTalla());
        actual.setMaterial(variante.getMaterial());
        actual.setPeso(variante.getPeso());
        actual.setStock(variante.getStock());
        actual.setPrecio(variante.getPrecio());
        actual.setEstacion(variante.getEstacion());
        return varianteProductoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        varianteProductoRepository.deleteById(id);
    }

    @Override
    public BigDecimal getPrecio(Long id) {
        return findById(id).getPrecio();
    }

    @Override
    public boolean tieneStock(Long id, Integer cantidad) {
        VarianteProducto variante = findById(id);
        return variante.getStock() >= cantidad;
    }

    @Override
    public VarianteProducto descontarStock(Long id, Integer cantidad) {
        VarianteProducto variante = findById(id);
        if (variante.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para variante: " + id);
        }
        variante.setStock(variante.getStock() - cantidad);
        return varianteProductoRepository.save(variante);
    }
}

