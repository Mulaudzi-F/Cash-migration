package transaction.cashin.TransactionService.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import transaction.cashin.TransactionService.Dto.CashInOperationRequest;
import transaction.cashin.TransactionService.Dto.CashInOperationResponse;
import transaction.cashin.TransactionService.Dto.FeeRequest;
import transaction.cashin.TransactionService.Dto.FeeResponse;
import transaction.cashin.TransactionService.Service.TransactionService;

@RestController
@RequestMapping("/v1/otc")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/fee")
    public ResponseEntity<FeeResponse> getFee(
            @RequestBody FeeRequest request) {

        return ResponseEntity.ok(
                transactionService.getFee(request));
    }

    @PostMapping("/cashin")
    public ResponseEntity<CashInOperationResponse> mbOTCCashinOperation(
            @RequestBody CashInOperationRequest request) {

        return ResponseEntity.ok(
                transactionService.mbOTCCashinOperation(request));
    }
}