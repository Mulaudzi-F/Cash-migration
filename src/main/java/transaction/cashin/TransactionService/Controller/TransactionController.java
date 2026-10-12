package transaction.cashin.TransactionService.Controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import transaction.cashin.TransactionService.Dto.TransactionRequest;
import transaction.cashin.TransactionService.Dto.TransactionResponse;
import transaction.cashin.TransactionService.Service.TransactionService;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody TransactionRequest request) {

        TransactionResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity.ok(response);
    }
}