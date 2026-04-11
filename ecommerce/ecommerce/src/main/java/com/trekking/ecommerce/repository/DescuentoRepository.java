package com.trekking.ecommerce.repository;

import com.trekking.ecommerce.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {
}

