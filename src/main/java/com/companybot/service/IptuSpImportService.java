package com.companybot.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Equivalente ao import_file.php: importa um CSV separado por ';'
 * para a tabela iptu_sp (mesma ordem de colunas do INSERT original).
 */
@Service
public class IptuSpImportService {

    private static final String INSERT =
        "INSERT INTO iptu_sp (cnpj, cod_contribuinte, cod_cidade, cod_empresa, cod_divisao, "
        + "empreendimento, bloco, cod_unidade, cod_parcela, cod_ano, tipo_de_documento) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public IptuSpImportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int importar(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo enviado!");
        }

        int inseridos = 0;

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }

                String[] csv = parseCsvLine(linha);
                if (csv.length < 11) {
                    continue;
                }

                jdbcTemplate.update(INSERT,
                    trimToNull(csv[0]),  // cnpj / cpfcnpj
                    trimToNull(csv[1]),  // cod_contribuinte
                    trimToNull(csv[2]),  // cod_cidade
                    trimToNull(csv[3]),  // cod_empresa
                    trimToNull(csv[4]),  // cod_divisao
                    trimToNull(csv[5]),  // empreendimento
                    trimToNull(csv[6]),  // bloco
                    trimToNull(csv[7]),  // cod_unidade
                    trimToNull(csv[8]),  // cod_parcela
                    trimToNull(csv[9]),  // cod_ano
                    trimToNull(csv[10])  // tipo_de_documento
                );
                inseridos++;
            }
        }

        return inseridos;
    }

    /**
     * Equivalente ao fgetcsv($file, 10000, ";") do import_file.php:
     * separa por ';' respeitando campos entre aspas duplas ("" = aspas escapadas).
     */
    private String[] parseCsvLine(String linha) {
        java.util.List<String> campos = new java.util.ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (c == ';' && !entreAspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trim = valor.trim();
        return trim.isEmpty() ? null : trim;
    }
}