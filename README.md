# DisiplinPro

<p align="center">
  <img src="https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEhFgtZ6p86tm4Ks4jixEl60bZdd7-H1PGd6OOG1XCf-rhH9EqmPFxNsR8tT2YpU1a5502NVQmSjJ7z9xRZh2QhMKhKEc5Uxn2l6-jRk2lWbjp0m7D3l7lQJ6x3OSvBnJ_YjzIjeyHoABjU0ZzQaQzFt1abrEntcdkAs7sMMci3BrhlL-M59S3TFQNR2Y8Y/s320/ic_launcher-playstore.png" width="150" alt="DisiplinPro Logo">
</p>

**Sederhanakan, Atur, dan Taklukkan Hari Anda!**

**DisiplinPro** adalah aplikasi manajemen jadwal dan tugas berbasis Android yang membantu pengguna mengatur waktu, tugas, dan jadwal kuliah dengan mudah. Dibangun dengan **Jetpack Compose** dan **Firebase**, aplikasi ini menawarkan antarmuka modern, notifikasi pengingat, dan pengelolaan data yang aman.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API_21+-green.svg)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange.svg)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📋 Daftar Isi

- [Tentang Aplikasi](#ℹ️-tentang-aplikasi)
- [Fitur Utama](#✨-fitur-utama)
- [Arsitektur](#️🏗️-arsitektur)
- [Prasyarat](#📜-prasyarat)
- [Instalasi](#️🛠️-instalasi)
- [Cara Menjalankan](#🚀-cara-menjalankan)
- [Struktur Proyek](#📁-struktur-proyek)
- [Dependensi](#📦-dependensi)
- [Kontribusi](#🤝-kontribusi)
- [Masalah yang Diketahui](#🐛-masalah-yang-diketahui)
- [Rencana Pengembangan](#🔮-rencana-pengembangan)
- [Lisensi](#📄-lisensi)

---

## ℹ️ Tentang Aplikasi

DisiplinPro dirancang untuk membantu mahasiswa dan profesional mengelola waktu mereka secara efisien. Dengan fitur seperti manajemen jadwal, pengingat tugas, dan kalender interaktif, aplikasi ini memastikan pengguna tetap terorganisir dan produktif. Aplikasi ini menggunakan **Firebase Authentication** untuk keamanan pengguna dan **Firestore** untuk penyimpanan data, serta **WorkManager** untuk notifikasi latar belakang.

**Tagline**: Kendalikan tugas Anda dan capai tujuan Anda!

---

## ✨ Fitur Utama

| Fitur                    | Deskripsi                                                                                                                                               |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Manajemen Jadwal**     | Tambah, edit, atau hapus jadwal kuliah mingguan dengan notifikasi pengingat yang dapat disesuaikan (10 menit, 30 menit, 1 jam, atau 1 hari sebelumnya). |
| **Manajemen Tugas**      | Atur tugas dengan tenggat waktu, tandai sebagai selesai, dan terima notifikasi pengingat.                                                               |
| **Kalender Interaktif**  | Lihat jadwal berdasarkan tanggal dengan tampilan kalender yang intuitif.                                                                                |
| **Autentikasi Pengguna** | Registrasi, login, reset kata sandi, dan verifikasi email menggunakan Firebase Authentication.                                                          |
| **Profil Pengguna**      | Lihat dan kelola informasi pengguna seperti username dan foto profil.                                                                                   |
| **Notifikasi**           | Aktifkan/nonaktifkan notifikasi untuk jadwal dan tugas, sesuaikan waktu pengingat, dan terima notifikasi pop-up (floating).                             |
| **Notifikasi Floating**  | Semua notifikasi muncul sebagai pop-up (heads-up) sehingga tidak terlewatkan, dengan prioritas tinggi dan dukungan penuh untuk semua versi Android.     |
| **Navigasi Pintar**      | Klik notifikasi untuk langsung menuju daftar tugas atau jadwal terkait.                                                                                 |
| **Desain Modern**        | Antarmuka responsif menggunakan Jetpack Compose dengan tema yang ramah pengguna.                                                                        |

---

## 🏗️ Arsitektur

DisiplinPro menggunakan arsitektur **MVVM (Model-View-ViewModel)** untuk pemisahan tanggung jawab yang jelas:

- **Model**: Kelas data seperti `Schedule`, `Task`, dan `User`, disimpan di Firestore.
- **View**: Antarmuka pengguna dibuat dengan **Jetpack Compose** untuk tampilan deklaratif.
- **ViewModel**: Mengelola logika aplikasi dan state (contoh: `ScheduleViewModel`, `TaskViewModel`).
- **Repository**: Menangani komunikasi dengan Firebase (`AuthRepository`, `FirestoreRepository`).
- **Worker**: Menggunakan **WorkManager** (`NotificationWorker`) untuk notifikasi latar belakang.

**Stack Teknologi**:

- **Frontend**: Jetpack Compose, Coil
- **Backend**: Firebase Authentication, Firestore
- **Latar Belakang**: WorkManager, Coroutines
- **Bahasa**: Kotlin

---

## 📜 Prasyarat

Untuk menjalankan DisiplinPro, Anda memerlukan:

- **Android Studio** (versi Arctic Fox atau lebih baru)
- **Kotlin**: Versi 1.9.0 atau lebih tinggi
- **Akun Firebase**: Untuk autentikasi dan Firestore
- **Perangkat/Emulator**: Android API 21 (Lollipop) atau lebih tinggi
- **Koneksi Internet**: Untuk autentikasi dan sinkronisasi data

---

## 🛠️ Instalasi

Ikuti langkah-langkah berikut untuk mengatur proyek:

1. **Kloning Repositori**:

   ```bash
   git clone https://github.com/febss20/disiplinpro_app.git
   cd disiplinpro_app
   ```

2. **Konfigurasi Firebase**:

    - Buat proyek di [Firebase Console](https://console.firebase.google.com/)
    - Tambahkan aplikasi Android dengan package name `com.example.disiplinpro`
    - Unduh file `google-services.json` dan tempatkan di direktori `/app`
    - Aktifkan Authentication (Email dan Google Sign-In)
    - Buat database Firestore

3. **Buka dengan Android Studio**:

    - Buka Android Studio
    - Pilih "Open an Existing Project"
    - Navigasi ke direktori proyek dan buka

4. **Sinkronisasi Gradle**:
    - Tunggu hingga Android Studio menyelesaikan sinkronisasi Gradle
    - Selesaikan instalasi dependensi yang diperlukan

---

## 🚀 Cara Menjalankan

Setelah menyelesaikan instalasi, Anda dapat menjalankan aplikasi:

1. **Pilih Perangkat Target**:

    - Pilih emulator atau perangkat fisik yang terhubung
    - Pastikan API level perangkat ≥ 21 (Android 5.0)

2. **Build dan Run**:

    - Klik tombol 'Run' (▶️) di Android Studio
    - Tunggu hingga proses build selesai
    - Aplikasi akan terbuka di perangkat target

3. **Aplikasi Pertama Kali**:
    - Layar Onboarding akan muncul untuk pengguna baru
    - Daftar atau masuk untuk menggunakan fitur lengkap
    - Verifikasi email Anda untuk keamanan tambahan

---

## 📁 Struktur Proyek

DisiplinPro mengikuti struktur proyek Android standar dengan fokus pada arsitektur MVVM:

```
app/
├── data/                  # Layer data
│   ├── model/             # Model data (User, Task, Schedule)
│   └── repository/        # Repository untuk akses data
├── viewmodel/             # ViewModels untuk setiap fitur
│   ├── auth/              # Autentikasi
│   ├── home/              # Dashboard utama
│   ├── schedule/          # Manajemen jadwal
│   └── task/              # Manajemen tugas
├── ui/                    # Komponen UI
│   ├── auth/              # Layar autentikasi
│   ├── home/              # Layar beranda
│   ├── schedule/          # Layar jadwal
│   ├── task/              # Layar tugas
│   ├── calendar/          # Layar kalender
│   ├── notification/      # Layar notifikasi
│   ├── profile/           # Layar profil
│   ├── components/        # Komponen UI yang dapat digunakan kembali
│   └── theme/             # Definisi tema dan styling
├── util/                  # Utilitas dan helper functions
└── worker/                # Worker untuk proses background
```

- **app/src/main/java/com/example/disiplinpro/**

    - **data/**: Model data dan repository
    - **ui/**: Layar dan komponen UI Jetpack Compose
    - **viewmodel/**: ViewModels untuk setiap fitur
    - **worker/**: Kelas Worker untuk tugas latar belakang
    - **util/**: Utility dan extension functions

- **app/src/main/res/**
    - **drawable/**: Aset gambar dan ikon
    - **values/**: Warna, string, dan dimensi
    - **font/**: Font kustom yang digunakan

---

## 📦 Dependensi

DisiplinPro menggunakan dependensi berikut:

| Kategori                  | Dependensi                                          |
| ------------------------- | --------------------------------------------------- |
| **UI**                    | Jetpack Compose, Material3, Navigation Compose      |
| **Firebase**              | Firebase Authentication, Firestore, Cloud Messaging |
| **Image Loading**         | Coil, Landscapist                                   |
| **Background Processing** | WorkManager                                         |
| **Local Storage**         | DataStore Preferences                               |
| **Testing**               | JUnit, Mockito, Espresso                            |

Lihat `app/build.gradle.kts` untuk daftar lengkap dan versi.

---

## 🤝 Kontribusi

Kontribusi untuk DisiplinPro sangat dihargai! Berikut cara Anda dapat berkontribusi:

1. **Fork repositori**
2. **Buat branch fitur baru**:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit perubahan Anda**:
   ```bash
   git commit -m 'Menambahkan fitur amazing'
   ```
4. **Push ke branch**:
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Buka Pull Request**

**Panduan Kontribusi**:

- Ikuti konvensi penamaan Kotlin
- Tambahkan komentar untuk kode kompleks
- Tulis unit test untuk fitur baru
- Update dokumentasi jika diperlukan

---

## 🐛 Masalah yang Diketahui

Beberapa masalah yang sedang ditangani:

- Notifikasi mungkin tertunda di beberapa perangkat dengan pengoptimalan baterai yang ketat
- Tampilan kalender membutuhkan optimasi untuk perangkat dengan layar kecil
- Sinkronisasi offline belum sepenuhnya diimplementasikan
- Beberapa animasi UI dapat menyebabkan lag di perangkat low-end

Jika Anda menemukan masalah lain, silakan [buka issue](https://github.com/febss20/disiplinpro_app/issues) di repositori.

---

## 🔮 Rencana Pengembangan

Fitur yang direncanakan untuk rilis mendatang:

- **Mode Gelap**: Implementasi tema gelap lengkap
- **Widget Home Screen**: Widget untuk akses cepat ke jadwal dan tugas
- **Statistik & Grafik**: Visualisasi produktivitas dan penyelesaian tugas
- **Sinkronisasi Kalendar**: Integrasi dengan Google Calendar
- **Grup & Kolaborasi**: Berbagi jadwal dan tugas dengan teman sekelas
- **Ekspor Data**: Ekspor jadwal dan tugas ke format PDF atau CSV
- **Pengoptimalan Penggunaan Baterai**: Perbaikan pada sistem notifikasi
- **Tindakan Cepat Notifikasi**: Tombol tindakan langsung pada notifikasi untuk menyelesaikan tugas
- **Pengelompokan Notifikasi**: Mengelompokkan beberapa notifikasi untuk mengurangi gangguan

---

## 📄 Lisensi

DisiplinPro dilisensikan di bawah Lisensi MIT - lihat file [LICENSE](LICENSE) untuk detail.

```
MIT License

Copyright (c) 2025 DisiplinPro

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

© 2025 DisiplinPro. Semua hak dilindungi.
