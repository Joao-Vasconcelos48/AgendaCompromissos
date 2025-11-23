package com.agenda.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Compromisso {
    private Integer id;
    private Integer contatoId;
    private LocalDateTime dateTime;
    private String local;
    private boolean online;
    private String descricao;

    public Compromisso() {}

    public Compromisso(Integer id, Integer contatoId, LocalDateTime dateTime, String local, boolean online, String descricao) {
        this.id = id;
        this.contatoId = contatoId;
        this.dateTime = dateTime;
        this.local = local;
        this.online = online;
        this.descricao = descricao;
    }

    public Compromisso(Integer contatoId, LocalDateTime dateTime, String local, boolean online) {
        this(null, contatoId, dateTime, local, online, null);
    }

    public Compromisso(Integer contatoId, LocalDateTime dateTime, String local, boolean online, String descricao) {
        this(null, contatoId, dateTime, local, online, descricao);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getContatoId() { return contatoId; }
    public void setContatoId(Integer contatoId) { this.contatoId = contatoId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String dt = dateTime == null ? "<sem data>" : dateTime.format(f);
        String lugar = (local == null || local.isEmpty()) ? (online ? "(online)" : "(sem local)") : local + (online ? " (online)" : "");
        String desc = (descricao == null || descricao.isEmpty()) ? "" : (" — " + descricao);
        return dt + " — " + lugar + desc;
    }
}
