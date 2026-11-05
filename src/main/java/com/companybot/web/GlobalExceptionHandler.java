package com.companybot.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Última barreira contra erros não tratados: registra o problema e
 * mostra uma página amigável em vez do stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView tratarRecursoNaoEncontrado(NoResourceFoundException excecao, HttpServletRequest request) {
        return paginaDeErro(HttpStatus.NOT_FOUND, "Página não encontrada",
                "O endereço que você tentou acessar não existe.", request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView tratarErro(Exception excecao, HttpServletRequest request) {
        log.error("Erro não tratado em {} {}", request.getMethod(), request.getRequestURI(), excecao);
        return paginaDeErro(HttpStatus.INTERNAL_SERVER_ERROR, "Algo deu errado",
                "Não foi possível concluir a operação. Tente novamente em instantes.", request);
    }

    private ModelAndView paginaDeErro(HttpStatus status, String titulo, String mensagem,
                                      HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", status.value());
        mav.addObject("titulo", titulo);
        mav.addObject("mensagem", mensagem);
        mav.addObject("caminho", request.getRequestURI());
        return mav;
    }
}
