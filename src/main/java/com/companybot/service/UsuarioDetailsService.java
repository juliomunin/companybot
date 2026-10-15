package com.companybot.service;

import com.companybot.entity.Usuario;
import com.companybot.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Carrega o usuário pelo deslogin (tb_users) para o Spring Security.
 * Os hashes bcrypt gerados pelo password_hash do PHP ($2y$) são
 * validados normalmente pelo BCryptPasswordEncoder.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String deslogin) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findLatestByDeslogin(deslogin)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário inexistente"));

        String role = (usuario.getInadmin() != null && usuario.getInadmin() == 1)
            ? ROLE_ADMIN
            : ROLE_USER;

        return User.withUsername(usuario.getDeslogin())
            .password(usuario.getDespassword())
            .authorities(new SimpleGrantedAuthority(role))
            .build();
    }
}