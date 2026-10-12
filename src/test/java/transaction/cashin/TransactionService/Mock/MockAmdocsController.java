package transaction.cashin.TransactionService.Mock;



import java.util.Collections;

import org.springframework.web.bind.annotation.*;

import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteDetail;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteResponse;

@RestController
@RequestMapping("/mock/amdocs")
public class MockAmdocsController {

    @PostMapping("/quotes")
    public QuoteResponse quote(
            @RequestBody QuoteRequest request) {

        QuoteResponse response = new QuoteResponse();

        response.setQuotationReference(
                "MOCK-Q-" + System.currentTimeMillis());

        response.setCreditParty(request.getCreditParty());
        response.setDebitParty(request.getDebitParty());
        response.setRequestAmount(request.getRequestAmount());

        QuoteDetail quoteDetail = new QuoteDetail();

        quoteDetail.setReceivingAmount(
                request.getRequestAmount());

        quoteDetail.setReceivingCurrency("MGA");

        quoteDetail.setSendingAmount(
                request.getRequestAmount());

        quoteDetail.setSendingCurrency("MGA");

        response.setQuotes(
                Collections.singletonList(quoteDetail));

        response.setFees(Collections.emptyList());
        response.setBonuses(Collections.emptyList());

        response.setQuotationReceipt("MOCK-RECEIPT");

        return response;
    }

 //   @PostMapping("/groups")
  //  public GroupResponse group(
 //           @RequestBody GroupRequest request) {

 //       GroupResponse response = new GroupResponse();

//        response.setMsisdn(request.getMsisdn());
//        response.setGroupName(request.getGroupName());

        // Test-only response.
//        response.setMember(true);

//        return response;
//    }

//    @PostMapping("/cashin")
 //   public CashInResponse cashIn(
  //          @RequestBody CashInRequest request) {

  //      CashInResponse response = new CashInResponse();

  //      response.setTransactionReference(
 //               request.getTransactionReference());

   //     if ("TXN-FAILED-001".equals(
   //             request.getTransactionReference())) {

   //         response.setStatus("FAILED");

   //         response.setMessage(
  //                  "Mock Cash-In failed");
//
  //          return response;
   //     }

   //     response.setStatus("SUCCESS");

  //      response.setMessage(
  //              "Mock Cash-In completed successfully");

   //     return response;
   // }
}