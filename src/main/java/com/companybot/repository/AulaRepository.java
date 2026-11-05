package com.companybot.repository;

import com.companybot.dto.Aula;

import java.util.List;

/**
 * Porta de leitura do acervo de aulas. A origem pode ser um JSON no
 * classpath, um banco de dados ou uma API externa, sem afetar as regras
 * de negócio em {@code AulaService}.
 */
public interface AulaRepository {

    List<Aula> findAll();
}
