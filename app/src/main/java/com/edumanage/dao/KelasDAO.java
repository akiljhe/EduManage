package com.edumanage.dao;

import com.edumanage.models.Kelas;
import com.edumanage.utils.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class KelasDAO {

    public ObservableList<Kelas> getAllKelas() {
        ObservableList<Kelas> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM kelas ORDER BY kode_kelas";

        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Kelas k = new Kelas(
                        rs.getInt("id"),
                        rs.getString("kode_kelas"),
                        rs.getString("wali_kelas")
                );
                list.add(k);
            }
        } catch (SQLException e) {
            System.err.println("[KelasDAO] getAllKelas error: " + e.getMessage());
        }
        return list;
    }

    public boolean tambahKelas(Kelas kelas) {
        String sql = "INSERT INTO kelas(kode_kelas, wali_kelas) VALUES(?, ?)";
        try (PreparedStatement pstmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, kelas.getKodeKelas());
            pstmt.setString(2, kelas.getWaliKelas());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[KelasDAO] tambahKelas error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateKelas(Kelas kelas) {
        String sql = "UPDATE kelas SET kode_kelas = ?, wali_kelas = ? WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, kelas.getKodeKelas());
            pstmt.setString(2, kelas.getWaliKelas());
            pstmt.setInt(3, kelas.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[KelasDAO] updateKelas error: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusKelas(int id) {
        String sql = "DELETE FROM kelas WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[KelasDAO] hapusKelas error: " + e.getMessage());
            return false;
        }
    }
}
