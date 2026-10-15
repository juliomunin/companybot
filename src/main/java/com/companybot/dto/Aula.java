package com.companybot.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Uma aula publicada na area de conteudo. O corpo vem do arquivo
 * classpath:conteudo/aulas.json, entao publicar uma aula nova
 * significa apenas adicionar um objeto ao JSON.
 */
public record Aula(
    String slug,
    String titulo,
    String resumo,
    String categoria,
    String nivel,
    String icone,
    Integer duracaoMinutos,
    LocalDate data,
    Boolean destaque,
    List<String> tags,
    List<SecaoAula> secoes
) {

    public boolean emDestaque() {
        return Boolean.TRUE.equals(destaque);
    }

    public String dataFormatada() {
        return data == null ? "" : data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String duracaoFormatada() {
        if (duracaoMinutos == null || duracaoMinutos <= 0) {
            return "";
        }
        int horas = duracaoMinutos / 60;
        int minutos = duracaoMinutos % 60;
        if (horas == 0) {
            return minutos + " min";
        }
        return horas + "h" + (minutos > 0 ? String.format("%02d", minutos) : "");
    }

    /** Matches "rgx" contra titulo, resumo, categoria e tags. */
    public boolean correspondeAoTermo(String termo) {
        if (termo == null || termo.isBlank()) {
            return true;
        }
        String alvo = termo.trim().toLowerCase();
        if (titulo != null && titulo.toLowerCase().contains(alvo)) {
            return true;
        }
        if (resumo != null && resumo.toLowerCase().contains(alvo)) {
            return true;
        }
        if (categoria != null && categoria.toLowerCase().contains(alvo)) {
            return true;
        }
        if (tags != null) {
            return tags.stream().anyMatch(tag -> tag != null && tag.toLowerCase().contains(alvo));
        }
        return false;
    }
}