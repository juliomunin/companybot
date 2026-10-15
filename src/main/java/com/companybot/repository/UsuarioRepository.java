package com.companybot.repository;

import com.companybot.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u ORDER BY u.dtregister DESC")
    List<Usuario> findRecentes();

    @Query(
        "SELECT u FROM Usuario u WHERE u.deslogin = :deslogin ORDER BY u.dtregister DESC"
    )
    List<Usuario> findByDesloginOrderByDtregisterDesc(@Param("deslogin") String deslogin);

    default Optional<Usuario> findLatestByDeslogin(String deslogin) {
        return findByDesloginOrderByDtregisterDesc(deslogin).stream().findFirst();
    }

    boolean existsByDeslogin(String deslogin);
}