package com.companybot.service;

import com.companybot.dto.Aula;
import com.companybot.repository.AulaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AulaServiceTest {

    private static Aula aula(String slug, String titulo, String categoria, boolean destaque, LocalDate data) {
        return new Aula(slug, titulo, "Resumo de " + titulo, categoria, "Iniciante",
                "fa-code", 30, data, destaque, List.of(categoria), List.of());
    }

    private static AulaRepository repositorio(Aula... aulas) {
        return () -> List.of(aulas);
    }

    @Test
    void destaquesRetornaSomenteAsMarcadasELimitaATres() {
        AulaService service = new AulaService(repositorio(
                aula("a", "A", "Git", true, LocalDate.of(2026, 1, 1)),
                aula("b", "B", "Git", false, LocalDate.of(2026, 1, 2)),
                aula("c", "C", "Git", true, LocalDate.of(2026, 1, 3)),
                aula("d", "D", "Git", true, LocalDate.of(2026, 1, 4)),
                aula("e", "E", "Git", true, LocalDate.of(2026, 1, 5))
        ));

        assertThat(service.destaques())
                .extracting(Aula::slug)
                .containsExactly("a", "c", "d");
    }

    @Test
    void listarFiltraPorCategoriaETermoDeBusca() {
        AulaService service = new AulaService(repositorio(
                aula("merge", "Merge sem medo", "Git", true, LocalDate.of(2026, 1, 1)),
                aula("record", "Records em Java", "Java", true, LocalDate.of(2026, 1, 2)),
                aula("rebase", "Rebase seguro", "Git", false, LocalDate.of(2026, 1, 3))
        ));

        assertThat(service.listar("git", null))
                .extracting(Aula::slug)
                .containsExactly("merge", "rebase");

        assertThat(service.listar(null, "seguro"))
                .extracting(Aula::slug)
                .containsExactly("rebase");

        assertThat(service.listar("git", "seguro"))
                .extracting(Aula::slug)
                .containsExactly("rebase");

        assertThat(service.listar("python", null)).isEmpty();
    }

    @Test
    void porSlugIgnoraCaixaEEspacosEVazioRetornaEmpty() {
        AulaService service = new AulaService(repositorio(
                aula("git-merge", "Merge", "Git", false, LocalDate.of(2026, 1, 1))
        ));

        assertThat(service.porSlug(" GIT-MERGE ")).map(Aula::slug).contains("git-merge");
        assertThat(service.porSlug(" ")).isEmpty();
        assertThat(service.porSlug(null)).isEmpty();
        assertThat(service.porSlug("inexistente")).isEmpty();
    }

    @Test
    void categoriasSaoUnicasEOrdenadas() {
        AulaService service = new AulaService(repositorio(
                aula("a", "A", "Python", false, LocalDate.of(2026, 1, 1)),
                aula("b", "B", "Git", false, LocalDate.of(2026, 1, 2)),
                aula("c", "C", "Python", false, LocalDate.of(2026, 1, 3)),
                new Aula("d", "D", "Sem categoria", null, null, null, null, null, false, List.of(), List.of())
        ));

        assertThat(service.categorias()).containsExactly("Git", "Python");
    }

    @Test
    void relacionadasPriorizamMesmaCategoriaECompletamAteOLimite() {
        AulaService service = new AulaService(repositorio(
                aula("atual", "Atual", "Git", false, LocalDate.of(2026, 1, 5)),
                aula("git-1", "Git 1", "Git", false, LocalDate.of(2026, 1, 4)),
                aula("java-1", "Java 1", "Java", false, LocalDate.of(2026, 1, 3)),
                aula("git-2", "Git 2", "Git", false, LocalDate.of(2026, 1, 2))
        ));

        Aula atual = service.porSlug("atual").orElseThrow();

        assertThat(service.relacionadas(atual, 2))
                .extracting(Aula::slug)
                .containsExactly("git-1", "git-2");

        assertThat(service.relacionadas(atual, 3))
                .extracting(Aula::slug)
                .containsExactly("git-1", "git-2", "java-1");

        assertThat(service.relacionadas(null, 3)).isEmpty();
        assertThat(service.relacionadas(atual, 0)).isEmpty();
    }
}
