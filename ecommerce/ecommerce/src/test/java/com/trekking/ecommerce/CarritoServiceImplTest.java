package com.trekking.ecommerce;

import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.service.VarianteProductoService;
import com.trekking.ecommerce.service.impl.CarritoServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ItemCarritoRepository itemCarritoRepository;
    @Mock
    private VarianteProductoService varianteProductoService;
    @Mock
    private DescuentoRepository descuentoRepository;
    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private ItemOrdenRepository itemOrdenRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    @Test
    void calcularTotal_retornaLaSumaDeLosItems() {
        Long carritoId = 1L;
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .descuento(null)
                .build();

        ItemCarrito item1 = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("100.00"))
                .cantidad(2)
                .build();
        ItemCarrito item2 = ItemCarrito.builder()
                .carrito(carrito)
                .precioUnitario(new BigDecimal("50.00"))
                .cantidad(3)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item1, item2));

        BigDecimal total = carritoService.calcularTotal(carritoId);

        // 100*2 + 50*3 = 200 + 150 = 350
        assertThat(total).isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    void realizarCompra_carritoVacio_lanzaBusinessRuleException() {
        Long carritoId = 2L;
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.VACIO)
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of());

        assertThatThrownBy(() -> carritoService.realizarCompra(carritoId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    void realizarCompra_stockInsuficiente_lanzaBusinessRuleException() {
        Long carritoId = 3L;
        Long varianteId = 10L;

        Producto producto = Producto.builder()
                .nombre("Campera")
                .estado(EstadoProducto.ACTIVO)
                .build();
        VarianteProducto variante = VarianteProducto.builder()
                .id(varianteId)
                .producto(producto)
                .stock(0)
                .precio(new BigDecimal("500.00"))
                .build();
        Carrito carrito = Carrito.builder()
                .id(carritoId)
                .estado(EstadoCarrito.ACTIVO)
                .build();
        ItemCarrito item = ItemCarrito.builder()
                .carrito(carrito)
                .variante(variante)
                .cantidad(2)
                .precioUnitario(variante.getPrecio())
                .build();

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoId(carritoId)).thenReturn(List.of(item));
        when(varianteProductoService.tieneStock(varianteId, 2)).thenReturn(false);

        assertThatThrownBy(() -> carritoService.realizarCompra(carritoId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}
