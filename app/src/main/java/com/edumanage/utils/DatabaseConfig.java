package com.edumanage.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:edumanage.db";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createSiswaTable = """
                CREATE TABLE IF NOT EXISTS siswa (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    email TEXT,
                    nis TEXT UNIQUE NOT NULL,
                    kelas TEXT NOT NULL
                );
                """;

        String createGuruTable = """
                CREATE TABLE IF NOT EXISTS guru (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    email TEXT,
                    nip TEXT UNIQUE NOT NULL,
                    mata_pelajaran TEXT NOT NULL,
                    jabatan TEXT DEFAULT 'Guru'
                );
                """;

        String createKelasTable = """
                CREATE TABLE IF NOT EXISTS kelas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    kode_kelas TEXT UNIQUE NOT NULL,
                    wali_kelas TEXT
                );
                """;

        String createMapelTable = """
                CREATE TABLE IF NOT EXISTS mapel (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    kode_mapel TEXT UNIQUE NOT NULL,
                    nama_mapel TEXT NOT NULL
                );
                """;

        String createNilaiTable = """
                CREATE TABLE IF NOT EXISTS nilai (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    siswa_id INTEGER NOT NULL,
                    mapel_id INTEGER NOT NULL,
                    nilai REAL DEFAULT 0,
                    FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE CASCADE,
                    FOREIGN KEY (mapel_id) REFERENCES mapel(id) ON DELETE CASCADE,
                    UNIQUE(siswa_id, mapel_id)
                );
                """;

        String createKelasMapelTable = """
                CREATE TABLE IF NOT EXISTS kelas_mapel (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    kelas_id INTEGER NOT NULL,
                    mapel_id INTEGER NOT NULL,
                    FOREIGN KEY (kelas_id) REFERENCES kelas(id) ON DELETE CASCADE,
                    FOREIGN KEY (mapel_id) REFERENCES mapel(id) ON DELETE CASCADE,
                    UNIQUE(kelas_id, mapel_id)
                );
                """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createSiswaTable);
            stmt.execute(createGuruTable);
            stmt.execute(createKelasTable);
            stmt.execute(createMapelTable);
            stmt.execute(createNilaiTable);
            stmt.execute(createKelasMapelTable);
            migrateDropGradeColumns(stmt);
            System.out.println("[DB] Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
    }

    private static void migrateDropGradeColumns(Statement stmt) {
        try {
            
            var rs = stmt.executeQuery("PRAGMA table_info(siswa)");
            boolean hasOldColumns = false;
            while (rs.next()) {
                String colName = rs.getString("name");
                if (colName.equals("nilai_matematika")) {
                    hasOldColumns = true;
                    break;
                }
            }
            rs.close();
            if (hasOldColumns) {
                stmt.execute("ALTER TABLE siswa DROP COLUMN nilai_matematika");
                stmt.execute("ALTER TABLE siswa DROP COLUMN nilai_ipa");
                stmt.execute("ALTER TABLE siswa DROP COLUMN nilai_bahasa_indonesia");
                stmt.execute("ALTER TABLE siswa DROP COLUMN nilai_inggris");
                System.out.println("[DB] Migrated: removed old grade columns from siswa table.");
            }
        } catch (SQLException e) {
            
            System.out.println("[DB] Migration note: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
    }
}
