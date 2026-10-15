package com.companybot.controller;

import com.companybot.service.IptuSpImportService;
import com.companybot.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

@Controller
public class ProfileController {

    private final UsuarioService usuarioService;
    private final IptuSpImportService iptuSpImportService;

    public ProfileController(UsuarioService usuarioService, IptuSpImportService iptuSpImportService) {
        this.usuarioService = usuarioService;
        this.iptuSpImportService = iptuSpImportService;
    }

    /** GET /profile -> página pessoal (requer login). */
    @GetMapping("/profile")
    public String profile(Model model) {
        var perfil = usuarioService.perfilAtual();
        if (perfil.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("user", perfil.get());
        return "profile/index";
    }

    /** POST /profile -> atualiza dados pessoais (requer login). */
    @PostMapping("/profile")
    public String atualizarPerfil(@RequestParam(value = "desperson", required = false) String desperson,
                                  @RequestParam(value = "nrddd", required = false) Integer nrddd,
                                  @RequestParam(value = "nrphone", required = false) Long nrphone,
                                  @RequestParam(value = "dtbirth", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dtbirth,
                                  RedirectAttributes redirect) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            usuarioService.atualizarPerfil(auth.getName(), desperson, nrddd, nrphone, dtbirth);
            redirect.addFlashAttribute("success", "Perfil atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    /** GET /upload -> formulário de importação (requer login). */
    @GetMapping("/upload")
    public String upload(Model model) {
        return "profile/upload";
    }

    /** POST /upload -> importa CSV para iptu_sp (equivalente ao import_file.php). */
    @PostMapping("/upload")
    public String receberArquivo(@RequestParam("file") MultipartFile arquivo,
                                 RedirectAttributes redirect) {
        try {
            int inseridos = iptuSpImportService.importar(arquivo);
            redirect.addFlashAttribute("success",
                inseridos + " registro(s) importado(s) com sucesso!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "Falha ao ler o arquivo enviado!");
        }
        return "redirect:/upload";
    }
}