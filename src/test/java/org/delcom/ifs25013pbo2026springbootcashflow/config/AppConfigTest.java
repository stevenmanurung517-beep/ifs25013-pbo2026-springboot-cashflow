package org.delcom.ifs25013pbo2026springbootcashflow.config;

import org.delcom.ifs25013pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25013pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25013pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void wiresBeans() {
        AppConfig config = new AppConfig();
        ITransactionRepository repository = config.transactionRepository();
        FinanceUseCase useCase = config.financeUseCase(repository);
        FinancePresenter presenter = config.financePresenter();

        assertNotNull(repository);
        assertNotNull(useCase);
        assertNotNull(presenter);
    }
}
