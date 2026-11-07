package org.delcom.ifs25013pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAndExpenseAssignIncrementingIds() {
        Transaction income = repository.saveIncome("Gaji", new BigDecimal("1000"));
        Transaction expense = repository.saveExpense("Makan", new BigDecimal("50"));

        assertEquals(1, income.getId());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", BigDecimal.TEN);

        List<Transaction> copy = repository.findAll();
        copy.clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdReturnsMatchOrEmpty() {
        Transaction saved = repository.saveIncome("Gaji", BigDecimal.TEN);

        assertSame(saved, repository.findById(saved.getId()).orElseThrow());
        assertTrue(repository.findById(99).isEmpty());
    }

    @Test
    void deleteByIdRemovesOnlyExistingEntity() {
        Transaction saved = repository.saveIncome("Gaji", BigDecimal.TEN);

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void idsAreNotReusedAfterDelete() {
        Transaction first = repository.saveIncome("A", BigDecimal.ONE);
        repository.deleteById(first.getId());

        assertEquals(2, repository.saveIncome("B", BigDecimal.ONE).getId());
    }

    @Test
    void findByFiltersWithPredicate() {
        repository.saveIncome("Gaji", BigDecimal.TEN);
        repository.saveExpense("Makan", BigDecimal.ONE);

        List<Transaction> incomes = repository.findBy(Transaction::isIncome);

        assertEquals(1, incomes.size());
        assertEquals("Gaji", incomes.get(0).getDescription());
    }

    @Test
    void saveStoresGivenEntityAndUpdateKeepsReferenceChanges() {
        Transaction manual = new Transaction(50, "Manual", BigDecimal.ONE, TransactionType.INCOME);

        assertSame(manual, repository.save(manual));

        manual.changeDescription("Diubah");
        repository.update(manual);

        assertEquals("Diubah", repository.findById(50).orElseThrow().getDescription());
    }
}
