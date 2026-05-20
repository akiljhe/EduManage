package com.edumanage.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Siswa extends Person implements Gradable {

    private String nis;
    private String kelas;
    
    private Map<String, Double> nilaiMap = new LinkedHashMap<>();

    public Siswa(int id, String nama, String email, String nis, String kelas) {
        super(id, nama, email);
        this.nis = nis;
        this.kelas = kelas;
    }

    public Siswa(String nama, String email, String nis, String kelas) {
        super(nama, email);
        this.nis = nis;
        this.kelas = kelas;
    }

    @Override
    public String getRole() {
        return "Siswa";
    }

    @Override
    public String getInfoLengkap() {
        return String.format("NIS: %s | Kelas: %s | Rata-rata: %.1f | Predikat: %s",
                nis, kelas, hitungRataRata(), getPredikat());
    }

    @Override
    public double hitungRataRata() {
        if (nilaiMap.isEmpty()) return 0;
        double total = 0;
        for (double v : nilaiMap.values()) {
            total += v;
        }
        return total / nilaiMap.size();
    }

    @Override
    public String getPredikat() {
        double rata = hitungRataRata();
        if (rata >= 90) return "A - Sangat Baik";
        else if (rata >= 80) return "B - Baik";
        else if (rata >= 70) return "C - Cukup";
        else if (rata >= 60) return "D - Kurang";
        else return "E - Sangat Kurang";
    }

    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }

    public String getKelas() { return kelas; }
    public void setKelas(String kelas) { this.kelas = kelas; }

    public Map<String, Double> getNilaiMap() { return nilaiMap; }
    public void setNilaiMap(Map<String, Double> nilaiMap) { this.nilaiMap = nilaiMap; }

    public void setNilai(String namaMapel, double nilai) {
        this.nilaiMap.put(namaMapel, nilai);
    }

    public double getNilai(String namaMapel) {
        return nilaiMap.getOrDefault(namaMapel, 0.0);
    }
}
