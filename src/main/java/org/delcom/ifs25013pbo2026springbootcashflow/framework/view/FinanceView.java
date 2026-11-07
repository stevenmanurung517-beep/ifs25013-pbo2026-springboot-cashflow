package org.delcom.ifs25013pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25013pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Tambah pemasukan/pengeluaran lewat dialog, cari berdasarkan keterangan,
 * urutkan lewat pilihan {@link SortOption} atau klik header kolom, hapus via dialog konfirmasi.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    /** Aksi penambahan transaksi (pemasukan atau pengeluaran) dari dialog form. */
    @FunctionalInterface
    private interface TransactionAdder {
        Transaction add(String description, String amount) throws ValidationException;
    }

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balance = new Span();

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Aksesibilitas: seluruh isi halaman berada di dalam landmark "main".
        getElement().setAttribute("role", "main");

        H1 title = new H1("Cashflow");

        configureGrid();
        configureToolbar();

        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ------------------------------------------------------------------
    // Kartu ringkasan
    // ------------------------------------------------------------------

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard("Saldo", balance, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balance.setText(presenter.formatBalance(financeUseCase.getBalance()));
    }

    // ------------------------------------------------------------------
    // Grid
    // ------------------------------------------------------------------

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(presenter::formatAmount)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction));
        badge.getElement().getThemeList().add(transaction.isIncome() ? "badge success" : "badge error");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(),
                event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("color", "#B91C1C");
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------
    // Toolbar
    // ------------------------------------------------------------------

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                sortTransactions(event.getValue());
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                event -> openIncomeDialog());
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        incomeButton.getStyle().set("background", "#166534").set("color", "#FFFFFF");

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openExpenseDialog());
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        expenseButton.getStyle().set("background", "#991B1B").set("color", "#FFFFFF");

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                sortBox,
                refreshButton,
                incomeButton,
                expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------

    private void openIncomeDialog() {
        openAddDialog("Tambah Pemasukan", financeUseCase::addIncome);
    }

    private void openExpenseDialog() {
        openAddDialog("Tambah Pengeluaran", financeUseCase::addExpense);
    }

    private void openAddDialog(String title, TransactionAdder adder) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                Transaction created = adder.add(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        VerticalLayout form = new VerticalLayout(
                descriptionField, amountField, buildDialogActions(cancelButton, saveButton));
        form.setPadding(false);
        dialog.add(form);
        dialog.open();
    }

    /** Baris tombol aksi (rata kanan) di bagian bawah dialog. */
    private HorizontalLayout buildDialogActions(Button cancelButton, Button confirmButton) {
        HorizontalLayout actions = new HorizontalLayout(cancelButton, confirmButton);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return actions;
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Menghapus Transaksi");
        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.getStyle().set("background", "#991B1B").set("color", "#FFFFFF");

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        VerticalLayout content = new VerticalLayout(message, buildDialogActions(cancelButton, confirmButton));
        content.setPadding(false);
        dialog.add(content);
        dialog.open();
    }

    // ------------------------------------------------------------------
    // Aksi
    // ------------------------------------------------------------------

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        showTransactions(
                financeUseCase.searchTransactions(keyword),
                presenter.showSearchHeader(keyword),
                presenter.showNotFoundMessage()
        );
    }

    private void sortTransactions(SortOption option) {
        showTransactions(
                financeUseCase.sortTransactions(option),
                presenter.showSortHeader(option),
                presenter.showEmptyMessage()
        );
    }

    private void refreshAll() {
        showTransactions(
                financeUseCase.getAllTransactions(),
                presenter.showListHeader(),
                presenter.showEmptyMessage()
        );
        sortBox.clear();
        searchField.clear();
    }

    private void showTransactions(List<Transaction> transactions, String title, String emptyMessage) {
        emptyState.setText(emptyMessage);
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(transactions.isEmpty() ? title + " — " + emptyMessage : title);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
