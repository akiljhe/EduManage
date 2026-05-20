package com.edumanage.dao;

import com.edumanage.models.Siswa;
import com.edumanage.utils.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Map;

public class SiswaDAO {

    private NilaiDAO nilaiDAO = new NilaiDAO();

    public ObservableList<Siswa> getAllSiswa() {
        ObservableList<Siswa> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM siswa ORDER BY kelas, nama";

        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Siswa s = new Siswa(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("email"),
                        rs.getString("nis"),
                        rs.getString("kelas")
                );
                
                Map<String, Double> grades = nilaiDAO.getNilaiBySiswa(s.getId());
                s.setNilaiMap(grades);
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] getAllSiswa error: " + e.getMessage());
        }
        return list;
    }

    public boolean tambahSiswa(Siswa siswa) {
        String sql = "INSERT INTO siswa (nama, email, nis, kelas) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, siswa.getNama());
            ps.setString(2, siswa.getEmail());
            ps.setString(3, siswa.getNis());
            ps.setString(4, siswa.getKelas());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                siswa.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] tambahSiswa error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSiswa(Siswa siswa) {
        String sql = "UPDATE siswa SET nama=?, email=?, nis=?, kelas=? WHERE id=?";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, siswa.getNama());
            ps.setString(2, siswa.getEmail());
            ps.setString(3, siswa.getNis());
            ps.setString(4, siswa.getKelas());
            ps.setInt(5, siswa.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] updateSiswa error: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusSiswa(int id) {
        
        nilaiDAO.hapusNilaiBySiswa(id);

        String sql = "DELETE FROM siswa WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] hapusSiswa error: " + e.getMessage());
            return false;
        }
    }

    public int countSiswa() {
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM siswa")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] countSiswa error: " + e.getMessage());
        }
        return 0;
    }

    public double getRataRataKelas(String kelas) {
        
        String sql = """
                SELECT AVG(avg_nilai) FROM (
                    SELECT s.id, AVG(n.nilai) as avg_nilai
                    FROM siswa s
                    LEFT JOIN nilai n ON s.id = n.siswa_id
                    WHERE s.kelas = ?
                    GROUP BY s.id
                )
                """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, kelas);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[SiswaDAO] getRataRataKelas error: " + e.getMessage());
        }
        return 0;
    }
}
