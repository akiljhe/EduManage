package com.edumanage.dao;

import com.edumanage.models.Mapel;
import com.edumanage.utils.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class MapelDAO {

    public ObservableList<Mapel> getAllMapel() {
        ObservableList<Mapel> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM mapel ORDER BY nama_mapel";

        try (Statement stmt = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Mapel m = new Mapel(
                        rs.getInt("id"),
                        rs.getString("kode_mapel"),
                        rs.getString("nama_mapel")
                );
                list.add(m);
            }
        } catch (SQLException e) {
            System.err.println("[MapelDAO] getAllMapel error: " + e.getMessage());
        }
        return list;
    }

    public ObservableList<Mapel> getMapelByKelas(String kodeKelas) {
        ObservableList<Mapel> list = FXCollections.observableArrayList();
        String sql = """
                SELECT m.* FROM mapel m
                JOIN kelas_mapel km ON m.id = km.mapel_id
                JOIN kelas k ON k.id = km.kelas_id
                WHERE k.kode_kelas = ?
                ORDER BY m.nama_mapel
                """;

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, kodeKelas);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Mapel(
                        rs.getInt("id"),
                        rs.getString("kode_mapel"),
                        rs.getString("nama_mapel")
                ));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[MapelDAO] getMapelByKelas error: " + e.getMessage());
        }
        return list;
    }

    public boolean tambahMapel(Mapel mapel) {
        String sql = "INSERT INTO mapel (kode_mapel, nama_mapel) VALUES (?, ?)";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, mapel.getKodeMapel());
            ps.setString(2, mapel.getNamaMapel());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[MapelDAO] tambahMapel error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMapel(Mapel mapel) {
        String sql = "UPDATE mapel SET kode_mapel=?, nama_mapel=? WHERE id=?";

        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, mapel.getKodeMapel());
            ps.setString(2, mapel.getNamaMapel());
            ps.setInt(3, mapel.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[MapelDAO] updateMapel error: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusMapel(int id) {
        String sql = "DELETE FROM mapel WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[MapelDAO] hapusMapel error: " + e.getMessage());
            return false;
        }
    }
}
