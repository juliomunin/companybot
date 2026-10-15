package com.companybot.controller;

import com.companybot.dto.Aula;
import com.companybot.service.AulaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AulaController {

    private static final int LIMITE_RELACIONADAS = 3;

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    /** GET /aulas -> listagem com filtros por categoria e busca. */
    @GetMapping("/aulas")
    public String aulas(@RequestParam(value = "categoria", required = false) String categoria,
                        @RequestParam(value = "busca", required = false) String busca,
                        Model model) {
        List<Aula> aulas = aulaService.listar(categoria, busca);
        List<String> categorias = aulaService.categorias();

        model.addAttribute("aulas", aulas);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriaAtiva", categoria == null ? "" : categoria);
        model.addAttribute("busca", busca == null ? "" : busca);
        model.addAttribute("total", aulaService.listar().size());
        model.addAttribute("temFiltro", (categoria != null && !categoria.isBlank())
                || (busca != null && !busca.isBlank()));

        return "aulas/index";
    }

    /** GET /aulas/{slug} -> aula aberta, com as relacionadas no rodape. */
    @GetMapping("/aulas/{slug}")
    public String aula(@PathVariable("slug") String slug, Model model) {
        return aulaService.porSlug(slug)
                .map(aula -> {
                    model.addAttribute("aula", aula);
                    model.addAttribute("relacionadas", aulaService.relacionadas(aula, LIMITE_RELACIONADAS));
                    return "aulas/aula";
                })
                .orElse("redirect:/aulas");
    }
}