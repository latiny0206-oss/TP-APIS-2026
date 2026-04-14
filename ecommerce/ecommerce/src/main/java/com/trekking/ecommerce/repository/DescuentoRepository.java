package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    List<Descuento> findByEstado(EstadoDescuento estado);

    @Query("SELECT d FROM Descuento d WHERE d.estado = 'ACTIVO' AND d.fechaFin < :hoy")
    List<Descuento> findActivosExpirados(@Param("hoy") LocalDate hoy);
}
