package transaction.cashin.TransactionService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = {
        "transaction.cashin.TransactionService",
        "transaction.cashout"
})
@EntityScan(basePackages = {
        "transaction.cashin.TransactionService.Entity",
        "transaction.cashout"
})
@EnableJpaRepositories(basePackages = {
        "transaction.cashin.TransactionService.Repository",
        "transaction.cashout"
})
public class TransactionServiceApplication extends  SpringBootServletInitializer{

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(TransactionServiceApplication.class);
    }
}
