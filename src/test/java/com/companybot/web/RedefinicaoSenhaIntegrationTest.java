package com.companybot.web;

import com.companybot.service.PasswordResetService;
import com.companybot.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedefinicaoSenhaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Test
    void fluxoCompletoDeRedefinicaoDeSenha() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        usuarioService.registrar("Teste", "Redefinição", email, "senhaAntiga1");
        String token = passwordResetService.gerarToken(email);

        mockMvc.perform(get("/reset").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Redefinir Senha")));

        mockMvc.perform(post("/reset").with(csrf())
                        .param("token", token)
                        .param("despassword", "senhaNova1")
                        .param("despassword2", "senhaNova1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?redefinido=true"));

        assertThat(usuarioService.autenticar(email, "senhaNova1")).isPresent();
        assertThat(usuarioService.autenticar(email, "senhaAntiga1")).isEmpty();
    }

    @Test
    void tokenInvalidoRedirecionaParaRecuperacao() throws Exception {
        mockMvc.perform(get("/reset").param("token", "token-invalido"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot"));
    }

    @Test
    void senhasDiferentesMantemOTokenEPedemCorrecao() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        usuarioService.registrar("Teste", "Redefinição", email, "senhaAntiga1");
        String token = passwordResetService.gerarToken(email);

        mockMvc.perform(post("/reset").with(csrf())
                        .param("token", token)
                        .param("despassword", "senhaNova1")
                        .param("despassword2", "outraSenha1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset?token=" + token));
    }
}
