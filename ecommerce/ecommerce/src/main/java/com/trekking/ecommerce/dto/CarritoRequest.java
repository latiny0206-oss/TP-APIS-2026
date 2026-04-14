package com.trekking.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarritoRequest {

    @NotNull
    private Long usuarioId;

    private Long descuentoId;
}
