package transaction.cashin.TransactionService.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import transaction.cashin.TransactionService.Dto.CashInOperationRequest;
import transaction.cashin.TransactionService.Dto.CashInOperationResponse;
import transaction.cashin.TransactionService.Dto.FeeRequest;
import transaction.cashin.TransactionService.Dto.FeeResponse;
import transaction.cashin.TransactionService.Dto.TransactionRequest;
import transaction.cashin.TransactionService.Dto.TransactionResponse;
import transaction.cashin.TransactionService.Entity.CashInTransaction;
import transaction.cashin.TransactionService.Entity.OtcHistory;
import transaction.cashin.TransactionService.Entity.OtcHistorySuccess;
import transaction.cashin.TransactionService.Entity.Party;
import transaction.cashin.TransactionService.Entity.TlmCellId;
import transaction.cashin.TransactionService.Exception.PartyNotFoundException;
import transaction.cashin.TransactionService.Integration.Amdocs.Authentication.AuthenticationResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Authentication.AuthenticationService;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInMetadata;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInParty;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInService;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.AgentGroup;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupService;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteAmount;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteFee;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteMetadata;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteParty;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteService;
import transaction.cashin.TransactionService.Repository.OtcHistoryRepository;
import transaction.cashin.TransactionService.Repository.OtcHistorySuccessRepository;
import transaction.cashin.TransactionService.Repository.PartyRepository;
import transaction.cashin.TransactionService.Repository.TlmCellIdRepository;
import transaction.cashin.TransactionService.Repository.TransactionRepository;
import transaction.cashin.TransactionService.Validator.CashInValidator;
import transaction.cashin.TransactionService.Validator.TransactionValidator;

@Service
public class TransactionService {

    private final PartyRepository partyRepository;

    private final TransactionRepository transactionRepository;

    private final TransactionValidator transactionValidator;

    private final CashInValidator cashInValidator;

    private final QuoteService quoteService;

    private final GroupService groupService;

    private final CashInService cashInService;

    private final AuthenticationService authenticationService;

    private final TlmCellIdRepository tlmCellIdRepository;

    private final OtcHistoryRepository otcHistoryRepository;

    private final OtcHistorySuccessRepository otcHistorySuccessRepository;

    @Value("${cashin.duration-hours:4}")
    private int cashInDurationHours;

    public TransactionService(

            PartyRepository partyRepository,

            TransactionRepository transactionRepository,

            TransactionValidator transactionValidator,

            CashInValidator cashInValidator,

            QuoteService quoteService,

            GroupService groupService,

            CashInService cashInService,

            AuthenticationService authenticationService,

            TlmCellIdRepository tlmCellIdRepository,

            OtcHistoryRepository otcHistoryRepository,

            OtcHistorySuccessRepository otcHistorySuccessRepository) {

        this.partyRepository = partyRepository;

        this.transactionRepository = transactionRepository;

        this.transactionValidator = transactionValidator;

        this.cashInValidator = cashInValidator;

        this.quoteService = quoteService;

        this.groupService = groupService;

        this.cashInService = cashInService;

        this.authenticationService = authenticationService;

        this.tlmCellIdRepository = tlmCellIdRepository;

        this.otcHistoryRepository = otcHistoryRepository;

        this.otcHistorySuccessRepository = otcHistorySuccessRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {

        // 1. Common validation

        transactionValidator.validate(request);

        // 2. Cash-In specific validation

        cashInValidator.validate(request);

        // 3. Authenticate with Amdocs

        AuthenticationResponse authenticationResponse = authenticationService.authenticate();

        String accessToken = authenticationResponse.getAccessToken();

        if (accessToken == null || accessToken.isBlank()) {

            throw new IllegalStateException("Amdocs authentication failed: no access token returned");
        }

        // 4. Resolve parties

        // Party initiator =
        // findPartyByMsisdn(
        // request.getInitiatorMsisdn());

        // Party receiver =
        // findPartyByMsisdn(
        // request.getReceiverMsisdn());

        // 5. Check initiator Amdocs groups

        GroupRequest initiatorGroupRequest = new GroupRequest(request.getInitiatorMsisdn());

        GroupResponse initiatorGroupResponse = groupService.getGroup(initiatorGroupRequest, accessToken);

        if (initiatorGroupResponse == null) {

            throw new IllegalStateException("Amdocs Group API returned no response " + "for initiator");
        }

        /*
         * //Please return this as soon as you're done testing. It's super neccessary if
         * (!hasGroup( initiatorGroupResponse, "RET_Assujetti")) {
         * 
         * throw new IllegalStateException( "Initiator is not a member of the " +
         * "RET_Assujetti group"); }
         */

        //
        // 6. Check receiver Amdocs groups
        //

        GroupRequest receiverGroupRequest = new GroupRequest(request.getReceiverMsisdn());

        GroupResponse receiverGroupResponse = groupService.getGroup(receiverGroupRequest, accessToken);

        if (receiverGroupResponse == null) {

            throw new IllegalStateException("Amdocs Group API returned no response " + "for receiver");
        }

        if (!hasGroup(receiverGroupResponse, "Customers")) {

            throw new IllegalStateException("Receiver is not a member of the " + "Customers group");
        }

        // 7. Build Quote API request

        String quoteInitiator = request.getInitiatorMsisdn();

        QuoteRequest quoteRequest = new QuoteRequest();

        quoteRequest.setRequestAmount(request.getAmount().toPlainString());

        quoteRequest.setRequestCurrency(request.getCurrency());

        quoteRequest.setRequestDate(Instant.now().toString());

        // ---------------------------------------------------------
        // Debit party
        // ---------------------------------------------------------

        quoteRequest.setDebitParty(List.of(new QuoteParty("accountid", request.getInitiatorMsisdn())));

        // ---------------------------------------------------------
        // Credit party
        // ---------------------------------------------------------

        quoteRequest.setCreditParty(List.of(new QuoteParty("accountid", request.getReceiverMsisdn())));

        // ---------------------------------------------------------
        // Quote metadata
        // ---------------------------------------------------------

        quoteRequest.setMetadata(List.of(

                new QuoteMetadata("description", request.getReference()),

                new QuoteMetadata("subType", request.getSubType())));

        // =========================================================
        // 8. Call Amdocs Quote API
        // =========================================================

        QuoteResponse quoteResponse = quoteService.getQuote(quoteRequest, accessToken, quoteInitiator);

        // =========================================================
        // 9. Validate Quote response
        // =========================================================

        if (quoteResponse == null) {

            throw new IllegalStateException("Amdocs Quote API returned no response");
        }

        if (quoteResponse.getQuotationReference() == null || quoteResponse.getQuotationReference().isBlank()) {

            throw new IllegalStateException("Amdocs Quote API returned no quotation reference");
        }

        // =========================================================
        // 10. Build Cash-In request
        // =========================================================

        CashInRequest cashInRequest = new CashInRequest();

        cashInRequest.setAmount(request.getAmount().toPlainString());

        cashInRequest.setCurrency(request.getCurrency());

        cashInRequest.setSubType(request.getSubType());

        cashInRequest.setType("transfer");

        cashInRequest.setRequestDate(Instant.now().toString());

        // ---------------------------------------------------------
        // Debit party
        //
        // Amdocs Cash-In example:
        // accountid = mvola
        // ---------------------------------------------------------

        CashInParty debitParty = new CashInParty("accountid", "mvola");

        // ---------------------------------------------------------
        // Credit party
        // ---------------------------------------------------------

        CashInParty creditParty = new CashInParty("accountid", request.getReceiverMsisdn());

        cashInRequest.setDebitParty(List.of(debitParty));

        cashInRequest.setCreditParty(List.of(creditParty));

        // ---------------------------------------------------------
        // Cash-In metadata
        // ---------------------------------------------------------

        CashInMetadata description = new CashInMetadata("description", request.getReference());

        cashInRequest.setMetadata(List.of(description));

        // =========================================================
        // 11. Execute Cash-In
        // =========================================================

        CashInResponse cashInResponse = cashInService.executeCashIn(cashInRequest, accessToken);

        // =========================================================
        // 12. Validate Cash-In response
        // =========================================================

        if (cashInResponse == null) {

            throw new IllegalStateException("Amdocs Cash-In API returned no response");
        }

        if (cashInResponse.getTransactionStatus() == null || !"completed".equalsIgnoreCase(cashInResponse.getTransactionStatus())) {

            throw new IllegalStateException("Amdocs Cash-In operation failed. " + "Transaction status: " + cashInResponse.getTransactionStatus());
        }

        // =========================================================
        // 13. Create Cash-In transaction
        // =========================================================

        CashInTransaction transaction = new CashInTransaction();

        transaction.setAmount(request.getAmount());

        transaction.setCurrency(request.getCurrency());

        transaction.setSubType(request.getSubType());

        transaction.setReference(request.getReference());

        transaction.setStatus(cashInResponse.getTransactionStatus());

        transaction.setCreatedAt(LocalDateTime.now());

        // transaction.setInitiator(
        // initiator);

        // transaction.setReceiver(
        // receiver);

        // =========================================================
        // 14. Store Amdocs quotation information
        // =========================================================

        transaction.setQuotationReference(quoteResponse.getQuotationReference());

        transaction.setQuotationReceipt(quoteResponse.getQuotationReceipt());

        // =========================================================
        // 15. Persist transaction
        // =========================================================

        CashInTransaction savedTransaction = (CashInTransaction) transactionRepository.save(transaction);

        // =========================================================
        // 16. Return REST response
        // =========================================================

        return new TransactionResponse(

                savedTransaction.getId(),

                request.getTransactionType(),

                savedTransaction.getAmount(),

                savedTransaction.getCurrency(),

                savedTransaction.getStatus(),

                savedTransaction.getReference(),

                savedTransaction.getQuotationReference(),

                savedTransaction.getQuotationReceipt());
    }

    // =============================================================
    // Check whether a group response contains a specific group
    // =============================================================

    private boolean hasGroup(GroupResponse groupResponse, String groupName) {

        if (groupResponse.getAgentGroups() == null) {
            return false;
        }

        return groupResponse.getAgentGroups().stream().map(AgentGroup::getName).anyMatch(name -> name != null && name.equalsIgnoreCase(groupName));
    }

    // =============================================================
    // Find Party
    // =============================================================

    private Party findPartyByMsisdn(String msisdn) {

        return partyRepository.findByMsisdn(msisdn).orElseThrow(() -> new PartyNotFoundException(msisdn));
    }

    public FeeResponse getFee(FeeRequest request) {
        if (request == null || isBlank(request.getCashPointMsisdn()) || isBlank(request.getCustomerMsisdn()) || request.getAmount() == null || isBlank(
                request.getCashPointCGI()) || isBlank(request.getSubtype())) {
            throw new IllegalArgumentException("Invalid msisdn or amount format");
        }
        if (!"cashin".equalsIgnoreCase(request.getSubtype()) && !"remote_cashin".equalsIgnoreCase(request.getSubtype())) {
            throw new IllegalArgumentException("Invalid msisdn or amount format");
        }
        if (isBlank(request.getCustomerCGI()) || "0".equals(request.getCustomerCGI().trim())) {
            return zeroFee();
        }

        String cashPointCity = cityFor(request.getCashPointCGI());
        if (cashPointCity == null) {
            return zeroFee();
        }
        String customerCity = cityFor(request.getCustomerCGI());
        if (customerCity == null || cashPointCity.equalsIgnoreCase(customerCity)) {
            return zeroFee();
        }

        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setRequestAmount(request.getAmount().toPlainString());
        quoteRequest.setRequestCurrency("MGA");
        quoteRequest.setRequestDate(Instant.now().toString());
        quoteRequest.setDebitParty(List.of(new QuoteParty("accountid", request.getCashPointMsisdn())));
        quoteRequest.setCreditParty(List.of(new QuoteParty("accountid", request.getCustomerMsisdn())));
        quoteRequest.setMetadata(List.of(new QuoteMetadata("cashPointCGI", request.getCashPointCGI()), new QuoteMetadata("customerCGI", request.getCustomerCGI()),
                new QuoteMetadata("subType", request.getSubtype())));

        AuthenticationResponse authentication = authenticationService.authenticate();
        QuoteResponse quoteResponse = quoteService.getQuote(quoteRequest, authentication.getAccessToken(), request.getCashPointMsisdn());
        FeeResponse response = new FeeResponse();
        response.setStatusCode(200);
        response.setStatusReason("Fee fetched from AMDOCS");
        response.setFees(readFee(quoteResponse));
        return response;
    }

    public CashInOperationResponse mbOTCCashinOperation(CashInOperationRequest request) {
        if (request == null || isBlank(request.getRequestId()) || isBlank(request.getCashPointRef()) || isBlank(request.getCustomerMsisdn()) || request
                .getAmount() == null || isBlank(request.getCashPointPin()) || isBlank(request.getType()) || isBlank(request.getSubtype())) {
            throw new IllegalArgumentException("Bad Inputs");
        }
        if ("remote_cashin".equalsIgnoreCase(request.getSubtype()) && isBlank(request.getFeeQuoteId())) {
            throw new IllegalArgumentException("Bad Inputs");
        }

        AuthenticationResponse authentication;
        try {
            authentication = authenticationService.authenticate();
        } catch (RuntimeException exception) {
            return failedOperation(exception);
        }

        GroupResponse groups;
        try {
            groups = groupService.getGroup(
                    new GroupRequest(request.getCashPointRef()),
                    authentication.getAccessToken());
        } catch (RuntimeException exception) {
            return failedOperation(exception);
        }
        if (groups == null) {
            CashInOperationResponse missing = new CashInOperationResponse();
            missing.setResponseCode("Technical Error");
            missing.setMessage("Technical Error");
            return missing;
        }
        boolean retAssujetti = hasGroup(groups, "RET_Assujetti");

        java.util.List<OtcHistorySuccess> previous = otcHistorySuccessRepository.findByBeneficiaireOrderByOperationDateDesc(request.getCustomerMsisdn());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusHours(cashInDurationHours);
        OtcHistorySuccess latest = previous.isEmpty() ? null : previous.get(0);
        boolean withinDuration = latest != null && latest.getOperationDate() != null && latest.getOperationDate().isAfter(windowStart);

        CashInRequest cashInRequest = new CashInRequest();
        cashInRequest.setAmount(request.getAmount().toPlainString());
        cashInRequest.setCurrency("MGA");
        cashInRequest.setType("transfer");
        cashInRequest.setSubType(request.getSubtype());
        cashInRequest.setRequestDate(Instant.now().toString());
        cashInRequest.setUsername(request.getCashPointRef());
        cashInRequest.setPassword(request.getCashPointPin());
        cashInRequest.setDebitParty(List.of(new CashInParty("accountid", request.getCashPointRef())));
        cashInRequest.setCreditParty(List.of(new CashInParty("accountid", request.getCustomerMsisdn())));
        ArrayList<CashInMetadata> metadata = new ArrayList<>();
        metadata.add(new CashInMetadata("operation_id", request.getRequestId()));
        if (!isBlank(request.getFeeQuoteId())) {
            metadata.add(new CashInMetadata("quotationReference", request.getFeeQuoteId()));
        }
        if (retAssujetti) {
            metadata.add(new CashInMetadata("asujeti_tva", "true"));
        }
        cashInRequest.setMetadata(metadata);

        CashInResponse cashInResponse;
        try {
            cashInResponse = cashInService.executeCashIn(cashInRequest);
        } catch (RuntimeException exception) {
            return failedOperation(exception);
        }

        CashInOperationResponse response = toOperationResponse(cashInResponse);
        if ("Success".equals(response.getResponseCode())) {
            int counter = nextCounter(latest, withinDuration);
            saveHistory(request, cashInResponse, now, counter);
        }
        return response;
    }

    private FeeResponse zeroFee() {
        FeeResponse response = new FeeResponse();
        response.setStatusCode(200);
        response.setStatusReason("Fee = 0");
        response.setFees(BigDecimal.ZERO);
        return response;
    }

    private String cityFor(String cgi) {
        return tlmCellIdRepository.findById(cgi).map(TlmCellId::getVille).filter(ville -> !isBlank(ville)).orElse(null);
    }

    private BigDecimal readFee(QuoteResponse quoteResponse) {
        BigDecimal total = BigDecimal.ZERO;
        if (quoteResponse == null || quoteResponse.getFees() == null) {
            return total;
        }
        for (QuoteFee fee : quoteResponse.getFees()) {
            if (fee.getAmounts() == null) {
                continue;
            }
            for (QuoteAmount amount : fee.getAmounts()) {
                if (amount.getAmount() != null && !amount.getAmount().isBlank()) {
                    total = total.add(new BigDecimal(amount.getAmount()));
                }
            }
        }
        return total;
    }

    private CashInOperationResponse toOperationResponse(CashInResponse cashInResponse) {
        CashInOperationResponse response = new CashInOperationResponse();
        if (cashInResponse == null || isBlank(cashInResponse.getTransactionStatus())) {
            response.setResponseCode("Technical Error");
            response.setMessage("Technical Error");
            return response;
        }
        if ("completed".equalsIgnoreCase(cashInResponse.getTransactionStatus())) {
            response.setResponseCode("Success");
            response.setMessage("Success");
            return response;
        }
        response.setResponseCode(cashInResponse.getTransactionStatus());
        response.setMessage(cashInResponse.getTransactionStatus());
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CashInOperationResponse failedOperation(RuntimeException exception) {
        CashInOperationResponse response = new CashInOperationResponse();
        response.setResponseCode(failureCode(exception));
        response.setMessage(response.getResponseCode());
        return response;
    }

    private String failureCode(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof HttpOperationFailedException httpFailure) {
                return String.valueOf(httpFailure.getStatusCode());
            }
            current = current.getCause();
        }
        return "Technical Error";
    }

    private int nextCounter(OtcHistorySuccess latest, boolean withinDuration) {
        if (latest == null || !withinDuration || latest.getCounterCashin() == null || latest.getCounterCashin() < 0) {
            return 1;
        }
        return latest.getCounterCashin() + 1;
    }

    private void saveHistory(CashInOperationRequest request, CashInResponse cashInResponse, LocalDateTime operationDate, int counter) {
        OtcHistory history = new OtcHistory();
        history.setOperationId(request.getRequestId());
        history.setInitiateur(request.getCashPointRef());
        history.setBeneficiaire(request.getCustomerMsisdn());
        history.setMontant(request.getAmount());
        history.setTransId(cashInResponse.getTransactionReference());
        history.setOperationType(request.getType());
        history.setOperationDate(operationDate);
        history.setCounterCashin(counter);
        otcHistoryRepository.save(history);

        OtcHistorySuccess success = new OtcHistorySuccess();
        success.setOperationId(request.getRequestId());
        success.setInitiateur(request.getCashPointRef());
        success.setBeneficiaire(request.getCustomerMsisdn());
        success.setMontant(request.getAmount());
        success.setTransId(cashInResponse.getTransactionReference());
        success.setOperationType(request.getType());
        success.setOperationDate(operationDate);
        success.setCounterCashin(counter);
        otcHistorySuccessRepository.save(success);
    }
}
