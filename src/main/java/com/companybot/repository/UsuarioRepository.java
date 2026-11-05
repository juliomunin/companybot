package com.companybot.repository;

import com.companybot.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByDesloginIgnoreCaseOrderByDtregisterDesc(String deslogin);

    default Optional<Usuario> findLatestByDeslogin(String deslogin) {
        if (deslogin == null || deslogin.isBlank()) {
            return Optional.empty();
        }
        return findByDesloginIgnoreCaseOrderByDtregisterDesc(deslogin.trim()).stream().findFirst();
    }

    boolean existsByDesloginIgnoreCase(String deslogin);
}
