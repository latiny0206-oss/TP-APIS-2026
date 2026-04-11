package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Orden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
	List<Orden> findByUsuarioId(Long usuarioId);
}

