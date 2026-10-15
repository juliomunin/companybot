package com.companybot.controller;

import com.companybot.service.MailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContatoController {

    private final MailService mailService;

    public ContatoController(MailService mailService) {
        this.mailService = mailService;
    }

    /** GET /contato -> página com o formulário. */
    @GetMapping("/contato")
    public String contato() {
        return "admin/contato";
    }

    /** POST /contato -> envia o e-mail (equivalente ao email.php). */
    @PostMapping("/contato")
    public String enviar(@RequestParam("name") String nome,
                         @RequestParam(value = "phone", required = false) String telefone,
                         @RequestParam("email") String email,
                         @RequestParam("message") String mensagem,
                         RedirectAttributes redirect) {

        if (nome == null || nome.isBlank() || email == null || email.isBlank() || mensagem == null || mensagem.isBlank()) {
            redirect.addFlashAttribute("error", "Preencha os campos obrigatórios!");
            return "redirect:/contato";
        }

        boolean enviado = mailService.enviarContato(nome, email, telefone == null ? "" : telefone, mensagem);
        if (enviado) {
            redirect.addFlashAttribute("success", "Mensagem enviada com sucesso! Obrigado pelo contato.");
        } else {
            redirect.addFlashAttribute("error", "Não foi possível enviar sua mensagem. Tente novamente.");
        }
        return "redirect:/contato";
    }
}