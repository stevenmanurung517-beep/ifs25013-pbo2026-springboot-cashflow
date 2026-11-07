package org.delcom.ifs25013pbo2026springbootcashflow.usecase;

import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Use case yang menangani logika bisnis aplikasi Cashflow.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(String description, String amount) throws ValidationException {
        String validDescription = validateDescription(description);
        BigDecimal validAmount = parseAmount(amount);
        return transactionRepository.saveIncome(validDescription, validAmount);
    }

    /** Menambahkan transaksi pengeluaran setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(String description, String amount) throws ValidationException {
        String validDescription = validateDescription(description);
        BigDecimal validAmount = parseAmount(amount);
        return transactionRepository.saveExpense(validDescription, validAmount);
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     *
     * @throws ValidationException     jika ID bukan bilangan positif
     * @throws EntityNotFoundException jika transaksi dengan ID tersebut tidak ada
     */
    public void removeTransaction(int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lowerKeyword = Objects.requireNonNullElse(keyword, "").trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return sumAmount(Transaction::isIncome);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return sumAmount(Predicate.not(Transaction::isIncome));
    }

    /** Saldo saat ini = total pemasukan − total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sumAmount(Predicate<Transaction> filter) {
        return transactionRepository.findBy(filter).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        return description.trim();
    }

    private BigDecimal parseAmount(String amount) throws ValidationException {
        if (amount == null || amount.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(amount.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (parsed.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return parsed;
    }
}
