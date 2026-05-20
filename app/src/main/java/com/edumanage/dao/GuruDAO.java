package com.edumanage.dao;

import com.edumanage.models.Guru;
import com.edumanage.utils.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class GuruDAO {

    public ObservableList<Guru> getAllGuru() {
        ObservableList<Guru> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM guru ORDER BY nama";

        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guru g = new Guru(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("email"),
                        rs.getString("nip"),
                        rs.getString("mata_pelajaran"),
                        rs.getString("jabatan")
                );
                list.add(g);
            }
        } catch (SQLException e) {
            System.err.println("[GuruDAO] getAllGuru error: " + e.getMessage());
        }
        return list;
    }

    public boolean tambahGuru(Guru guru) {
        String sql = "INSERT INTO guru (nama, email, nip, mata_pelajaran, jabatan) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, guru.getNama());
            ps.setString(2, guru.getEmail());
            ps.setString(3, guru.getNip());
            ps.setString(4, guru.getMataPelajaran());
            ps.setString(5, guru.getJabatan());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[GuruDAO] tambahGuru error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateGuru(Guru guru) {
        String sql = "UPDATE guru SET nama=?, email=?, nip=?, mata_pelajaran=?, jabatan=? WHERE id=?";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, guru.getNama());
            ps.setString(2, guru.getEmail());
            ps.setString(3, guru.getNip());
            ps.setString(4, guru.getMataPelajaran());
            ps.setString(5, guru.getJabatan());
            ps.setInt(6, guru.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[GuruDAO] updateGuru error: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusGuru(int id) {
        String sql = "DELETE FROM guru WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[GuruDAO] hapusGuru error: " + e.getMessage());
            return false;
        }
    }

    public int countGuru() {
        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM guru")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[GuruDAO] countGuru error: " + e.getMessage());
        }
        return 0;
    }

}
