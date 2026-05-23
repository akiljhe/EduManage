# 🚀 Cara Menjalankan EduManage

## Prasyarat
- Java JDK 21 (download: https://www.oracle.com/java/technologies/downloads/)
- Koneksi internet (untuk download Gradle & dependencies pertama kali)

## Langkah di VS Code (Cara Modul - Gradle)

### 1. Install Extension
- Install extension **Gradle for Java** di VS Code

### 2. Clone / Copy Project
```
Salin seluruh folder EduManage ke komputer kamu
```

### 3. Jalankan via Terminal
```bash
# Masuk ke folder project
cd EduManage

# Windows
gradlew.bat run

# Mac / Linux  
chmod +x gradlew
./gradlew run
```

### 4. Atau via VS Code Gradle Panel
- Buka panel Gradle di sidebar kiri
- Expand: app → Tasks → application → **run**
- Klik tombol ▶ run

## Login Aplikasi
```
Username : admin
Password : admin123
```

## Struktur Folder
```
EduManage/
├── app/
│   ├── build.gradle          ← Konfigurasi Gradle
│   └── src/main/java/com/edumanage/
│       ├── App.java           ← Entry point
│       ├── models/            ← 4 Pilar OOP
│       │   ├── Person.java    ← Abstract class
│       │   ├── Gradable.java  ← Interface
│       │   ├── Siswa.java     ← extends Person implements Gradable
│       │   └── Guru.java      ← extends Person
│       ├── dao/               ← Akses database SQLite
│       ├── scenes/            ← Tampilan JavaFX
│       └── utils/             ← Konfigurasi DB
├── gradle/wrapper/            ← Gradle wrapper
├── gradlew                    ← Run script (Mac/Linux)
├── gradlew.bat                ← Run script (Windows)
└── settings.gradle
```

## Catatan
- Database `edumanage.db` otomatis dibuat saat pertama dijalankan
- Data seed (contoh siswa & guru) otomatis dimasukkan
