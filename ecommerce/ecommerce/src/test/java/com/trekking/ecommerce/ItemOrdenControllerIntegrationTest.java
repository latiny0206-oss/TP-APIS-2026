package com.trekking.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trekking.ecommerce.model.Categoria;
import com.trekking.ecommerce.model.Marca;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.Producto;
import com.trekking.ecommerce.model.Usuario;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.Estacion;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.EstadoUsuario;
import com.trekking.ecommerce.model.enums.RolUsuario;
import com.trekking.ecommerce.repository.CategoriaRepository;
import com.trekking.ecommerce.repository.MarcaRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.repository.ProductoRepository;
import com.trekking.ecommerce.repository.UsuarioRepository;
import com.trekking.ecommerce.repository.VarianteProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemOrdenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

    @Test
    @WithMockUser
    void createItemOrdenWithAssociations() throws Exception {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username("usuario_rel_test")
                .email("usuario_rel_test@mail.com")
                .password("secret")
                .nombre("Usuario")
                .apellido("Test")
                .rol(RolUsuario.CLIENTE)
                .estado(EstadoUsuario.ACTIVO)
                .build());

        Orden orden = ordenRepository.save(Orden.builder()
                .usuario(usuario)
                .fechaCreacion(LocalDateTime.now())
                .montoFinal(BigDecimal.valueOf(1000))
                .estado(EstadoOrden.PENDIENTE)
                .build());

        Marca marca = marcaRepository.save(Marca.builder()
                .nombre("Marca Test")
                .descripcion("Desc")
                .build());

        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Categoria Test")
                .descripcion("Desc")
                .build());

        Producto producto = productoRepository.save(Producto.builder()
                .marca(marca)
                .categoria(categoria)
                .nombre("Producto Test")
                .descripcion("Desc")
                .estado(EstadoProducto.ACTIVO)
                .precioBase(BigDecimal.valueOf(300))
                .build());

        VarianteProducto variante = varianteProductoRepository.save(VarianteProducto.builder()
                .producto(producto)
                .color("Negro")
                .talla("M")
                .material("Nylon")
                .peso(BigDecimal.valueOf(0.8))
                .stock(10)
                .precio(BigDecimal.valueOf(350))
                .estacion(Estacion.VERANO)
                .build());

        String payload = objectMapper.writeValueAsString(new ItemOrdenRequest(
                new RefRequest(orden.getId()),
                new RefRequest(variante.getId()),
                2,
                BigDecimal.valueOf(350)));

        mockMvc.perform(post("/api/items-orden")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orden.id").value(orden.getId()))
                .andExpect(jsonPath("$.variante.id").value(variante.getId()))
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    private record RefRequest(Long id) {
    }

    private record ItemOrdenRequest(RefRequest orden, RefRequest variante, Integer cantidad, BigDecimal precioAlMomento) {
    }
}


