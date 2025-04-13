# DisiplinPro

![DisiplinPro Banner](https://via.placeholder.com/1200x300.png?text=DisiplinPro+-+Atur+Hari+Anda)  
**Sederhanakan, Atur, dan Taklukkan Hari Anda!**

**DisiplinPro** adalah aplikasi manajemen jadwal dan tugas berbasis Android yang membantu pengguna mengatur waktu, tugas, dan jadwal kuliah dengan mudah. Dibangun dengan **Jetpack Compose** dan **Firebase**, aplikasi ini menawarkan antarmuka modern, notifikasi pengingat, dan pengelolaan data yang aman.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API_21+-green.svg)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange.svg)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📋 Daftar Isi
- [Tentang Aplikasi](#tentang-aplikasi)
- [Fitur Utama](#fitur-utama)
- [Arsitektur](#arsitektur)
- [Prasyarat](#prasyarat)
- [Instalasi](#instalasi)
- [Cara Menjalankan](#cara-menjalankan)
- [Struktur Proyek](#struktur-proyek)
- [Dependensi](#dependensi)
- [Kontribusi](#kontribusi)
- [Masalah yang Diketahui](#masalah-yang-diketahui)
- [Rencana Pengembangan](#rencana-pengembangan)
- [Lisensi](#lisensi)

---

## ℹ️ Tentang Aplikasi

DisiplinPro dirancang untuk membantu mahasiswa dan profesional mengelola waktu mereka secara efisien. Dengan fitur seperti manajemen jadwal, pengingat tugas, dan kalender interaktif, aplikasi ini memastikan pengguna tetap terorganisir dan produktif. Aplikasi ini menggunakan **Firebase Authentication** untuk keamanan pengguna dan **Firestore** untuk penyimpanan data, serta **WorkManager** untuk notifikasi latar belakang.

**Tagline**: Kendalikan tugas Anda dan capai tujuan Anda!

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| **Manajemen Jadwal** | Tambah, edit, atau hapus jadwal kuliah mingguan dengan notifikasi pengingat yang dapat disesuaikan (10 menit, 30 menit, 1 jam, atau 1 hari sebelumnya). |
| **Manajemen Tugas** | Atur tugas dengan tenggat waktu, tandai sebagai selesai, dan terima notifikasi pengingat. |
| **Kalender Interaktif** | Lihat jadwal berdasarkan tanggal dengan tampilan kalender yang intuitif. |
| **Autentikasi Pengguna** | Registrasi, login, reset kata sandi, dan verifikasi email menggunakan Firebase Authentication. |
| **Profil Pengguna** | Lihat dan kelola informasi pengguna seperti username dan foto profil. |
| **Notifikasi** | Aktifkan/nonaktifkan notifikasi untuk jadwal dan tugas, serta sesuaikan waktu pengingat. |
| **Desain Modern** | Antarmuka responsif menggunakan Jetpack Compose dengan tema yang ramah pengguna. |

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