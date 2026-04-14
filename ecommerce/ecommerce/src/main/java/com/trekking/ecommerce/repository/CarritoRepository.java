package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);

    Optional<Carrito> findByUsuarioIdAndEstado(Long usuarioId, EstadoCarrito estado);
}
