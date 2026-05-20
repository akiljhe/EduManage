package com.edumanage.models;

public class Kelas {
    private int id;
    private String kodeKelas;
    private String waliKelas;

    public Kelas(int id, String kodeKelas, String waliKelas) {
        this.id = id;
        this.kodeKelas = kodeKelas;
        this.waliKelas = waliKelas;
    }

    public Kelas(String kodeKelas, String waliKelas) {
        this.kodeKelas = kodeKelas;
        this.waliKelas = waliKelas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKodeKelas() {
        return kodeKelas;
    }

    public void setKodeKelas(String kodeKelas) {
        this.kodeKelas = kodeKelas;
    }

    public String getWaliKelas() {
        return waliKelas;
    }

    public void setWaliKelas(String waliKelas) {
        this.waliKelas = waliKelas;
    }
}
