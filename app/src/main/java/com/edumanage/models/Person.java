package com.edumanage.models;

public abstract class Person {

    private int id;
    private String nama;
    private String email;

    public Person(int id, String nama, String email) {
        this.id = id;
        this.nama = nama;
        this.email = email;
    }

    public Person(String nama, String email) {
        this.nama = nama;
        this.email = email;
    }

    public abstract String getRole();
    public abstract String getInfoLengkap();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "[" + getRole() + "] " + nama + " (" + email + ")";
    }
}
