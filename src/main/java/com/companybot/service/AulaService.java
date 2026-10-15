package com.companybot.service;

import com.companybot.dto.Aula;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Area de conteudo de aulas e dicas. O acervo fica em
 * src/main/resources/conteudo/aulas.json e e carregado uma vez
 * na subida da aplicacao, entao publicar uma aula nova nao exige
 * migration nem ajuste no banco legado.
 */
@Component
public class AulaService {

    private static final Logger log = LoggerFactory.getLogger(AulaService.class);
    private static final String ARQUIVO = "conteudo/aulas.json";
    private static final int MAXIMO_RELACIONADAS = 3;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    private List<Aula> aulas = List.of();

    public AulaService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void carregar() {
        Resource resource = resourceLoader.getResource("classpath:" + ARQUIVO);
        if (!resource.exists()) {
            log.warn("Acervo de aulas nao encontrado em {} - area publicada sem conteudo.", ARQUIVO);
            return;
        }

        try (InputStream in = resource.getInputStream()) {
            ObjectMapper mapper = objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Aula> carregadas = mapper.readValue(in, new TypeReference<List<Aula>>() {});
            aulas = normalizar(carregadas);
            log.info("Area de aulas carregada: {} aulas em {} categorias.", aulas.size(), categorias().size());
        } catch (IOException e) {
            log.error("Falha ao ler o acervo de aulas em {}.", ARQUIVO, e);
        }
    }

    /** Todas as aulas, da mais recente para a mais antiga. */
    public List<Aula> listar() {
        return aulas;
    }

    /** Aulas em destaque usadas nos destaques da listagem, no maximo 3. */
    public List<Aula> destaques() {
        return aulas.stream()
                .filter(Aula::emDestaque)
                .limit(MAXIMO_RELACIONADAS)
                .toList();
    }

    /** Listagem filtrada por categoria e/ou termo de busca. */
    public List<Aula> listar(String categoria, String busca) {
        String alvo = (categoria == null ? "" : categoria).trim().toLowerCase(Locale.ROOT);
        return aulas.stream()
                .filter(aula -> {
                    boolean categoriaOk = alvo.isEmpty()
                            || (aula.categoria() != null && aula.categoria().toLowerCase(Locale.ROOT).contains(alvo));
                    return categoriaOk && aula.correspondeAoTermo(busca);
                })
                .toList();
    }

    public Optional<Aula> porSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        String alvo = slug.trim().toLowerCase(Locale.ROOT);
        return aulas.stream().filter(aula -> alvo.equals(aula.slug())).findFirst();
    }

    /** Categorias existentes no acervo, em ordem alfabetica e sem repeticao. */
    public List<String> categorias() {
        Map<String, String> unicas = new LinkedHashMap<>();
        aulas.stream()
                .map(Aula::categoria)
                .filter(categoria -> categoria != null && !categoria.isBlank())
                .forEach(categoria -> unicas.putIfAbsent(categoria, categoria));
        return unicas.values().stream().sorted().toList();
    }

    /** Aulas da mesma categoria, excluindo a própria. */
    public List<Aula> relacionadas(Aula aula, int limite) {
        if (aula == null || limite <= 0) {
            return List.of();
        }
        List<Aula> mesmaCategoria = aulas.stream()
                .filter(outra -> !outra.slug().equals(aula.slug()))
                .filter(outra -> aula.categoria() != null && aula.categoria().equals(outra.categoria()))
                .toList();

        List<Aula> resultado = new ArrayList<>(mesmaCategoria);
        if (resultado.size() < limite) {
            for (Aula outra : aulas) {
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

    /** Descarta slugs invalidos/vazios e ordena o acervo por data decrescente. */
    private List<Aula> normalizar(List<Aula> carregadas) {
        if (carregadas == null) {
            return List.of();
        }
        List<Aula> validas = carregadas.stream()
                .filter(aula -> aula != null && aula.slug() != null && !aula.slug().isBlank())
                .filter(aula -> aula.titulo() != null && !aula.titulo().isBlank())
                .toList();

        List<Aula> ordenadas = new ArrayList<>(validas);
        ordenadas.sort(Comparator.comparing(Aula::data, Comparator.nullsLast(Comparator.reverseOrder())));
        return List.copyOf(ordenadas);
    }
}