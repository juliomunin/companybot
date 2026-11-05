package com.companybot.service;

import com.companybot.dto.Aula;
import com.companybot.repository.AulaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Regras de negócio da área de conteúdo: busca, filtros, destaques e
 * aulas relacionadas. A origem dos dados fica abstraída em
 * {@link AulaRepository}.
 */
@Service
public class AulaService {

    private static final int MAXIMO_DESTAQUES = 3;

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    /** Todas as aulas, da mais recente para a mais antiga. */
    public List<Aula> listar() {
        return aulaRepository.findAll();
    }

    /** Aulas em destaque usadas na listagem, no máximo 3. */
    public List<Aula> destaques() {
        return aulaRepository.findAll().stream()
                .filter(Aula::emDestaque)
                .limit(MAXIMO_DESTAQUES)
                .toList();
    }

    /** Listagem filtrada por categoria e/ou termo de busca. */
    public List<Aula> listar(String categoria, String busca) {
        String alvo = (categoria == null ? "" : categoria).trim().toLowerCase(Locale.ROOT);
        return aulaRepository.findAll().stream()
                .filter(aula -> {
                    boolean categoriaOk = alvo.isEmpty()
                            || (aula.categoria() != null
                            && aula.categoria().toLowerCase(Locale.ROOT).contains(alvo));
                    return categoriaOk && aula.correspondeAoTermo(busca);
                })
                .toList();
    }

    public Optional<Aula> porSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        String alvo = slug.trim();
        return aulaRepository.findAll().stream()
                .filter(aula -> aula.slug() != null && alvo.equalsIgnoreCase(aula.slug()))
                .findFirst();
    }

    /** Categorias existentes no acervo, em ordem alfabética e sem repetição. */
    public List<String> categorias() {
        Map<String, String> unicas = new LinkedHashMap<>();
        aulaRepository.findAll().stream()
                .map(Aula::categoria)
                .filter(categoria -> categoria != null && !categoria.isBlank())
                .forEach(categoria -> unicas.putIfAbsent(categoria, categoria));
        return unicas.values().stream().sorted().toList();
    }

    /** Aulas da mesma categoria, completando com as mais recentes se necessário. */
    public List<Aula> relacionadas(Aula aula, int limite) {
        if (aula == null || limite <= 0) {
            return List.of();
        }
        List<Aula> todas = aulaRepository.findAll();
        List<Aula> resultado = new ArrayList<>(todas.stream()
                .filter(outra -> !outra.slug().equals(aula.slug()))
                .filter(outra -> mesmaCategoria(aula, outra))
                .toList());

        if (resultado.size() < limite) {
            for (Aula outra : todas) {
                if (resultado.size() >= limite) {
                    break;
                }
                if (outra.slug().equals(aula.slug()) || resultado.contains(outra)) {
                    continue;
                }
                resultado.add(outra);
            }
        }
        return resultado.stream().limit(limite).toList();
    }

    private boolean mesmaCategoria(Aula aula, Aula outra) {
        return aula.categoria() != null
                && outra.categoria() != null
                && aula.categoria().equalsIgnoreCase(outra.categoria());
    }
}
