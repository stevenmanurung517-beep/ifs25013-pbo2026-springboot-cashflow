package org.delcom.ifs25013pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final Transaction salary = new Transaction(1, "Gaji", new BigDecimal("5000000"), TransactionType.INCOME);
    private final Transaction lunch = new Transaction(2, "Makan", new BigDecimal("25000"), TransactionType.EXPENSE);
    private final Transaction bonus = new Transaction(3, "Bonus", new BigDecimal("100000"), TransactionType.INCOME);
    private final Transaction coffee = new Transaction(4, "Kopi", new BigDecimal("15000"), TransactionType.EXPENSE);
    private final List<Transaction> all = List.of(salary, lunch, bonus, coffee);

    private List<Integer> idsSortedBy(SortOption option) {
        return all.stream().sorted(option.comparator()).map(Transaction::getId).toList();
    }

    @Test
    void amountAscendingSortsSmallestFirst() {
        assertEquals(List.of(4, 2, 3, 1), idsSortedBy(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingSortsLargestFirst() {
        assertEquals(List.of(1, 3, 2, 4), idsSortedBy(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstPlacesIncomeBeforeExpense() {
        assertEquals(List.of(1, 3, 2, 4), idsSortedBy(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstPlacesExpenseBeforeIncome() {
        assertEquals(List.of(2, 4, 1, 3), idsSortedBy(SortOption.EXPENSE_FIRST));
    }

    @Test
    void hasFourOptionsAndEnumLookupWorks() {
        assertEquals(4, SortOption.values().length);
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
        assertEquals(2, TransactionType.values().length);
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
    }
}
