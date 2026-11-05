package com.companybot.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaginasPublicasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeRenderizaSemErro() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CompanyBot")))
                .andExpect(content().string(containsString("Soluções com ferramentas Open Source")));
    }

    @Test
    void listagemDeAulasRenderizaDestaquesEFiltros() throws Exception {
        mockMvc.perform(get("/aulas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aulas e Dicas")))
                .andExpect(content().string(containsString("git-rebase-vs-merge")));

        mockMvc.perform(get("/aulas").param("busca", "rebase"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("git-rebase-vs-merge")));
    }

    @Test
    void detalheDaAulaRenderizaConteudo() throws Exception {
        mockMvc.perform(get("/aulas/git-rebase-vs-merge"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Git: principais comandos")))
                .andExpect(content().string(containsString("Documentação oficial do Git")))
                .andExpect(content().string(containsString("https://git-scm.com/docs")));
    }

    @Test
    void slugInexistenteRedirecionaParaListagem() throws Exception {
        mockMvc.perform(get("/aulas/nao-existe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/aulas"));
    }

    @Test
    void paginasDeAutenticacaoRenderizam() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Entrar")));

        mockMvc.perform(get("/cadastrar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Criar Conta")));

        mockMvc.perform(get("/forgot"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recuperar senha")));
    }

    @Test
    void contatoRenderizaSemMapaQuandoNaoHaChave() throws Exception {
        mockMvc.perform(get("/contato"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rua São Nocásio, 211")));
    }

    @Test
    void areaRestritaRedirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    void rotaInexistenteRetornaPagina404Amigavel() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Página não encontrada")))
                .andExpect(content().string(containsString("/rota-que-nao-existe")));
    }
}
