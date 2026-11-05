package com.companybot.controller;

import com.companybot.config.MapsProperties;
import com.companybot.service.MailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContatoController {

    private final MailService mailService;
    private final MapsProperties mapsProperties;

    public ContatoController(MailService mailService, MapsProperties mapsProperties) {
        this.mailService = mailService;
        this.mapsProperties = mapsProperties;
    }

    /** GET /contato -> página com o formulário. */
    @GetMapping("/contato")
    public String contato(Model model) {
        model.addAttribute("pageTitle", "Contato");
        model.addAttribute("mapsApiKey", mapsProperties.apiKey());
        model.addAttribute("mapsHabilitado", mapsProperties.habilitado());
        return "admin/contato";
    }

    /** POST /contato -> envia o e-mail (equivalente ao email.php). */
    @PostMapping("/contato")
    public String enviar(@RequestParam("name") String nome,
                         @RequestParam(value = "phone", required = false) String telefone,
                         @RequestParam("email") String email,
                         @RequestParam("message") String mensagem,
                         RedirectAttributes redirect) {

        if (nome == null || nome.isBlank() || email == null || email.isBlank()
                || mensagem == null || mensagem.isBlank()) {
            redirect.addFlashAttribute("error", "Preencha os campos obrigatórios!");
            return "redirect:/contato";
        }

        boolean enviado = mailService.enviarContato(nome.trim(), email.trim(),
                telefone == null ? "" : telefone.trim(), mensagem.trim());
        if (enviado) {
            redirect.addFlashAttribute("success", "Mensagem enviada com sucesso! Obrigado pelo contato.");
        } else {
            redirect.addFlashAttribute("error", "Não foi possível enviar sua mensagem. Tente novamente.");
        }
        return "redirect:/contato";
    }
}
