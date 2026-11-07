package org.delcom.ifs25013pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void constructorWithoutTimeUsesCurrentTime() {
        Transaction transaction = new Transaction(1, "Gaji", new BigDecimal("5000000"), TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(new BigDecimal("5000000"), transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void constructorWithTimeKeepsGivenTime() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 7, 13, 30);
        Transaction transaction = new Transaction(2, "Makan", BigDecimal.TEN, TransactionType.EXPENSE, time);

        assertEquals(time, transaction.getCreatedAt());
    }

    @Test
    void isIncomeAndSignedAmountFollowType() {
        Transaction income = new Transaction(1, "Gaji", new BigDecimal("100"), TransactionType.INCOME);
        Transaction expense = new Transaction(2, "Makan", new BigDecimal("40"), TransactionType.EXPENSE);

        assertTrue(income.isIncome());
        assertFalse(expense.isIncome());
        assertEquals(new BigDecimal("100"), income.signedAmount());
        assertEquals(new BigDecimal("-40"), expense.signedAmount());
    }

    @Test
    void changeMethodsUpdateData() {
        Transaction transaction = new Transaction(1, "Lama", new BigDecimal("10"), TransactionType.INCOME);

        transaction.changeDescription("Baru");
        transaction.changeAmount(new BigDecimal("20"));

        assertEquals("Baru", transaction.getDescription());
        assertEquals(new BigDecimal("20"), transaction.getAmount());
    }
}
