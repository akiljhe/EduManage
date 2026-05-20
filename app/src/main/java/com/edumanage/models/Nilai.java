package com.edumanage.models;

public class Nilai {
    private int id;
    private int siswaId;
    private int mapelId;
    private String namaMapel;
    private double nilai;

    public Nilai(int id, int siswaId, int mapelId, String namaMapel, double nilai) {
        this.id = id;
        this.siswaId = siswaId;
        this.mapelId = mapelId;
        this.namaMapel = namaMapel;
        this.nilai = nilai;
    }

    public Nilai(int siswaId, int mapelId, double nilai) {
        this.siswaId = siswaId;
        this.mapelId = mapelId;
        this.nilai = nilai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSiswaId() { return siswaId; }
    public void setSiswaId(int siswaId) { this.siswaId = siswaId; }

    public int getMapelId() { return mapelId; }
    public void setMapelId(int mapelId) { this.mapelId = mapelId; }

    public String getNamaMapel() { return namaMapel; }
    public void setNamaMapel(String namaMapel) { this.namaMapel = namaMapel; }

    public double getNilai() { return nilai; }
    public void setNilai(double nilai) { this.nilai = nilai; }
}
