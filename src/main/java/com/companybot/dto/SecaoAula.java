package com.companybot.dto;

import java.util.List;

/**
 * Bloco de conteudo de uma aula. Uma secao pode ter texto corrido,
 * uma lista de topicos e um trecho de codigo opcional.
 */
public record SecaoAula(
    String titulo,
    String texto,
    List<String> topicos,
    String codigo,
    String linguagem
) {

    public boolean possuiTopicos() {
        return topicos != null && !topicos.isEmpty();
    }

    public boolean possuiCodigo() {
        return codigo != null && !codigo.isBlank();
    }
}