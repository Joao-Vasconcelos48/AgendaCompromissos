package com.agenda.util;

import com.agenda.model.Contato;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvExporter {
    /**
     * Exporta contatos para um arquivo CSV com cabeçalho: id,nome,email,telefone
     */
    public static void export(List<Contato> contatos, File file) throws IOException {
        // garante que o diretório pai exista
        Path p = file.toPath();
        Path parent = p.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            // header
            w.write("id,nome,email,telefone");
            w.newLine();

            for (Contato c : contatos) {
                w.write(csvEscape(c.getId() == null ? "" : c.getId().toString()));
                w.write(',');
                w.write(csvEscape(c.getNome()));
                w.write(',');
                w.write(csvEscape(c.getEmail()));
                w.write(',');
                w.write(csvEscape(c.getTelefone()));
                w.newLine();
            }
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        if (needQuotes) return "\"" + escaped + "\"";
        return escaped;
    }
}
