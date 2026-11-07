package org.delcom.ifs25013pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<TransactionType, String> TYPE_LABELS = Map.of(
            TransactionType.INCOME, "Pemasukan",
            TransactionType.EXPENSE, "Pengeluaran"
    );

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    );

    /** Memformat uang, contoh: 1000000 → "Rp 1,000,000.00". */
    public String formatMoney(BigDecimal amount) {
        return "Rp " + String.format(Locale.US, "%,.2f", amount);
    }

    /** Memformat jumlah milik satu transaksi. */
    public String formatAmount(Transaction transaction) {
        return formatMoney(transaction.getAmount());
    }

    /** Label tipe transaksi: "Pemasukan" atau "Pengeluaran". */
    public String formatType(Transaction transaction) {
        return TYPE_LABELS.get(transaction.getType());
    }

    /** Label opsi pengurutan untuk ditampilkan di UI. */
    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat satu transaksi: {@code [id] keterangan | Rp 1,000,000.00 | [Pemasukan]}. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatAmount(transaction),
                formatType(transaction)
        );
    }

    /** Memformat saldo: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Judul daftar semua transaksi. */
    public String showListHeader() {
        return "Daftar Transaksi";
    }

    /** Judul hasil pencarian berdasarkan kata kunci. */
    public String showSearchHeader(String keyword) {
        return "Hasil Pencarian: \"" + keyword + "\"";
    }

    /** Judul daftar yang sudah diurutkan. */
    public String showSortHeader(SortOption option) {
        return "Daftar Transaksi (Urut: " + formatSortOption(option) + ")";
    }

    /** Pesan saat belum ada transaksi. */
    public String showEmptyMessage() {
        return "Data transaksi belum tersedia!";
    }

    /** Pesan saat pencarian tidak menemukan hasil. */
    public String showNotFoundMessage() {
        return "Transaksi tidak ditemukan!";
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah " + formatType(transaction).toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
