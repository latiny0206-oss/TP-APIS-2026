package com.trekking.ecommerce;

import com.trekking.ecommerce.dto.DescuentoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.service.impl.DescuentoServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescuentoServiceImplTest {

    @Mock private DescuentoRepository descuentoRepository;
    @Mock private CarritoRepository carritoRepository;

    @InjectMocks
    private DescuentoServiceImpl descuentoService;

    // ─────────────────────────────────────────────────────────────────────────
    // validarReglas — create
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void create_conFechaFinAntesDeInicio_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.FIJO);
        request.setFechaInicio(LocalDate.now().plusDays(5));
        request.setFechaFin(LocalDate.now());          // fin ANTES que inicio

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("fecha de fin");
    }

    @Test
    void create_conTipoPorcentajeSinCampoPorcentaje_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.PORCENTAJE);
        request.setPorcentaje(null);                   // obligatorio para PORCENTAJE

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("porcentaje");
    }

    @Test
    void create_conPorcentajeFueraDe100_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.PORCENTAJE);
        request.setPorcentaje(150.0);                  // > 100

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("máximo 100");
    }

    @Test
    void create_conTipoFijoYValorCero_lanzaBusinessRuleException() {
        DescuentoRequest request = descuentoRequestBase(TipoDescuento.FIJO);
        request.setValor(BigDecimal.ZERO);             // debe ser > 0

        assertThatThrownBy(() -> descuentoService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("mayor a 0");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calcularDescuento
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void calcularDescuento_fijo_menorAlMonto_devuelveElValorFijo() {
        Long id = 1L;
        Descuento d = descuentoActivo(TipoDescuento.FIJO, new BigDecimal("30.00"), null);
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("200.00"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void calcularDescuento_fijo_mayorAlMonto_devuelveElMontoCompleto() {
        Long id = 2L;
        Descuento d = descuentoActivo(TipoDescuento.FIJO, new BigDecimal("500.00"), null);
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        // el descuento no puede ser mayor que lo que hay que pagar
        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("100.00"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void calcularDescuento_porcentaje_calculaCorrectamente() {
        Long id = 3L;
        Descuento d = descuentoActivo(TipoDescuento.PORCENTAJE, new BigDecimal("20"), 20.0);
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("150.00"));

        // 150 * 20 / 100 = 30
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void calcularDescuento_descuentoExpirado_devuelveCero() {
        Long id = 4L;
        Descuento d = Descuento.builder()
                .id(id)
                .tipo(TipoDescuento.FIJO)
                .valor(new BigDecimal("50.00"))
                .estado(EstadoDescuento.EXPIRADO)
                .fechaInicio(LocalDate.now().minusDays(10))
                .fechaFin(LocalDate.now().minusDays(1))
                .build();
        when(descuentoRepository.findById(id)).thenReturn(Optional.of(d));

        BigDecimal resultado = descuentoService.calcularDescuento(id, new BigDecimal("200.00"));

        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private DescuentoRequest descuentoRequestBase(TipoDescuento tipo) {
        DescuentoRequest req = new DescuentoRequest();
        req.setNombre("Descuento Test");
        req.setTipo(tipo);
        req.setValor(new BigDecimal("50.00"));
        req.setPorcentaje(tipo == TipoDescuento.PORCENTAJE ? 10.0 : null);
        req.setFechaInicio(LocalDate.now());
        req.setFechaFin(LocalDate.now().plusDays(30));
        req.setEstado(EstadoDescuento.ACTIVO);
        return req;
    }

    private Descuento descuentoActivo(TipoDescuento tipo, BigDecimal valor, Double porcentaje) {
        return Descuento.builder()
                .tipo(tipo)
                .valor(valor)
                .porcentaje(porcentaje)
                .estado(EstadoDescuento.ACTIVO)
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(1))
                .build();
    }
}
