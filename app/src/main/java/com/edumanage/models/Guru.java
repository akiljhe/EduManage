package com.edumanage.models;

public class Guru extends Person {

    private String nip;
    private String mataPelajaran;
    private String jabatan;

    public Guru(int id, String nama, String email, String nip,
                String mataPelajaran, String jabatan) {
        super(id, nama, email);
        this.nip = nip;
        this.mataPelajaran = mataPelajaran;
        this.jabatan = jabatan;
    }

    public Guru(String nama, String email, String nip,
                String mataPelajaran, String jabatan) {
        super(nama, email);
        this.nip = nip;
        this.mataPelajaran = mataPelajaran;
        this.jabatan = jabatan;
    }

    @Override
    public String getRole() {
        return "Guru";
    }

    @Override
    public String getInfoLengkap() {
        return String.format("NIP: %s | Mapel: %s | Jabatan: %s",
                nip, mataPelajaran, jabatan);
    }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getMataPelajaran() { return mataPelajaran; }
    public void setMataPelajaran(String mp) { this.mataPelajaran = mp; }

    public String getJabatan() { return jabatan; }
    public void setJabatan(String jabatan) { this.jabatan = jabatan; }
}
