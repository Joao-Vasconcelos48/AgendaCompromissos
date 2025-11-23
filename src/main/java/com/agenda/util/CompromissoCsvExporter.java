package com.agenda.util;

import com.agenda.model.Compromisso;
import com.agenda.model.Contato;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompromissoCsvExporter {
    public static void export(List<Compromisso> compromissos, List<Contato> contatos, File file) throws IOException {
        Path p = file.toPath();
        Path parent = p.getParent();
        if (parent != null) Files.createDirectories(parent);

        Map<Integer, String> nomeMap = new HashMap<>();
        if (contatos != null) {
            for (Contato c : contatos) {
                if (c.getId() != null) nomeMap.put(c.getId(), c.getNome());
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            w.write("id,contato_id,contato_nome,datetime,local,online,descricao");
            w.newLine();
            for (Compromisso c : compromissos) {
                String nome = nomeMap.getOrDefault(c.getContatoId(), "");
                w.write(csvEscape(c.getId() == null ? "" : c.getId().toString()));
                w.write(',');
                w.write(csvEscape(c.getContatoId() == null ? "" : c.getContatoId().toString()));
                w.write(',');
                w.write(csvEscape(nome));
                w.write(',');
                w.write(csvEscape(c.getDateTime() == null ? "" : c.getDateTime().toString()));
                w.write(',');
                w.write(csvEscape(c.getLocal()));
                w.write(',');
                w.write(csvEscape(Boolean.toString(c.isOnline())));
                w.write(',');
                w.write(csvEscape(c.getDescricao()));
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
