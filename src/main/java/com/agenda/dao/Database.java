package com.agenda.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Database {
    private static final String URL = "jdbc:sqlite:agenda.db";

    static {
        // initialize database on first load
        try {
            initDatabase();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            // ignore if not supported; connection still returned
        }
        return conn;
    }

    private static void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create table contatos
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS contatos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL,"
                    + "email TEXT,"
                    + "telefone TEXT"
                    + ");");

            // Create table compromissos (include descricao column)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS compromissos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "contato_id INTEGER NOT NULL,"
                    + "datetime TEXT,"
                    + "local TEXT,"
                    + "online INTEGER DEFAULT 0,"
                    + "descricao TEXT,"
                    + "FOREIGN KEY(contato_id) REFERENCES contatos(id) ON DELETE CASCADE"
                    + ");");

            // ensure descricao column exists for older DBs
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(compromissos)")) {
                boolean found = false;
                while (rs.next()) {
                    String name = rs.getString("name");
                    if ("descricao".equalsIgnoreCase(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    try {
                        stmt.executeUpdate("ALTER TABLE compromissos ADD COLUMN descricao TEXT");
                    } catch (SQLException ex) {
                        // ignore if cannot alter
                    }
                }
            }

            // if empty, seed with sample data
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM contatos")) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    if (cnt == 0) {
                        seed(stmt);
                    }
                }
            }
        }
    }

    private static void seed(Statement stmt) throws SQLException {
        // insert some sample contacts
        stmt.addBatch("INSERT INTO contatos (nome, email, telefone) VALUES ('Alice Silva', 'alice@example.com', '+55 11 99999-0001')");
        stmt.addBatch("INSERT INTO contatos (nome, email, telefone) VALUES ('Bruno Costa', 'bruno@example.com', '+55 21 98888-0002')");
        stmt.addBatch("INSERT INTO contatos (nome, email, telefone) VALUES ('Carla Santos', 'carla@example.com', '+55 31 97777-0003')");
        stmt.addBatch("INSERT INTO contatos (nome, email, telefone) VALUES ('Diego Almeida', 'diego@example.com', '+55 41 96666-0004')");
        stmt.addBatch("INSERT INTO contatos (nome, email, telefone) VALUES ('Elisa Pereira', 'elisa@example.com', '+55 51 95555-0005')");
        stmt.executeBatch();

        // seed some appointments (compromissos) for these contacts
        // using 'yyyy-MM-dd HH:mm:ss' format and including a short descricao
        stmt.addBatch("INSERT INTO compromissos (contato_id, datetime, local, online, descricao) VALUES (1, '2025-12-01 10:00:00', 'Sala 101', 0, 'Reunião inicial')");
        stmt.addBatch("INSERT INTO compromissos (contato_id, datetime, local, online, descricao) VALUES (2, '2025-12-02 14:30:00', 'Sala 202', 0, 'Apresentação de projeto')");
        stmt.addBatch("INSERT INTO compromissos (contato_id, datetime, local, online, descricao) VALUES (1, '2025-12-05 09:00:00', 'Zoom: https://zoom.us/j/123', 1, 'Chamada com cliente')");
        stmt.addBatch("INSERT INTO compromissos (contato_id, datetime, local, online, descricao) VALUES (3, '2025-12-10 16:00:00', '', 0, 'Check-up')");
        stmt.executeBatch();
    }
}
