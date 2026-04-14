package com.trekking.ecommerce.dto;

import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DescuentoRequest {

    @NotNull
    private TipoDescuento tipo;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal valor;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    private EstadoDescuento estado;

    private Double porcentaje;
}
