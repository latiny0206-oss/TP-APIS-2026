package com.trekking.ecommerce.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FotoResponse {
    Long id;
    Long productoId;
    String nombre;
    Integer orden;
}
