package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
}

