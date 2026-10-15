package com.companybot.dto;

import com.companybot.entity.Person;
import com.companybot.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mesmo modelo que o PHP montava com User::getData() apos o JOIN
 * tb_users x tb_persons (campos usados pelas views).
 */
public record UsuarioPerfil(
    Long iduser,
    Long idperson,
    String deslogin,
    Integer inadmin,
    LocalDateTime dtregister,
    String desperson,
    String desemail,
    String desnick,
    Integer nrddd,
    Long nrphone,
    LocalDate dtbirth
) {

    public static UsuarioPerfil of(Usuario user, Person person) {
        return new UsuarioPerfil(
            user.getIduser(),
            user.getIdperson(),
            user.getDeslogin(),
            user.getInadmin(),
            user.getDtregister(),
            person != null ? person.getDesperson() : null,
            person != null ? person.getDesemail() : null,
            person != null ? person.getDesnick() : null,
            person != null ? person.getNrddd() : null,
            person != null ? person.getNrphone() : null,
            person != null ? person.getDtbirth() : null
        );
    }
}