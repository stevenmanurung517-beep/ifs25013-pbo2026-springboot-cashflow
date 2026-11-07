package org.delcom.ifs25013pbo2026springbootcashflow.usecase;

import org.delcom.ifs25013pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2);
    }

    @Test
    void addIncomeAndExpenseStoreTrimmedDescriptionAndTwoDecimalAmount() throws Exception {
        Transaction income = useCase.addIncome("  Gaji  ", " 5000000 ");
        Transaction expense = useCase.addExpense("Makan", "25000.505");

        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(money("5000000"), income.getAmount());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(money("25000.51"), expense.getAmount());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void addRejectsBlankOrNullDescription() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, "100"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("   ", "100"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("", "100"));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void addRejectsBlankNullNonNumericAndNonPositiveAmount() {
        ValidationException nullAmount = assertThrows(ValidationException.class,
                () -> useCase.addIncome("Gaji", null));
        ValidationException blankAmount = assertThrows(ValidationException.class,
                () -> useCase.addExpense("Makan", "  "));
        ValidationException notNumber = assertThrows(ValidationException.class,
                () -> useCase.addIncome("Gaji", "seribu"));
        ValidationException negative = assertThrows(ValidationException.class,
                () -> useCase.addExpense("Makan", "-5000"));
        ValidationException zero = assertThrows(ValidationException.class,
                () -> useCase.addIncome("Gaji", "0"));
        ValidationException tooSmall = assertThrows(ValidationException.class,
                () -> useCase.addIncome("Gaji", "0.001"));

        assertEquals("Jumlah tidak boleh kosong", nullAmount.getMessage());
        assertEquals("Jumlah tidak boleh kosong", blankAmount.getMessage());
        assertEquals("Jumlah harus berupa angka", notNumber.getMessage());
        assertEquals("Jumlah harus lebih besar dari 0", negative.getMessage());
        assertEquals("Jumlah harus lebih besar dari 0", zero.getMessage());
        assertEquals("Jumlah harus lebih besar dari 0", tooSmall.getMessage());
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeTransactionDeletesExistingOne() throws Exception {
        Transaction saved = useCase.addIncome("Gaji", "1000");

        useCase.removeTransaction(saved.getId());

        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeTransactionRejectsInvalidAndUnknownId() {
        ValidationException invalid = assertThrows(ValidationException.class,
                () -> useCase.removeTransaction(0));
        EntityNotFoundException missing = assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransaction(99));

        assertEquals("ID tidak valid", invalid.getMessage());
        assertEquals(99, missing.getId());
    }

    @Test
    void searchFiltersByDescriptionCaseInsensitiveWithoutChangingData() throws Exception {
        useCase.addIncome("Gaji Bulanan", "1000");
        useCase.addExpense("Makan Siang", "10");

        assertEquals(1, useCase.searchTransactions("gaji").size());
        assertEquals(1, useCase.searchTransactions("  SIANG ").size());
        assertTrue(useCase.searchTransactions("tidak ada").isEmpty());
        assertEquals(2, useCase.searchTransactions(null).size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortTransactionsUsesDomainComparators() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");
        useCase.addIncome("Bonus", "100000");

        assertEquals(List.of(2, 3, 1), ids(useCase.sortTransactions(SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 3, 2), ids(useCase.sortTransactions(SortOption.AMOUNT_DESC)));
        assertEquals(List.of(1, 3, 2), ids(useCase.sortTransactions(SortOption.INCOME_FIRST)));
        assertEquals(List.of(2, 1, 3), ids(useCase.sortTransactions(SortOption.EXPENSE_FIRST)));
        assertEquals(List.of(1, 2, 3), ids(useCase.getAllTransactions()));
    }

    @Test
    void balanceIsIncomeMinusExpenseAndStaysConsistentAfterAddAndRemove() throws Exception {
        assertEquals(BigDecimal.ZERO, useCase.getBalance());

        useCase.addIncome("Gaji", "5000000");
        Transaction expense = useCase.addExpense("Belanja", "25000");

        assertEquals(money("5000000"), useCase.getTotalIncome());
        assertEquals(money("25000"), useCase.getTotalExpense());
        assertEquals(money("4975000"), useCase.getBalance());

        useCase.removeTransaction(expense.getId());

        assertEquals(money("5000000"), useCase.getBalance());
    }

    @Test
    void balanceCanBeNegativeWhenExpenseExceedsIncome() throws Exception {
        useCase.addIncome("Uang saku", "10");
        useCase.addExpense("Buku", "30");

        assertEquals(money("-20"), useCase.getBalance());
    }

    private static List<Integer> ids(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }
}
