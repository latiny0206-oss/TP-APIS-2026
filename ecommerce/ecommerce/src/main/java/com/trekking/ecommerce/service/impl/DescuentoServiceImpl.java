package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.service.DescuentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado: " + id));
    }

    @Override
    public Descuento create(Descuento descuento) {
        return descuentoRepository.save(descuento);
    }

    @Override
    public Descuento update(Long id, Descuento descuento) {
        Descuento actual = findById(id);
        actual.setTipo(descuento.getTipo());
        actual.setValor(descuento.getValor());
        actual.setFechaInicio(descuento.getFechaInicio());
        actual.setFechaFin(descuento.getFechaFin());
        actual.setEstado(descuento.getEstado());
        actual.setPorcentaje(descuento.getPorcentaje());
        return descuentoRepository.save(actual);
    }

    @Override
    public void delete(Long id) {
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
        return monto.multiply(porcentaje).divide(BigDecimal.valueOf(100));
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

