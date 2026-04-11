package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.repository.CategoriaRepository;
import com.trekking.ecommerce.repository.MarcaRepository;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import com.trekking.ecommerce.service.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
    }

    @Override
    public Producto create(Producto producto) {
        producto.setMarca(marcaRepository.findById(producto.getMarca().getId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + producto.getMarca().getId())));
        producto.setCategoria(categoriaRepository.findById(producto.getCategoria().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + producto.getCategoria().getId())));
        return productoRepository.save(producto);
    }

    @Override
    public Producto update(Long id, Producto producto) {
        Producto actual = findById(id);
        actual.setMarca(marcaRepository.findById(producto.getMarca().getId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + producto.getMarca().getId())));
        actual.setCategoria(categoriaRepository.findById(producto.getCategoria().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + producto.getCategoria().getId())));
        actual.setNombre(producto.getNombre());
        actual.setDescripcion(producto.getDescripcion());
        actual.setEstado(producto.getEstado());
        actual.setPrecioBase(producto.getPrecioBase());
        return productoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public boolean estaDisponible(Long id) {
        Producto producto = findById(id);
        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            return false;
        }
        return varianteProductoRepository.findByProductoId(id).stream()
                .map(variante -> variante.getStock())
                .anyMatch(stock -> stock != null && stock > 0);
    }
}

