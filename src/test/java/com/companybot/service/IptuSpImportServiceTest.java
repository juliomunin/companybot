package com.companybot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IptuSpImportServiceTest {

    private JdbcTemplate jdbcTemplate;
    private IptuSpImportService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[0]);
        service = new IptuSpImportService(jdbcTemplate);
    }

    private static MockMultipartFile csv(String conteudo) {
        return new MockMultipartFile("file", "iptu.csv", "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("unchecked")
    void importaIgnorandoBomELinhasInvalidasEPreservandoPontoEVirgulaEntreAspas() throws Exception {
        String conteudo = "\uFEFFcnpj;contrib;cidade;empresa;divisao;\"Empreendimento; X\";bloco;unidade;parcela;ano;tipo\n"
                + "linha;incompleta\n"
                + "\n"
                + "123;456;7;8;9;\"Empreendimento; Y\";bloco;unidade;parcela;2026;IPTU\n";

        int inseridos = service.importar(csv(conteudo));

        assertThat(inseridos).isEqualTo(2);

        ArgumentCaptor<List<Object[]>> captor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        List<Object[]> linhas = captor.getValue();
        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(0)[0]).isEqualTo("cnpj");
        assertThat(linhas.get(0)[5]).isEqualTo("Empreendimento; X");
        assertThat(linhas.get(1)[0]).isEqualTo("123");
        assertThat(linhas.get(1)[5]).isEqualTo("Empreendimento; Y");
        assertThat(linhas.get(1)[10]).isEqualTo("IPTU");
    }

    @Test
    void importaEmLotesDeQuinhentasLinhas() throws Exception {
        StringBuilder conteudo = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            conteudo.append(i).append(";1;2;3;4;5;6;7;8;9;10\n");
        }

        int inseridos = service.importar(csv(conteudo.toString()));

        assertThat(inseridos).isEqualTo(501);
        verify(jdbcTemplate, times(2)).batchUpdate(anyString(), anyList());
    }

    @Test
    void rejeitaArquivoVazio() {
        MockMultipartFile vazio = new MockMultipartFile("file", "vazio.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> service.importar(vazio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nenhum arquivo");

        assertThatThrownBy(() -> service.importar(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
