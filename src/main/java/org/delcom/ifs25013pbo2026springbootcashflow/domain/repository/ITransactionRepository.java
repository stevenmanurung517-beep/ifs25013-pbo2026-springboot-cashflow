package org.delcom.ifs25013pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi baru dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /**
     * Menyimpan transaksi pemasukan baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveIncome(String description, BigDecimal amount);

    /**
     * Menyimpan transaksi pengeluaran baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveExpense(String description, BigDecimal amount);
}
