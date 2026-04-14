package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);

    Optional<Carrito> findByUsuarioIdAndEstado(Long usuarioId, EstadoCarrito estado);

    @Query("SELECT c FROM Carrito c WHERE c.estado = 'ACTIVO' AND c.fechaUltimaModificacion < :limite")
    List<Carrito> findActivosNoModificadosDesde(@Param("limite") LocalDateTime limite);

    boolean existsByDescuentoIdAndEstado(Long descuentoId, EstadoCarrito estado);
}
