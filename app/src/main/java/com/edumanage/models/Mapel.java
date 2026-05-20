package com.edumanage.models;

public class Mapel {
    private int id;
    private String kodeMapel;
    private String namaMapel;

    public Mapel(int id, String kodeMapel, String namaMapel) {
        this.id = id;
        this.kodeMapel = kodeMapel;
        this.namaMapel = namaMapel;
    }

    public Mapel(String kodeMapel, String namaMapel) {
        this.kodeMapel = kodeMapel;
        this.namaMapel = namaMapel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKodeMapel() { return kodeMapel; }
    public void setKodeMapel(String kodeMapel) { this.kodeMapel = kodeMapel; }

    public String getNamaMapel() { return namaMapel; }
    public void setNamaMapel(String namaMapel) { this.namaMapel = namaMapel; }

    @Override
    public String toString() {
        return namaMapel;
    }
}
