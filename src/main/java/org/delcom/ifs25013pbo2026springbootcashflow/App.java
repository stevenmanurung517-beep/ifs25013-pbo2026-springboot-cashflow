package org.delcom.ifs25013pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Meta(name = "description", content = "Cashflow: aplikasi pencatat pemasukan, pengeluaran, dan saldo keuangan pribadi.")
public class App implements AppShellConfigurator {

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }

}
