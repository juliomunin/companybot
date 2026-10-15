package com.companybot.controller;

import com.companybot.service.MailService;
import com.companybot.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final MailService mailService;

    public AuthController(UsuarioService usuarioService, MailService mailService) {
        this.usuarioService = usuarioService;
        this.mailService = mailService;
    }

    /**
     * GET /login  -> formulário (processado pelo Spring Security via POST /login).
     * O parâmetro ?error=true é adicionado automaticamente no failureUrl.
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "cadastrado", required = false) String cadastrado,
                        @RequestParam(value = "recuperado", required = false) String recuperado,
                        org.springframework.ui.Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuário inexistente ou senha inválida!");
        }
        if (cadastrado != null) {
            model.addAttribute("success", "Conta criada com sucesso! Efetue o login.");
        }
        if (recuperado != null) {
            model.addAttribute("success", "Enviamos as instruções para o seu e-mail.");
        }
        return "admin/login";
    }

    /** GET /cadastrar -> formulário de registro. */
    @GetMapping("/cadastrar")
    public String cadastrar() {
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
    public String forgot() {
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
            redirect.addAttribute("recuperado", "true");
        } else {
            redirect.addFlashAttribute("error", "E-mail não encontrado!");
        }
        return "redirect:/login";
    }
}