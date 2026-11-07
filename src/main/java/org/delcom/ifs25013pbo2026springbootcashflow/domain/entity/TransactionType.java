package org.delcom.ifs25013pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Urutan deklarasi dipakai sebagai urutan alami: INCOME lebih dulu daripada EXPENSE.
 */
public enum TransactionType {

    /** Pemasukan: menambah saldo. */
    INCOME,

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE
}
