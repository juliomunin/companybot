package com.companybot.controller;

import com.companybot.service.MailService;
import com.companybot.service.PasswordResetService;
import com.companybot.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final MailService mailService;
    private final PasswordResetService passwordResetService;

    public AuthController(UsuarioService usuarioService, MailService mailService,
                          PasswordResetService passwordResetService) {
        this.usuarioService = usuarioService;
        this.mailService = mailService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * GET /login  -> formulário (processado pelo Spring Security via POST /login).
     * O parâmetro ?error=true é adicionado automaticamente no failureUrl.
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "cadastrado", required = false) String cadastrado,
                        @RequestParam(value = "recuperado", required = false) String recuperado,
                        @RequestParam(value = "redefinido", required = false) String redefinido,
                        Model model) {

        model.addAttribute("pageTitle", "Entrar");
        if (error != null) {
            model.addAttribute("error", "Usuário inexistente ou senha inválida!");
        }
        if (cadastrado != null) {
            model.addAttribute("success", "Conta criada com sucesso! Efetue o login.");
        }
        if (recuperado != null) {
            model.addAttribute("success", "Enviamos as instruções para o seu e-mail.");
        }
        if (redefinido != null) {
            model.addAttribute("success", "Senha alterada com sucesso! Entre com a nova senha.");
        }
        return "admin/login";
    }

    /** GET /cadastrar -> formulário de registro. */
    @GetMapping("/cadastrar")
    public String cadastrar(Model model) {
        model.addAttribute("pageTitle", "Criar conta");
        return "admin/register";
    }

    /** POST /registrar -> cria Person + Usuario (equivalente ao antigo "registrar"). */
    @PostMapping("/registrar")
    public String registrar(@RequestParam("nome") String nome,
                            @RequestParam(value = "sobnome", required = false) String sobrenome,
                            @RequestParam("deslogin") String deslogin,
                            @RequestParam("despassword") String despassword,
                            RedirectAttributes redirect) {

        try {
            usuarioService.registrar(nome, sobrenome, deslogin, despassword);
            redirect.addAttribute("cadastrado", "true");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/cadastrar";
        }
    }

    /** GET /forgot -> formulário de recuperação. */
    @GetMapping("/forgot")
    public String forgot(Model model) {
        model.addAttribute("pageTitle", "Recuperar senha");
        return "admin/relembrar";
    }

    /** POST /forgot -> envia e-mail de recuperação (se a conta existir). */
    @PostMapping("/forgot")
    public String forgotPost(@RequestParam("deslogin") String deslogin,
                             RedirectAttributes redirect) {

        var pessoa = usuarioService.pessoaPorDeslogin(deslogin);
        if (pessoa.isPresent()) {
            String nome = pessoa.get().getDesperson() != null ? pessoa.get().getDesperson() : deslogin;
            mailService.enviarRecuperacao(deslogin, nome);
        }
        // A mesma resposta para contas existentes e inexistentes evita enumeração de usuários.
        redirect.addAttribute("recuperado", "true");
        return "redirect:/login";
    }

    /** GET /reset?token=... -> formulário para criar a nova senha. */
    @GetMapping("/reset")
    public String reset(@RequestParam(value = "token", required = false) String token,
                        Model model, RedirectAttributes redirect) {

        Optional<String> email = passwordResetService.validarToken(token);
        if (email.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Link de redefinição inválido ou expirado. Solicite um novo.");
            return "redirect:/forgot";
        }
        model.addAttribute("pageTitle", "Redefinir senha");
        model.addAttribute("token", token);
        return "admin/reset";
    }

    /** POST /reset -> grava a nova senha quando o token é válido. */
    @PostMapping("/reset")
    public String resetPost(@RequestParam("token") String token,
                            @RequestParam("despassword") String despassword,
                            @RequestParam("despassword2") String confirmacao,
                            RedirectAttributes redirect) {

        Optional<String> email = passwordResetService.validarToken(token);
        if (email.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Link de redefinição inválido ou expirado. Solicite um novo.");
            return "redirect:/forgot";
        }
        if (!despassword.equals(confirmacao)) {
            redirect.addFlashAttribute("error", "As senhas não conferem!");
            redirect.addAttribute("token", token);
            return "redirect:/reset";
        }
        try {
            usuarioService.alterarSenha(email.get(), despassword);
            redirect.addAttribute("redefinido", "true");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addAttribute("token", token);
            return "redirect:/reset";
        }
    }
}