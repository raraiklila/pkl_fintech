# 🚀 PKL Fintech / Qigo Finance App

Sistem Aplikasi Keuangan dan Investasi Emas Digital Berbasis Android & Web Admin.

---

## 📌 Deskripsi Singkat

Aplikasi ini merupakan sistem finansial terpadu yang memfasilitasi:
1. **Pembayaran Digital (QRIS):** Menerima dan memproses transaksi pembayaran digital usaha/merchant via QRIS secara *real-time*.
2. **Investasi & Cicilan Emas:** Memfasilitasi transaksi jual-beli emas digital, pemantauan grafik harga, hingga program cicilan emas.
3. **Manajemen Saldo & Laporan:** Mengelola pencairan/penarikan saldo ke rekening bank serta menyajikan laporan riwayat keuangan lengkap.

---

## 🏗️ Struktur Arsitektur Sistem

* **Backend API (`/backend`)**: Node.js (Express), Supabase Database, dan Integrasi Payment Gateway (Midtrans).
* **Web Admin (`/fintech-admin`)**: Dashboard Admin berbasis React, Vite, dan disajikan via Nginx.
* **Mobile App (`/app`)**: Aplikasi Android Native yang dibangun menggunakan Kotlin & Jetpack Compose.

---

## 🐳 Cara Menjalankan Aplikasi Menggunakan Docker

Proyek ini telah dilengkapi dengan infrastruktur **Docker & Docker Compose** untuk memudahkan pengujian Backend API dan Web Admin secara instan tanpa perlu setup environment secara manual.

### 1. Prasyarat (Prerequisites)
Pastikan di komputer kamu sudah terinstall:
* [Git](https://git-scm.com/)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) (yang sudah berjalan)

### 2. Langkah-Langkah Running

1. **Clone Repository ini:**
   ```bash
   git clone https://github.com/raraiklila/pkl_fintech.git
   cd pkl_fintech
   ```

2. **Jalankan Docker Compose:**
   Jalankan perintah berikut di terminal / command prompt:
   ```bash
   docker compose up -d --build
   ```

3. **Akses Layanan:**
   Setelah proses build selesai, layanan dapat diakses melalui browser:
   * 🖥️ **Web Admin Dashboard:** `http://localhost:8080`
   * ⚙️ **Backend API Service:** `http://localhost:3000`

---

## 🌐 Akses dari Jaringan Lokal (Wi-Fi / Perangkat Lain)

Untuk mengakses Web Admin atau Backend dari HP Android atau laptop lain yang berada di **satu jaringan Wi-Fi yang sama**:

1. Cek IP Lokal komputer host (misal via `ipconfig` di Windows).
2. Gunakan IP tersebut untuk mengakses layanan:
   * **Web Admin di Laptop Lain:** `http://<IP-LOKAL-KAMU>:8080` (Contoh: `http://10.0.99.12:8080`)
   * **Base URL API di App Android:** `http://<IP-LOKAL-KAMU>:3000` (Contoh: `http://10.0.99.12:3000`)

---

## 📱 Menjalankan Aplikasi Android (`/app`)

1. Buka folder proyek ini di **Android Studio**.
2. Pastikan `BASE_URL` pada konfigurasi Retrofit/API Client sudah mengarah ke IP komputer yang menjalankan Docker Backend (`http://<IP-LOKAL-KAMU>:3000/`).
3. Hubungkan HP Android atau jalankan Emulator.
4. Klik tombol **Run / Install Debug** (`Shift + F10` atau `.\gradlew.bat installDebug`).

---

## 🛠️ Perintah Perawatan Docker (Useful Commands)

* **Melihat status container:**
  ```bash
  docker compose ps
  ```
* **Melihat log container secara langsung:**
  ```bash
  docker compose logs -f
  ```
* **Memberhentikan semua container:**
  ```bash
  docker compose down
  ```
