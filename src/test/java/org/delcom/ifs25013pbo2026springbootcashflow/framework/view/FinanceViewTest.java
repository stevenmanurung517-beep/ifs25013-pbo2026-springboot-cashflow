package org.delcom.ifs25013pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25013pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter()));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void addSearchSortAndDeleteFlow() throws Exception {
        assertTrue(statusText().contains("Data transaksi belum tersedia"));
        assertSummary("0", "Rp 0.00", "Rp 0.00", "Saldo: Rp 0.00");

        submitDialog("Tambah Pemasukan", "Gaji", "5000000");
        assertEquals(1, useCase.getAllTransactions().size());
        assertEquals("Daftar Transaksi", statusText());

        submitDialog("Tambah Pengeluaran", "Makan Siang", "25000.5");
        submitDialog("Tambah Pengeluaran", "Kopi", "10000");
        assertEquals(3, useCase.getAllTransactions().size());
        assertSummary("3", "Rp 5,000,000.00", "Rp 35,000.50", "Saldo: Rp 4,964,999.50");

        // Pencarian hanya memfilter tampilan, data tetap utuh.
        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(statusText().contains("Transaksi tidak ditemukan"));

        _setValue(searchField, "makan");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"makan\"", statusText());
        Grid<Transaction> grid = grid();
        assertEquals(1, grid.getListDataView().getItemCount());
        assertEquals(3, useCase.getAllTransactions().size());

        clickButton("Muat Ulang");
        assertEquals(3, grid.getListDataView().getItemCount());
        assertEquals("Daftar Transaksi", statusText());
        assertEquals("", searchField.getValue());

        // Pengurutan lewat pilihan SortOption (comparator domain).
        ComboBox<SortOption> sortBox = sortBox();
        _setValue(sortBox, SortOption.AMOUNT_ASC);
        assertEquals("Daftar Transaksi (Urut: Jumlah terkecil)", statusText());
        assertEquals("Kopi", firstItem(grid).getDescription());

        _setValue(sortBox, SortOption.AMOUNT_DESC);
        assertEquals("Gaji", firstItem(grid).getDescription());

        _setValue(sortBox, SortOption.INCOME_FIRST);
        assertTrue(firstItem(grid).isIncome());

        _setValue(sortBox, SortOption.EXPENSE_FIRST);
        assertFalse(firstItem(grid).isIncome());

        clickButton("Muat Ulang");
        assertTrue(sortBox.isEmpty());
        assertEquals("Daftar Transaksi", statusText());

        // Pengurutan lewat header kolom grid.
        for (String key : List.of("id", "description", "amount", "type", "createdAt")) {
            grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(key), SortDirection.ASCENDING)));
            grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(key), SortDirection.DESCENDING)));
        }
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("amount"), SortDirection.ASCENDING)));
        assertEquals("Kopi", firstItem(grid).getDescription());
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("description"), SortDirection.DESCENDING)));
        assertEquals("Makan Siang", firstItem(grid).getDescription());

        // Badge tipe transaksi.
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey("id"), SortDirection.ASCENDING)));
        assertEquals("Pemasukan", ((Span) _getCellComponent(grid, 0, "type")).getText());
        assertEquals("Pengeluaran", ((Span) _getCellComponent(grid, 1, "type")).getText());

        // Hapus dengan konfirmasi.
        openDeleteDialog(grid, indexOfDescription(grid, "Kopi"));
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(2, useCase.getAllTransactions().size());
        assertSummary("2", "Rp 5,000,000.00", "Rp 25,000.50", "Saldo: Rp 4,974,999.50");
    }

    @Test
    void handlesValidationErrorsAndCancelledDialogs() throws Exception {
        clickButton("Tambah Pemasukan");
        Dialog incomeDialog = _get(Dialog.class);
        List<TextField> incomeFields = _find(incomeDialog, TextField.class);
        _setValue(incomeFields.get(0), " ");
        _setValue(incomeFields.get(1), "100");
        clickDialogButton(incomeDialog, "Simpan");
        clickDialogButton(incomeDialog, "Batal");

        clickButton("Tambah Pengeluaran");
        Dialog expenseDialog = _get(Dialog.class);
        List<TextField> expenseFields = _find(expenseDialog, TextField.class);
        _setValue(expenseFields.get(0), "Makan");
        _setValue(expenseFields.get(1), "abc");
        clickDialogButton(expenseDialog, "Simpan");
        _setValue(expenseFields.get(1), "-5");
        clickDialogButton(expenseDialog, "Simpan");
        _setValue(expenseFields.get(1), "");
        clickDialogButton(expenseDialog, "Simpan");
        clickDialogButton(expenseDialog, "Batal");

        assertTrue(useCase.getAllTransactions().isEmpty());

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "   ");
        clickButton("Cari");
        assertTrue(statusText().contains("Data transaksi belum tersedia"));

        useCase.addIncome("Ada", "10");
        clickButton("Muat Ulang");

        openDeleteDialog(grid(), 0);
        clickDialogButton(_get(Dialog.class), "Batal");
        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void showsErrorWhenDeleteFails() throws Exception {
        Transaction saved = useCase.addIncome("Temp", "10");
        clickButton("Muat Ulang");

        doThrow(new EntityNotFoundException(saved.getId())).when(useCase).removeTransaction(saved.getId());
        openDeleteDialog(grid(), 0);
        clickDialogButton(_get(Dialog.class), "Hapus");

        doThrow(new ValidationException("ID tidak valid")).when(useCase).removeTransaction(saved.getId());
        openDeleteDialog(grid(), 0);
        clickDialogButton(_get(Dialog.class), "Hapus");

        assertEquals(1, useCase.getAllTransactions().size());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    @SuppressWarnings("unchecked")
    private ComboBox<SortOption> sortBox() {
        return _get(ComboBox.class);
    }

    private Transaction firstItem(Grid<Transaction> grid) {
        return grid.getListDataView().getItems().toList().get(0);
    }

    private int indexOfDescription(Grid<Transaction> grid, String description) {
        List<Transaction> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (description.equals(items.get(i).getDescription())) {
                return i;
            }
        }
        throw new AssertionError("Transaksi tidak ditemukan: " + description);
    }

    private String statusText() {
        return _get(_get(FinanceView.class), Paragraph.class).getText();
    }

    private void assertSummary(String count, String income, String expense, String balance) {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = (HorizontalLayout) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElseThrow();
        List<String> values = summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
        assertEquals(List.of(count, income, expense, balance), values);
    }

    private void submitDialog(String openButton, String description, String amount) {
        clickButton(openButton);
        Dialog dialog = _get(Dialog.class);
        List<TextField> fields = _find(dialog, TextField.class);
        _setValue(fields.get(0), description);
        _setValue(fields.get(1), amount);
        clickDialogButton(dialog, "Simpan");
    }

    private void openDeleteDialog(Grid<Transaction> grid, int row) {
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, row, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}
