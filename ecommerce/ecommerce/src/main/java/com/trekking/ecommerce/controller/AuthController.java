package com.trekking.ecommerce.controller;

import com.trekking.ecommerce.dto.AuthResponse;
import com.trekking.ecommerce.dto.LoginRequest;
import com.trekking.ecommerce.dto.UsuarioRequest;
import com.trekking.ecommerce.dto.UsuarioResponse;
import com.trekking.ecommerce.security.JwtUtil;
import com.trekking.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        String rol = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .rol(rol)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.create(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .rol(usuario.getRol().name())
                .build());
    }
}
