package transaction.cashout;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/cashout")
public class CashoutController {

    private final CashoutService cashoutService;

    public CashoutController(CashoutService cashoutService) {
        this.cashoutService = cashoutService;
    }

    @PostMapping("/fee")
    public ResponseEntity<CashoutFeeResponse> getFee(
            @RequestBody CashoutFeeRequest request) {
        return ResponseEntity.ok(cashoutService.getFee(request));
    }

    @PostMapping
    public ResponseEntity<CashoutResponse> cashout(
            @RequestBody CashoutRequest request) {
        return ResponseEntity.ok(cashoutService.cashout(request));
    }
}
