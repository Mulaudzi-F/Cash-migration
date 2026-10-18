package transaction.cashin.TransactionService.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.time.LocalDateTime;
import transaction.cashin.TransactionService.Exception.PartyNotFoundException;
import transaction.cashin.TransactionService.Exception.DuplicateTransactionException;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInMetadata;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInParty;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.CashIn.CashInService;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.AgentGroup;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Group.GroupService;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteMetadata;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteParty;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteRequest;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteService;
import transaction.cashin.TransactionService.Integration.Amdocs.Authentication.AuthenticationResponse;
import transaction.cashin.TransactionService.Integration.Amdocs.Authentication.AuthenticationService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import transaction.cashin.TransactionService.Dto.TransactionRequest;
import transaction.cashin.TransactionService.Dto.TransactionResponse;
import transaction.cashin.TransactionService.Entity.CashInTransaction;
import transaction.cashin.TransactionService.Entity.Party;
import transaction.cashin.TransactionService.Repository.PartyRepository;
import transaction.cashin.TransactionService.Repository.TransactionRepository;
import transaction.cashin.TransactionService.Validator.CashInValidator;
import transaction.cashin.TransactionService.Validator.TransactionValidator;

@Service
public class TransactionService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PartyRepository partyRepository;

    private final TransactionRepository transactionRepository;

    private final TransactionValidator transactionValidator;

    private final CashInValidator cashInValidator;

    private final QuoteService quoteService;

    private final GroupService groupService;

    private final CashInService cashInService;

    private final AuthenticationService authenticationService;


    public TransactionService(

            PartyRepository partyRepository,

            TransactionRepository transactionRepository,

            TransactionValidator transactionValidator,

            CashInValidator cashInValidator,

            QuoteService quoteService,

            GroupService groupService,

            CashInService cashInService,

            AuthenticationService authenticationService) {

        this.partyRepository = partyRepository;

        this.transactionRepository = transactionRepository;

        this.transactionValidator = transactionValidator;

        this.cashInValidator = cashInValidator;

        this.quoteService = quoteService;

        this.groupService = groupService;

        this.cashInService = cashInService;

        this.authenticationService = authenticationService;
    }


    @Transactional
    public TransactionResponse createTransaction(
            TransactionRequest request) {

       
        // 1. Common validation
        

        transactionValidator.validate(request);


       
        // 2. Cash-In specific validation
       

        cashInValidator.validate(request);

        // PostgreSQL transaction-scoped advisory lock serializes requests sharing
        // the same reference, including requests handled by different app instances.
        // Acquire it BEFORE checking duplicates or invoking external Amdocs APIs.
        // The lock is released automatically at transaction commit/rollback.
        // This application/test configuration uses PostgreSQL.
        entityManager.createNativeQuery(
                "SELECT pg_advisory_xact_lock(hashtextextended(CAST(:reference AS text), 0))")
                .setParameter("reference", request.getReference())
                .getSingleResult();

        // Re-check only AFTER obtaining the lock: the previous request may
        // have committed while this request was waiting for the lock.
        if (transactionRepository.existsByReference(request.getReference())) {
            throw new DuplicateTransactionException(
                    "Transaction already exists: " + request.getReference());
        }


        
        // 3. Validate local parties before any external Amdocs calls.
        Party initiator = findPartyByMsisdn(request.getInitiatorMsisdn());
        Party receiver = findPartyByMsisdn(request.getReceiverMsisdn());

        // 4. Authenticate with Amdocs.
        AuthenticationResponse authenticationResponse =
                authenticationService.authenticate();

        String accessToken = authenticationResponse.getAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Amdocs authentication failed: no access token returned");
        }


        // 5. Check initiator Amdocs groups
  
        GroupRequest initiatorGroupRequest =
                new GroupRequest(
                        request.getInitiatorMsisdn());


        GroupResponse initiatorGroupResponse =
                groupService.getGroup(
                        initiatorGroupRequest,
                        accessToken);


        if (initiatorGroupResponse == null) {

            throw new IllegalStateException(
                    "Amdocs Group API returned no response " +
                    "for initiator");
        }


        // The initiator must belong to the authorized group.
        if (!hasGroup(initiatorGroupResponse, "RET_Assujetti")) {
            throw new IllegalStateException(
                    "Initiator is not a member of the RET_Assujetti group");
        }

        // 6. Check receiver Amdocs groups
        //
  

        GroupRequest receiverGroupRequest =
                new GroupRequest(
                        request.getReceiverMsisdn());


        GroupResponse receiverGroupResponse =
                groupService.getGroup(
                        receiverGroupRequest,
                        accessToken);


        if (receiverGroupResponse == null) {

            throw new IllegalStateException(
                    "Amdocs Group API returned no response " +
                    "for receiver");
        }


        if (!hasGroup(
                receiverGroupResponse,
                "Customers")) {

            throw new IllegalStateException(
                    "Receiver is not a member of the " +
                    "Customers group");
        }



        // 7. Build Quote API request
       

        String quoteInitiator =
                request.getInitiatorMsisdn();


        QuoteRequest quoteRequest =
                new QuoteRequest();


        quoteRequest.setRequestAmount(
                request.getAmount().toPlainString());


        quoteRequest.setRequestCurrency(
                request.getCurrency());


        quoteRequest.setRequestDate(
                Instant.now().toString());


        // ---------------------------------------------------------
        // Debit party
        // ---------------------------------------------------------

        quoteRequest.setDebitParty(
                List.of(
                        new QuoteParty(
                                "accountid",
                                request.getInitiatorMsisdn()
                        )
                )
        );


        // ---------------------------------------------------------
        // Credit party
        // ---------------------------------------------------------

        quoteRequest.setCreditParty(
                List.of(
                        new QuoteParty(
                                "accountid",
                                request.getReceiverMsisdn()
                        )
                )
        );


        // ---------------------------------------------------------
        // Quote metadata
        // ---------------------------------------------------------

        quoteRequest.setMetadata(
                List.of(

                        new QuoteMetadata(
                                "description",
                                request.getReference()
                        ),

                        new QuoteMetadata(
                                "subType",
                                request.getSubType()
                        )
                )
        );


        // =========================================================
        // 8. Call Amdocs Quote API
        // =========================================================

        QuoteResponse quoteResponse =
                quoteService.getQuote(
                        quoteRequest,
                        accessToken,
                        quoteInitiator);


        // =========================================================
        // 9. Validate Quote response
        // =========================================================

        if (quoteResponse == null) {

            throw new IllegalStateException(
                    "Amdocs Quote API returned no response");
        }


        if (quoteResponse.getQuotationReference() == null ||
                quoteResponse.getQuotationReference().isBlank()) {

            throw new IllegalStateException(
                    "Amdocs Quote API returned no quotation reference");
        }


        // =========================================================
        // 10. Build Cash-In request
        // =========================================================

        CashInRequest cashInRequest =
                new CashInRequest();


        cashInRequest.setAmount(
                request.getAmount().toPlainString());


        cashInRequest.setCurrency(
                request.getCurrency());


        cashInRequest.setSubType(
                request.getSubType());


        cashInRequest.setType(
                "transfer");


        cashInRequest.setRequestDate(
                Instant.now().toString());


        // ---------------------------------------------------------
        // Debit party
        //
        // Amdocs Cash-In example:
        // accountid = mvola
        // ---------------------------------------------------------

        CashInParty debitParty =
                new CashInParty(
                        "accountid",
                        "mvola");


        // ---------------------------------------------------------
        // Credit party
        // ---------------------------------------------------------

        CashInParty creditParty =
                new CashInParty(
                        "accountid",
                        request.getReceiverMsisdn());


        cashInRequest.setDebitParty(
                List.of(debitParty));


        cashInRequest.setCreditParty(
                List.of(creditParty));


        // ---------------------------------------------------------
        // Cash-In metadata
        // ---------------------------------------------------------

        CashInMetadata description =
                new CashInMetadata(
                        "description",
                        request.getReference());


        cashInRequest.setMetadata(
                List.of(description));


        // =========================================================
        // 11. Execute Cash-In
        // =========================================================

        CashInResponse cashInResponse =
                cashInService.executeCashIn(
                        cashInRequest,
                        accessToken);


        // =========================================================
        // 12. Validate Cash-In response
        // =========================================================

        if (cashInResponse == null) {

            throw new IllegalStateException(
                    "Amdocs Cash-In API returned no response");
        }


        if (cashInResponse.getTransactionStatus() == null ||
                !"completed".equalsIgnoreCase(
                        cashInResponse.getTransactionStatus())) {

            throw new IllegalStateException(
                    "Amdocs Cash-In operation failed. " +
                    "Transaction status: "
                    + cashInResponse.getTransactionStatus());
        }


        // =========================================================
        // 13. Create Cash-In transaction
        // =========================================================

        CashInTransaction transaction =
                new CashInTransaction();


        transaction.setAmount(
                request.getAmount());


        transaction.setCurrency(
                request.getCurrency());


        transaction.setSubType(
                request.getSubType());


        transaction.setReference(
                request.getReference());


        transaction.setStatus(
                cashInResponse.getTransactionStatus());


        transaction.setCreatedAt(
                LocalDateTime.now());


        transaction.setInitiator(initiator);
        transaction.setReceiver(receiver);

        // =========================================================
        // 14. Store Amdocs quotation information
        // =========================================================

        transaction.setQuotationReference(
                quoteResponse.getQuotationReference());


        transaction.setQuotationReceipt(
                quoteResponse.getQuotationReceipt());


        // =========================================================
        // 15. Persist transaction
        // =========================================================

        CashInTransaction savedTransaction =
                (CashInTransaction)
                        transactionRepository.save(
                                transaction);


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

                savedTransaction.getQuotationReceipt()
        );
    }


    // =============================================================
    // Check whether a group response contains a specific group
    // =============================================================

    private boolean hasGroup(
            GroupResponse groupResponse,
            String groupName) {

        if (groupResponse.getAgentGroups() == null) {
            return false;
        }


        return groupResponse.getAgentGroups()
                .stream()
                .map(AgentGroup::getName)
                .anyMatch(
                        name -> name != null &&
                                name.equalsIgnoreCase(groupName)
                );
    }


    // =============================================================
    // Find Party
    // =============================================================

    private Party findPartyByMsisdn(
            String msisdn) {

        return partyRepository
                .findByMsisdn(msisdn)
                .orElseThrow(
                        () -> new PartyNotFoundException(msisdn)
                );
    }
}

