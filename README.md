# 🎓 EduManage — Sistem Manajemen Sekolah

> **Proyek Akhir Lab Pemrograman Berorientasi Objek (PBO)**  
> Tema: **Pendidikan** | Platform: **JavaFX 21 + SQLite + Gradle**

---

## 📖 Deskripsi Aplikasi

**EduManage** adalah aplikasi manajemen sekolah berbasis desktop yang dibangun menggunakan **JavaFX**. Aplikasi ini dirancang untuk membantu pengelolaan data siswa, data guru, dan penilaian akademik secara efisien dengan antarmuka yang modern dan intuitif.

### Latar Belakang
Pengelolaan data sekolah secara manual (kertas/spreadsheet) rentan terhadap kesalahan, sulit dicari, dan tidak efisien. EduManage hadir sebagai solusi digital yang memudahkan admin/staf sekolah dalam mengelola data akademik secara terpusat.

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| 🔐 Login | Autentikasi pengguna sebelum masuk sistem |
| 📊 Dashboard | Statistik real-time (jumlah siswa, guru, kelas) + jam digital |
| 👨‍🎓 Manajemen Siswa | CRUD data siswa + nilai + predikat otomatis |
| 👨‍🏫 Manajemen Guru | CRUD data guru + mata pelajaran + jabatan |
| 🔍 Pencarian | Live search di tabel siswa dan guru |
| 🕐 Real-Time Clock | Background thread untuk update jam setiap detik |

---

## 🏗️ Struktur Proyek

```
EduManage/
├── app/
│   ├── build.gradle                    # Konfigurasi Gradle + JavaFX + SQLite
│   └── src/main/java/com/edumanage/
│       ├── App.java                    # Entry point (extends Application)
│       ├── models/
│       │   ├── Person.java             # Abstract class (ABSTRACTION + INHERITANCE)
│       │   ├── Gradable.java           # Interface (ABSTRACTION)
│       │   ├── Siswa.java              # Extends Person, implements Gradable
│       │   └── Guru.java              # Extends Person
│       ├── dao/
│       │   ├── SiswaDAO.java           # CRUD Siswa ke SQLite
│       │   └── GuruDAO.java            # CRUD Guru ke SQLite
│       ├── scenes/
│       │   ├── LoginScene.java         # Scene 1: Halaman Login
│       │   ├── DashboardScene.java     # Scene 2: Dashboard + navigasi
│       │   ├── SiswaScene.java         # Scene 3: Manajemen Siswa
│       │   └── GuruScene.java          # Scene 4: Manajemen Guru
│       └── utils/
│           └── DatabaseConfig.java     # Konfigurasi & inisialisasi SQLite
└── settings.gradle
```

---

## 🔧 Penerapan 4 Pilar OOP

### 1. 🔒 Encapsulation
Semua atribut di kelas `Person`, `Siswa`, dan `Guru` dideklarasikan **private** dan hanya bisa diakses melalui **getter/setter**.

```java
// Person.java
private int id;
private String nama;
private String email;

public String getNama() { return nama; }
public void setNama(String nama) { this.nama = nama; }
```

### 2. 🧬 Inheritance
Kelas `Siswa` dan `Guru` mewarisi atribut dan method dari **abstract class `Person`** menggunakan keyword `extends`.

```java
// Siswa mewarisi id, nama, email dari Person
public class Siswa extends Person implements Gradable { ... }

// Guru mewarisi id, nama, email dari Person
public class Guru extends Person { ... }
```

### 3. 💡 Abstraction
- **Abstract class `Person`**: mendefinisikan kontrak `getRole()` dan `getInfoLengkap()` yang wajib diimplementasikan subclass.
- **Interface `Gradable`**: mendefinisikan kontrak `hitungRataRata()` dan `getPredikat()`.

```java
// Person.java - abstract class
public abstract String getRole();
public abstract String getInfoLengkap();

// Gradable.java - interface
public interface Gradable {
    double hitungRataRata();
    String getPredikat();
    default boolean isLulus() { return hitungRataRata() >= 75.0; }
}
```

### 4. 🔄 Polymorphism
Method `getRole()` dan `getInfoLengkap()` menghasilkan **output berbeda** tergantung objek yang memanggilnya (Siswa atau Guru) — inilah **runtime polymorphism**.

```java
// Siswa.java
@Override
public String getRole() { return "Siswa"; }

@Override
public String getInfoLengkap() {
    return "NIS: " + nis + " | Kelas: " + kelas + " | Rata-rata: " + hitungRataRata();
}

// Guru.java — implementasi BERBEDA
@Override
public String getRole() { return "Guru"; }

@Override
public String getInfoLengkap() {
    return "NIP: " + nip + " | Mapel: " + mataPelajaran + " | Jabatan: " + jabatan;
}
```

---

## 🧵 Implementasi Thread

Background thread digunakan di **Dashboard** untuk menampilkan jam real-time tanpa memblokir UI JavaFX:

```java
// DashboardScene.java
clockThread = new Thread(() -> {
    while (running && !Thread.currentThread().isInterrupted()) {
        String now = LocalDateTime.now().format(formatter);
        // Platform.runLater() wajib untuk update UI dari non-JavaFX thread
        Platform.runLater(() -> clockLabel.setText("🕐 " + now));
        try {
            Thread.sleep(1000); // update setiap 1 detik
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }
});
clockThread.setDaemon(true); // otomatis berhenti saat app ditutup
clockThread.start();
```

---

## 🗄️ Database SQLite

Data disimpan di file lokal `edumanage.db` yang otomatis dibuat saat pertama kali dijalankan.

**Tabel:**
- `siswa` — id, nama, email, nis, kelas, nilai_matematika, nilai_ipa, nilai_bahasa_indonesia, nilai_inggris
- `guru` — id, nama, email, nip, mata_pelajaran, jabatan

---

## 🚀 Cara Menjalankan

### Prasyarat
- Java 21 (JDK)
- Gradle (atau gunakan `./gradlew`)
- Tidak perlu install JavaFX/SQLite manual (sudah di-handle Gradle)

### Langkah

```bash
# Clone repository
git clone https://github.com/USERNAME/EduManage.git
cd EduManage

# Jalankan aplikasi
./gradlew run
```

### Akun Login Default
```
Username : admin
Password : admin123
```

---

## 👥 Anggota Tim

| Nama | NIM | Tugas |
|------|-----|-------|
| [Nama 1] | [NIM] | Model (OOP) + DAO + Database |
| [Nama 2] | [NIM] | Scene Login + Dashboard + Thread |
| [Nama 3] | [NIM] | Scene Siswa + Guru + UI Design |

---

## 📚 Referensi Materi

Proyek ini menerapkan materi dari modul praktikum:
- BAB I-II: Struktur Java, Class, Object, Constructor
- BAB III: Encapsulation (Access Modifier, Getter/Setter)
- BAB IV: Inheritance (extends, super, this)
- BAB V: Abstraction (Abstract Class, Interface)
- BAB VI: Polymorphism (Method Overriding)
- BAB VII: Thread & Multithreading
- BAB VIII/IX: JavaFX (Stage, Scene, Layout, Controls, TableView)
