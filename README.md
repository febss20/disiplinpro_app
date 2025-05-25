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

- [Tentang Aplikasi](#tentang-aplikasi)
- [Fitur Utama](#fitur-utama)
- [Arsitektur](#arsitektur)
- [Teknologi Stack](#teknologi-stack)
- [Prasyarat](#prasyarat)
- [Instalasi](#instalasi)
- [Cara Menjalankan](#cara-menjalankan)
- [Dependensi](#dependensi)
- [Masalah yang Diketahui](#masalah-yang-diketahui)
- [Lisensi](#lisensi)
- [Kontak](#kontak)

---

## Tentang Aplikasi

DisiplinPro dirancang untuk membantu mahasiswa dan profesional mengelola waktu mereka secara efisien. Dengan fitur seperti manajemen jadwal, pengingat tugas, dan kalender interaktif, aplikasi ini memastikan pengguna tetap terorganisir dan produktif. Aplikasi ini menggunakan **Firebase Authentication** untuk keamanan pengguna dan **Firestore** untuk penyimpanan data, serta **WorkManager** untuk notifikasi latar belakang.

**Tagline**: Kendalikan tugas Anda dan capai tujuan Anda!

---

## Fitur Utama

| Fitur                       | Deskripsi                                                                                                                                               |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Manajemen Jadwal**        | Tambah, edit, atau hapus jadwal kuliah mingguan dengan notifikasi pengingat yang dapat disesuaikan (10 menit, 30 menit, 1 jam, atau 1 hari sebelumnya). |
| **Manajemen Tugas**         | Atur tugas dengan tenggat waktu, tandai sebagai selesai, dan terima notifikasi pengingat.                                                               |
| **Kalender Interaktif**     | Lihat jadwal berdasarkan tanggal dengan tampilan kalender yang intuitif.                                                                                |
| **Autentikasi Pengguna**    | Registrasi, login, reset kata sandi, dan verifikasi email menggunakan Firebase Authentication.                                                          |
| **Profil Pengguna**         | Lihat dan kelola informasi pengguna seperti username dan foto profil.                                                                                   |
| **Notifikasi**              | Aktifkan/nonaktifkan notifikasi untuk jadwal dan tugas, sesuaikan waktu pengingat, dan terima notifikasi pop-up (floating).                             |
| **Notifikasi Floating**     | Semua notifikasi muncul sebagai pop-up (heads-up) sehingga tidak terlewatkan, dengan prioritas tinggi dan dukungan penuh untuk semua versi Android.     |
| **Navigasi Pintar**         | Klik notifikasi untuk langsung menuju daftar tugas atau jadwal terkait.                                                                                 |
| **Desain Modern**           | Antarmuka responsif menggunakan Jetpack Compose dengan tema yang ramah pengguna.                                                                        |
| **Sinkronisasi Cloud**      | Data disimpan di cloud, memungkinkan akses dari berbagai perangkat dan pencadangan otomatis.                                                            |
| **Mode Offline**            | Tetap dapat mengakses jadwal dan tugas meskipun tidak ada koneksi internet.                                                                             |
| **Kategori & Label**        | Organisasikan tugas dan jadwal dengan kategori dan label yang dapat disesuaikan.                                                                        |
| **Prioritas Tugas**         | Tetapkan prioritas ke tugas untuk fokus pada yang paling penting.                                                                                       |
| **Widget Home Screen**      | Akses cepat ke jadwal dan tugas mendatang langsung dari layar beranda perangkat.                                                                        |

---

## Arsitektur

DisiplinPro menggunakan arsitektur **MVVM (Model-View-ViewModel)** untuk pemisahan tanggung jawab yang jelas dan diimplementasikan dengan **Clean Architecture** untuk memastikan ketahanan dan skalabilitas kode:

### Diagram Arsitektur

```
┌─────────────────┐     ┌────────────────┐     ┌────────────────┐
│      View       │     │   ViewModel    │     │    Repository  │
│  (Compose UI)   │◄────┤(Business Logic)│◄────┤  (Data Access) │
└────────┬────────┘     └────────────────┘     └───────┬────────┘
         │                                             │
         │                                             │
         │                                     ┌───────▼────────┐
         │                                     │  Data Sources  │
         └────────────────────────────────────►│ Firebase, Local│
                      User Events              └────────────────┘
```

### Lapisan Utama:

- **Presentation Layer (UI)**:

    - **Jetpack Compose**: UI deklaratif dengan komponen yang dapat digunakan kembali
    - **State Hoisting**: Memisahkan UI dari logika bisnis
    - **Navigation Component**: Untuk navigasi antar layar yang mulus

- **Domain Layer (Business Logic)**:

    - **ViewModels**: Mengelola state UI dan logika bisnis
    - **Use Cases**: Tindakan bisnis tertentu, seperti mengambil tugas atau menambahkan jadwal
    - **State Management**: Menggunakan `StateFlow` dan `Flow` untuk reaktivitas

- **Data Layer**:
    - **Repositories**: Abstraksi untuk akses data
    - **Data Sources**: Firebase Firestore, DataStore, WorkManager
    - **Models**: Entitas data seperti User, Task, Schedule

---

## Teknologi Stack

DisiplinPro dibangun menggunakan berbagai teknologi modern:

### Frontend

| Teknologi              | Deskripsi                                                 |
| ---------------------- | --------------------------------------------------------- |
| **Jetpack Compose**    | Framework UI deklaratif untuk antarmuka pengguna modern   |
| **Material3**          | Sistem desain dengan komponen visual yang konsisten       |
| **Navigation Compose** | Navigasi antar layar yang mulus dan terintegrasi          |
| **Coil**               | Library untuk memuat dan caching gambar                   |
| **Accompanist**        | Library pendamping untuk Compose dengan komponen tambahan |

### Backend

| Teknologi                    | Deskripsi                                                                                                |
| ---------------------------- | -------------------------------------------------------------------------------------------------------- |
| **Firebase Authentication**  | Sistem autentikasi pengguna yang aman dan scalable dengan dukungan email dan reset password              |
| **Firebase Firestore**       | Database NoSQL berbasis cloud untuk penyimpanan data tugas, jadwal, dan informasi pengguna               |
| **AWS S3**                   | Layanan penyimpanan objek untuk menyimpan foto profil pengguna dengan URL presigned untuk akses aman     |
| **WorkManager**              | API penjadwalan tugas latar belakang untuk mengirim notifikasi lokal tepat waktu meskipun aplikasi tutup |

### State Management

| Teknologi             | Deskripsi                                                  |
| --------------------- | ---------------------------------------------------------- |
| **Kotlin Coroutines** | Pemrograman asinkron yang lebih mudah dan efisien          |
| **Flow**              | Aliran data reaktif untuk pemrosesan nilai asinkron        |
| **StateFlow**         | Pengelolaan state reaktif dalam ViewModel                  |
| **LiveData**          | Pengelolaan data berbasis observer yang sadar siklus hidup |

### Persistensi & Data

| Teknologi                 | Deskripsi                                            |
| ------------------------- | ---------------------------------------------------- |
| **DataStore Preferences** | Penyimpanan preferensi pengguna yang modern dan aman |
| **Room Database**         | Library abstraksi SQLite untuk caching dan offline   |
| **Kotlin Serialization**  | Serialisasi dan deserialisasi data dengan Kotlin     |

### Tools & Utilitas

| Teknologi            | Deskripsi                                                   |
| -------------------- | ----------------------------------------------------------- |
| **Hilt**             | Framework injeksi dependensi berbasis Dagger                |
| **WorkManager**      | API untuk menjalankan tugas latar belakang yang dijadwalkan |
| **Timber**           | Utilitas logging yang fleksibel                             |
| **kotlinx-datetime** | Library untuk manipulasi tanggal dan waktu di Kotlin        |

### Testing

| Teknologi    | Deskripsi                            |
| ------------ | ------------------------------------ |
| **JUnit**    | Framework testing unit standar       |
| **Espresso** | Framework testing UI untuk Android   |
| **Mockito**  | Framework mocking untuk testing unit |
| **Truth**    | Library asertif fluent dari Google   |

---

## Prasyarat

Untuk menjalankan DisiplinPro, Anda memerlukan:

- **Android Studio** (versi Arctic Fox atau lebih baru)
- **Kotlin**: Versi 1.9.0 atau lebih tinggi
- **Akun Firebase**: Untuk autentikasi dan Firestore
- **Perangkat/Emulator**: Android API 21 (Lollipop) atau lebih tinggi
- **Koneksi Internet**: Untuk autentikasi dan sinkronisasi data
- **JDK**: Versi 11 atau lebih tinggi
- **Git**: Untuk cloning repositori

---

## Instalasi

Ikuti langkah-langkah berikut untuk mengatur proyek:

1. **Kloning Repositori**:

   ```bash
   git clone https://github.com/febss20/disiplinpro_app.git
   cd disiplinpro_app
   ```

2. **Konfigurasi Firebase**:

    - Buat proyek di [Firebase Console](https://console.firebase.google.com/)
    - Tambahkan aplikasi Android dengan package name `com.dsp.disiplinpro`
    - Unduh file `google-services.json` dan tempatkan di direktori `/app`
    - Aktifkan Authentication (Email dan Google Sign-In)
    - Buat database Firestore dengan aturan keamanan berikut:

      ```
      rules_version = '2';
      service cloud.firestore {
        match /databases/{database}/documents {
          match /users/{userId} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
          match /users/{userId}/tasks/{taskId} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
          match /users/{userId}/schedules/{scheduleId} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
        }
      }
      ```

3. **Konfigurasi AWS S3**:

    - Buat bucket di Amazon S3 untuk penyimpanan foto profil
    - Konfigurasikan IAM dengan akses terbatas ke bucket
    - Update kredensial di S3Repository dengan nilai yang sesuai

4. **Buka dengan Android Studio**:

    - Buka Android Studio
    - Pilih "Open an Existing Project"
    - Navigasi ke direktori proyek dan buka

5. **Sinkronisasi Gradle**:

    - Tunggu hingga Android Studio menyelesaikan sinkronisasi Gradle
    - Selesaikan instalasi dependensi yang diperlukan

6. **Konfigurasi Build Variants**:
    - Pilih variant "debug" untuk pengembangan
    - Atau "release" untuk build produksi

---

## Cara Menjalankan

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

4. **Testing Notifikasi**:
    - Buat jadwal atau tugas dengan pengingat untuk menguji fitur notifikasi
    - Notifikasi akan muncul pada waktu yang ditentukan

---

## Dependensi

DisiplinPro menggunakan dependensi berikut:

| Kategori                 | Dependensi                        | Versi  |
| ------------------------ | --------------------------------- | ------ |
| **Kotlin**               | Kotlin                            | 1.9.0  |
| **Coroutines**           | kotlinx-coroutines-core           | 1.7.3  |
| **Jetpack Compose**      | compose-ui, compose-material3     | 1.5.0  |
| **Navigation**           | navigation-compose                | 2.7.0  |
| **Lifecycle**            | lifecycle-viewmodel-compose       | 2.6.1  |
| **Firebase**             | firebase-auth, firebase-firestore | 32.2.0 |
| **WorkManager**          | work-runtime-ktx                  | 2.8.1  |
| **Dependency Injection** | hilt-android                      | 2.47   |
| **Image Loading**        | coil-compose                      | 2.4.0  |
| **Local Storage**        | datastore-preferences             | 1.0.0  |
| **Date/Time**            | kotlinx-datetime                  | 0.4.0  |
| **Testing**              | junit, espresso-core, truth       | -      |

Untuk daftar lengkap, lihat `app/build.gradle.kts`.

---

## Masalah yang Diketahui

Beberapa masalah yang sedang ditangani:

- Notifikasi mungkin tertunda di beberapa perangkat dengan pengoptimalan baterai yang ketat
- Tampilan kalender membutuhkan optimasi untuk perangkat dengan layar kecil
- Sinkronisasi offline belum sepenuhnya diimplementasikan
- Beberapa animasi UI dapat menyebabkan lag di perangkat low-end
- Fitur widget masih dalam tahap pengembangan
- Integrasi Google Calendar belum sempurna untuk semua jenis perangkat

Jika Anda menemukan masalah lain, silakan [buka issue](https://github.com/febss20/disiplinpro_app/issues) di repositori.

---

## Lisensi

DisiplinPro dilisensikan di bawah Lisensi MIT - lihat file [LICENSE](LICENSE) untuk detail.

---

## Kontak

- **Developer**: Febs - [GitHub](https://github.com/febss20)
- **Email**: alif.23131@mhs.unesa.ac.id

---

<p align="center">
  <b>DisiplinPro</b> - Dibangun dengan ❤️ menggunakan Kotlin dan Jetpack Compose
</p>
