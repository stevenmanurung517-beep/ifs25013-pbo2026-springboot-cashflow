package org.delcom.ifs25013pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private final Transaction income = new Transaction(
            1, "Gaji", new BigDecimal("1000000"), TransactionType.INCOME,
            LocalDateTime.of(2026, 11, 7, 9, 5));
    private final Transaction expense = new Transaction(
            2, "Makan", new BigDecimal("25000.5"), TransactionType.EXPENSE,
            LocalDateTime.of(2026, 11, 8, 18, 45));

    @Test
    void formatMoneyUsesThousandSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 0.50", presenter.formatMoney(new BigDecimal("0.5")));
        assertEquals("Rp -20.00", presenter.formatMoney(new BigDecimal("-20")));
    }

    @Test
    void formatAmountAndTypeAndCreatedAt() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(income));
        assertEquals("Pemasukan", presenter.formatType(income));
        assertEquals("Pengeluaran", presenter.formatType(expense));
        assertEquals("07/11/2026 09:05", presenter.formatCreatedAt(income));
    }

    @Test
    void formatLineFollowsModuleReferenceFormat() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan | Rp 25,000.50 | [Pengeluaran]", presenter.format(expense));
    }

    @Test
    void formatBalanceFollowsModuleReferenceFormat() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new BigDecimal("4975000")));
    }

    @Test
    void sortOptionLabelsCoverAllOptions() {
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void headersAndEmptyMessages() {
        assertEquals("Daftar Transaksi", presenter.showListHeader());
        assertEquals("Hasil Pencarian: \"gaji\"", presenter.showSearchHeader("gaji"));
        assertEquals("Daftar Transaksi (Urut: Jumlah terkecil)",
                presenter.showSortHeader(SortOption.AMOUNT_ASC));
        assertEquals("Data transaksi belum tersedia!", presenter.showEmptyMessage());
        assertEquals("Transaksi tidak ditemukan!", presenter.showNotFoundMessage());
    }

    @Test
    void notificationMessages() {
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menambah pengeluaran: [2] Makan | Rp 25,000.50 | [Pengeluaran]",
                presenter.showAddSuccess(expense));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("[!] Jumlah tidak boleh kosong", presenter.showValidationError("Jumlah tidak boleh kosong"));
    }
}
