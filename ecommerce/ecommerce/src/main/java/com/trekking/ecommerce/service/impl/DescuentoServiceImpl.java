package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.service.DescuentoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DescuentoServiceImpl implements DescuentoService {

    private final DescuentoRepository descuentoRepository;

    @Override
    public List<Descuento> findAll() {
        return descuentoRepository.findAll();
    }

    @Override
    public Descuento findById(Long id) {
        return descuentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento", id));
    }

    @Override
    @Transactional
    public Descuento create(DescuentoRequest request) {
        Descuento descuento = Descuento.builder()
                .tipo(request.getTipo())
                .valor(request.getValor())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estado(request.getEstado())
                .porcentaje(request.getPorcentaje())
                .build();
        return descuentoRepository.save(descuento);
    }

    @Override
    @Transactional
    public Descuento update(Long id, DescuentoRequest request) {
        Descuento actual = findById(id);
        actual.setTipo(request.getTipo());
        actual.setValor(request.getValor());
        actual.setFechaInicio(request.getFechaInicio());
        actual.setFechaFin(request.getFechaFin());
        actual.setEstado(request.getEstado());
        actual.setPorcentaje(request.getPorcentaje());
        return descuentoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        descuentoRepository.deleteById(id);
    }

    @Override
    public BigDecimal calcularDescuento(Long id, BigDecimal monto) {
        Descuento descuento = findById(id);
        if (!estaVigente(id)) {
            return BigDecimal.ZERO;
        }
        if (descuento.getTipo() == TipoDescuento.FIJO) {
            return descuento.getValor().min(monto);
        }
        BigDecimal porcentaje = descuento.getPorcentaje() != null
                ? BigDecimal.valueOf(descuento.getPorcentaje())
                : descuento.getValor();
        return monto.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean estaVigente(Long id) {
        Descuento descuento = findById(id);
        LocalDate hoy = LocalDate.now();
        return descuento.getEstado() == EstadoDescuento.ACTIVO
                && !hoy.isBefore(descuento.getFechaInicio())
                && !hoy.isAfter(descuento.getFechaFin());
    }
}
