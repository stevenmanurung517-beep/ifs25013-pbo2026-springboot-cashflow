# ifs25013-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan pribadi (**Cashflow**) berbasis **Spring Boot**, **Maven**, dan **Vaadin**
dengan arsitektur **Clean Architecture**. Data disimpan **in-memory** (hilang saat aplikasi di-restart).

- Group: `org.delcom`
- Package: `org.delcom.ifs25013pbo2026springbootcashflow`
- Java 25, Spring Boot 4.1.2, Vaadin 25.3.4

## Fitur

- Tambah **Pemasukan** dan **Pengeluaran** (dialog form)
- Cari berdasarkan keterangan (case-insensitive)
- Urutkan lewat pilihan `SortOption` (jumlah terkecil/terbesar, pemasukan/pengeluaran dulu) atau klik header kolom grid
- Kartu ringkasan: jumlah transaksi, total pemasukan, total pengeluaran, **saldo**
- Hapus dengan dialog konfirmasi
- Validasi: keterangan kosong, jumlah kosong / non-numerik / ≤ 0, ID tidak valid / tidak ditemukan
- Format uang: `Rp 1,000,000.00`

## Struktur Layer

```text
.env / .env.example                  # APP_PORT=8080
src/main/java/org/delcom/ifs25013pbo2026springbootcashflow/
├── App.java                         # entry point (memanggil DotEnv.load())
├── DotEnv.java                      # memuat .env ke system properties
├── config/AppConfig.java            # composition root (@Bean)
├── domain/
│   ├── entity/                      # Transaction, TransactionType, SortOption
│   ├── exception/                   # DomainException, EntityNotFoundException, ValidationException
│   └── repository/                  # IRepository<T, ID>, ITransactionRepository
├── usecase/FinanceUseCase.java      # termasuk getBalance()
├── adapter/
│   ├── repository/                  # InMemoryRepository<T, ID>, TransactionRepository
│   └── presenter/FinancePresenter.java
└── framework/view/FinanceView.java  # Vaadin (@Route(""))
src/test/java/.../                   # AppTest + unit test per layer (Jacoco 100%)
```

Dependency Rule: `domain ← usecase ← adapter / framework`.

## Menjalankan

```powershell
.\mvnw.cmd spring-boot:run
```

Buka `http://localhost:8080` (atau sesuai `APP_PORT` di `.env`).

Build JAR (wajib profil production Vaadin):

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25013-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Test + coverage Jacoco 100%:

```powershell
.\mvnw.cmd clean verify
```

Laporan: `target/site/jacoco/index.html`
