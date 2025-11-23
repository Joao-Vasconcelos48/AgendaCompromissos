package com.agenda.dao;

import com.agenda.model.Compromisso;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CompromissoDAO {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Compromisso> findAll() {
        List<Compromisso> list = new ArrayList<>();
        String sql = "SELECT id, contato_id, datetime, local, online, descricao FROM compromissos ORDER BY datetime";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Compromisso c = mapRow(rs);
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Compromisso> findByContatoId(int contatoId) {
        List<Compromisso> list = new ArrayList<>();
        String sql = "SELECT id, contato_id, datetime, local, online, descricao FROM compromissos WHERE contato_id = ? ORDER BY datetime";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Compromisso findById(int id) {
        String sql = "SELECT id, contato_id, datetime, local, online, descricao FROM compromissos WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Compromisso comp) {
        String sql = "INSERT INTO compromissos (contato_id, datetime, local, online, descricao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comp.getContatoId());
            ps.setString(2, comp.getDateTime().format(F));
            ps.setString(3, comp.getLocal());
            ps.setBoolean(4, comp.isOnline());
            ps.setString(5, comp.getDescricao());
            int affected = ps.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) comp.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Compromisso comp) {
        if (comp.getId() == null) return false;
        String sql = "UPDATE compromissos SET contato_id = ?, datetime = ?, local = ?, online = ?, descricao = ? WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comp.getContatoId());
            ps.setString(2, comp.getDateTime().format(F));
            ps.setString(3, comp.getLocal());
            ps.setBoolean(4, comp.isOnline());
            ps.setString(5, comp.getDescricao());
            ps.setInt(6, comp.getId());
            int affected = ps.executeUpdate();
            return affected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM compromissos WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Compromisso mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int contatoId = rs.getInt("contato_id");
        String dt = rs.getString("datetime");
        LocalDateTime ldt = null;
        if (dt != null && !dt.isEmpty()) ldt = LocalDateTime.parse(dt, F);
        String local = rs.getString("local");
        boolean online = rs.getBoolean("online");
        String descricao = rs.getString("descricao");
        return new Compromisso(id, contatoId, ldt, local, online, descricao);
    }
}
