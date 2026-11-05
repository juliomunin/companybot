package com.companybot.repository;

import com.companybot.config.ContentProperties;
import com.companybot.dto.Aula;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Adaptador que carrega o acervo de aulas de um arquivo JSON no classpath.
 * A leitura acontece uma única vez na subida da aplicação.
 */
@Repository
public class JsonAulaRepository implements AulaRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonAulaRepository.class);

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final ContentProperties contentProperties;

    private volatile List<Aula> aulas = List.of();

    public JsonAulaRepository(ObjectMapper objectMapper,
                              ResourceLoader resourceLoader,
                              ContentProperties contentProperties) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.contentProperties = contentProperties;
    }

    @PostConstruct
    public void carregar() {
        String arquivo = contentProperties.aulasOuPadrao();
        Resource resource = resourceLoader.getResource("classpath:" + arquivo);
        if (!resource.exists()) {
            log.warn("Acervo de aulas não encontrado em {} - área publicada sem conteúdo.", arquivo);
            return;
        }

        try (InputStream in = resource.getInputStream()) {
            ObjectMapper mapper = objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Aula> carregadas = mapper.readValue(in, new TypeReference<List<Aula>>() {});
            aulas = normalizar(carregadas);
            log.info("Acervo de aulas carregado de {}: {} aulas.", arquivo, aulas.size());
        } catch (IOException e) {
            log.error("Falha ao ler o acervo de aulas em {}.", arquivo, e);
        }
    }

    @Override
    public List<Aula> findAll() {
        return aulas;
    }

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
