package com.edumanage.dao;

import com.edumanage.utils.DatabaseConfig;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class NilaiDAO {

    public Map<String, Double> getNilaiBySiswa(int siswaId) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = """
                SELECT m.nama_mapel, n.nilai
                FROM nilai n
                JOIN mapel m ON n.mapel_id = m.id
                WHERE n.siswa_id = ?
                ORDER BY m.nama_mapel
                """;

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("nama_mapel"), rs.getDouble("nilai"));
            }
        } catch (SQLException e) {
            System.err.println("[NilaiDAO] getNilaiBySiswa error: " + e.getMessage());
        }
        return result;
    }

    public boolean simpanNilai(int siswaId, int mapelId, double nilai) {
        String sql = """
                INSERT INTO nilai (siswa_id, mapel_id, nilai)
                VALUES (?, ?, ?)
                ON CONFLICT(siswa_id, mapel_id) DO UPDATE SET nilai = excluded.nilai
                """;

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.setInt(2, mapelId);
            ps.setDouble(3, nilai);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[NilaiDAO] simpanNilai error: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusNilaiBySiswa(int siswaId) {
        String sql = "DELETE FROM nilai WHERE siswa_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[NilaiDAO] hapusNilaiBySiswa error: " + e.getMessage());
            return false;
        }
    }
}
