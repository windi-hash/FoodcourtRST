# Foodcourt RST — Aplikasi Android (Kas Kantin)

## Cara paling gampang: biar GitHub yang build-in APK-nya (tanpa Android Studio)

Project ini sudah dilengkapi **GitHub Actions** — semacam "robot" yang
otomatis meng-compile APK di server GitHub begitu kamu upload kodenya.
Kamu tidak perlu install Android Studio sama sekali untuk cara ini.

1. Buat akun **github.com** kalau belum punya (gratis)
2. Klik **New repository** (bisa Public atau Private, sama-sama gratis),
   kasih nama bebas misalnya `foodcourt-rst-android`, klik **Create repository**
3. Di halaman repo yang kosong itu, klik **uploading an existing file**
4. **Extract dulu** zip project ini di komputer kamu, lalu **drag & drop
   seluruh isi folder `FoodcourtRST`** (bukan folder-nya, tapi isinya —
   `app`, `gradle`, `.github`, `build.gradle.kts`, dst) ke halaman upload
   itu, lalu klik **Commit changes**
5. Klik tab **Actions** di bagian atas repo — akan ada proses "Build APK"
   yang otomatis jalan (ikon kuning berputar = sedang proses, biasanya
   3-8 menit)
6. Setelah selesai (ikon jadi centang hijau ✅), klik run tersebut, lalu
   scroll ke bawah ke bagian **Artifacts**, klik **app-debug-apk** untuk
   download (isinya file `.apk` di dalam zip kecil)
7. Extract, kirim `.apk`-nya ke HP Android, install seperti biasa

Kalau prosesnya gagal (ikon merah ❌), klik run itu untuk lihat log
error-nya, lalu screenshot/copy-paste ke saya — saya bisa perbaiki file
workflow-nya tanpa kamu perlu paham isinya.

> Catatan: cara ini menghasilkan APK **debug** (untuk testing/pemakaian
> sendiri), sama seperti yang dihasilkan Android Studio di langkah manual
> di bawah. Untuk publish ke Play Store, tetap perlu langkah "Generate
> Signed Bundle" di Android Studio (lihat bagian Play Console di bawah),
> karena proses tanda tangan (keystore) sengaja tidak diotomatisasi di
> sini — itu kunci pribadi yang harus tetap di tangan kamu saja.

---

## Cara manual lewat Android Studio (kalau lebih suka begini)

Ini adalah project **Android Studio** yang membungkus web app Google Apps Script
kamu ("Foodcourt RST — Kas Kantin") menjadi aplikasi Android asli: ikon sendiri,
tanpa address bar, splash sesuai tema merah brand, tombol back berfungsi,
mendukung download file, dan ada halaman "tidak ada koneksi" saat offline.

Web app aslinya (Google Sheets + Apps Script) **tidak diubah sama sekali** —
aplikasi ini hanya membuka:

```
https://script.google.com/macros/s/AKfycbxJL3HyDAqEDZSIvtrh_G3Tc2_Gcib5aymBLuoO-Lg7zguwFvShToDYxtWeGVWygwnXwA/exec
```

di dalam WebView native.

## Cara build APK

1. Install **Android Studio** (gratis, dari developer.android.com) kalau belum ada.
2. Buka Android Studio → **Open** → pilih folder `FoodcourtRST` ini.
3. Tunggu proses **Gradle Sync** selesai (butuh koneksi internet, otomatis
   mengunduh Gradle & library Android yang diperlukan).
   - Kalau muncul peringatan "Gradle wrapper is missing/invalid", klik
     tombol yang ditawarkan Android Studio untuk **membuatkan ulang wrapper**
     (ini normal — file biner `gradle-wrapper.jar` sengaja tidak disertakan
     karena harus diunduh dari internet, bukan sesuatu yang saya rakit sendiri).
4. Setelah sync selesai, klik menu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. APK hasil build ada di `app/build/outputs/apk/debug/app-debug.apk` — file
   ini sudah bisa langsung di-install ke HP Android (aktifkan dulu
   "Install dari sumber tidak dikenal" di HP kalau diminta).

Untuk publish ke Play Store atau bagikan versi "release" yang lebih optimal,
pilih **Build → Generate Signed Bundle / APK** dan ikuti wizard untuk membuat
keystore sendiri (jangan bagikan keystore ini ke siapa pun).

## Kalau link Apps Script berubah

Kalau kamu suatu saat re-deploy Apps Script dan dapat URL `/exec` baru, cukup
ganti satu baris di:

```
app/src/main/java/com/rss/foodcourtrst/MainActivity.kt
```

cari konstanta `APP_URL` di bagian atas file, ganti dengan link deployment yang baru.

## Tentang login Google

Karena Apps Script kamu mensyaratkan login akun Google, WebView di app ini
sudah diatur supaya proses sign-in Google tetap bisa jalan (biasanya Google
memblokir sign-in di WebView bawaan Android). Kalau suatu saat muncul pesan
"This browser or app may not be secure" dari Google, kemungkinan Google
memperbarui deteksinya — solusi paling aman jangka panjang adalah mengubah
setelan deployment Apps Script ke akses **"Anyone within [organisasi]"**
tanpa perlu login ulang tiap buka app, atau gunakan Custom Tabs (bisa saya
bantu upgrade nanti kalau dibutuhkan).

## Struktur project

- `app/src/main/java/.../MainActivity.kt` — logika WebView (satu-satunya layar)
- `app/src/main/res/layout/activity_main.xml` — layout (WebView + halaman offline)
- `app/src/main/res/mipmap-*` — ikon aplikasi (tema merah `#C0392B`, motif nota + koin Rp)
- `app/src/main/AndroidManifest.xml` — konfigurasi aplikasi (nama, ikon, izin internet)

## Nama & ikon aplikasi

- Nama aplikasi: **Foodcourt RST** (ubah di `app/src/main/res/values/strings.xml` → `app_name`)
- Package name: `com.rss.foodcourtrst` (ubah di `app/build.gradle.kts` kalau perlu ID lain)
- Ikon: nota + koin "Rp" merah — kalau kamu punya logo resmi, kirim saja
  file logonya dan saya bisa gantikan ikon ini.

---

## Publish ke Google Play (Internal Testing) — lebih aman dari bagi-bagi APK manual

Internal Testing itu jalur "privat" di Play Console: aplikasinya **tidak
tampil ke publik** di Play Store, cuma bisa diinstall oleh email yang
kamu daftarkan sendiri (misalnya email staf kasir/admin kantin). Tapi
tetap dapat manfaat keamanan dari Play Protect dan proses install yang
lebih rapi (tidak perlu "izinkan sumber tidak dikenal" tiap install).

### Langkah 1 — Buat keystore (kunci penandatangan) sendiri

**Jangan pakai keystore buatan orang lain, termasuk dari saya** — ini
kunci yang membuktikan aplikasi ini benar-benar dari kamu, dan
**wajib dipakai sama persis setiap kali update aplikasi** di masa depan.
Kalau hilang, kamu tidak bisa lagi mengupdate aplikasi yang sudah
terlanjur di-publish dengan keystore itu.

1. Di Android Studio, buka **Build → Generate Signed App Bundle / APK…**
2. Pilih **Android App Bundle** (bukan APK — Play Store sekarang
   mewajibkan format `.aab` untuk aplikasi baru), klik **Next**
3. Klik **Create new…** untuk bikin keystore baru
4. Isi:
   - **Key store path** — pilih lokasi & nama file, misal
     `foodcourtrst-release.jks` (simpan di tempat aman, **backup ke
     Google Drive/USB terpisah**)
   - **Password** keystore & password key — pakai password kuat, catat
     di tempat aman (mis. password manager)
   - **Alias** — bebas, misal `foodcourtrst`
   - **Validity (years)** — pakai default 25 tahun
   - **Certificate** — isi nama/organisasi (boleh asal-asalan kalau app
     ini internal, tidak divalidasi Google)
5. Klik **Next**, pilih build variant **release**, klik **Finish**
6. Tunggu proses build — hasilnya file `.aab` ada di
   `app/release/app-release.aab` (lokasinya juga ditunjukkan di
   notifikasi "locate" setelah build selesai)

**Simpan baik-baik**: file `.jks` + kedua password + alias di atas.
Itu semua wajib dipakai lagi setiap kali kamu upload versi baru.

### Langkah 2 — Buat akun Google Play Console

1. Buka [play.google.com/console](https://play.google.com/console/), login
   dengan akun Google yang akan jadi "pemilik" aplikasi ini
2. Bayar biaya pendaftaran developer **sekali seumur hidup ±US$25**
3. Isi data akun developer (bisa atas nama pribadi atau PT Roda Sejahtera
   Sinergi) — verifikasi identitas bisa makan waktu 1-2 hari

### Langkah 3 — Siapkan kebijakan privasi (wajib diisi Play Console)

Saya sudah buatkan draft-nya: file `privacy-policy.html` di dalam zip
ini. Isi bagian `[ISI TANGGAL...]` dan `[ISI EMAIL/NOMOR...]`, lalu
publish di tempat yang bisa diakses lewat link publik, misalnya:
- **Google Sites** (situs.google.com) — paling gampang, tinggal
  copy-paste isinya, gratis
- Atau GitHub Pages / hosting lain kalau kamu sudah familiar

Simpan link-nya (contoh: `https://sites.google.com/view/foodcourtrst-privasi`),
nanti dipakai di Langkah 4.

### Langkah 4 — Buat aplikasi di Play Console & isi App content

1. Di Play Console, klik **Create app**
2. Isi nama app (**Foodcourt RST**), bahasa default (Indonesia), pilih
   **App** (bukan Game), pilih **Free**
3. Masuk ke menu **App content** di sidebar kiri, lengkapi semua
   deklarasi yang diminta (semuanya cuma isi formulir centang, tidak
   ada biaya tambahan):
   - **Privacy policy** — tempel link dari Langkah 3
   - **Data safety** — jelaskan app ini mengumpulkan email (untuk
     login) & data keuangan (transaksi kas), tidak dibagikan ke pihak
     ketiga, dienkripsi saat transit (HTTPS)
   - **Content rating questionnaire** — isi apa adanya (app finance
     internal, tidak ada konten dewasa/kekerasan dll → biasanya dapat
     rating "Everyone")
   - **Target audience** — pilih rentang usia dewasa/umum (bukan app
     untuk anak-anak)
   - **Ads** — pilih "No, my app does not contain ads"
   - **Government app**, **Financial features** dll — isi sesuai
     kondisi (untuk "Financial features" kemungkinan perlu centang
     karena ini app pencatatan keuangan — ikuti saja instruksi di
     layar, tidak menghambat untuk Internal Testing)

### Langkah 5 — Upload ke Internal Testing

1. Di sidebar, buka **Testing → Internal testing**
2. Klik **Create new release**
3. Upload file `.aab` dari Langkah 1
4. Isi "Release notes" singkat (misal: "Rilis awal Kas Kantin")
5. Klik **Save**, lalu **Review release**, lalu **Start rollout to
   Internal testing**

### Langkah 6 — Tambahkan penguji (tester)

1. Masih di halaman **Internal testing**, buka tab **Testers**
2. Buat daftar email (list baru), masukkan alamat Gmail staf kasir/admin
   yang boleh pakai app ini
3. Salin **link opt-in** yang muncul, kirim ke mereka lewat WhatsApp/email
4. Mereka buka link itu di HP, klik **Become a tester**, lalu bisa
   install lewat Play Store seperti aplikasi biasa (aman, sudah lewat
   Play Protect)

Proses review Google untuk Internal Testing biasanya cepat (beberapa
jam, kadang instan), jauh lebih ringan dibanding rilis publik.

### Update aplikasi di kemudian hari

Setiap kali ada perubahan (misal ganti ikon, atau link Apps Script
berubah): naikkan `versionCode` di `app/build.gradle.kts` (misal dari
`1` ke `2`), build ulang **.aab** pakai keystore **yang sama persis**
dari Langkah 1, lalu upload release baru di **Internal testing → Create
new release**.
