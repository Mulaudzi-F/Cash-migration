package transaction.cashin.TransactionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import transaction.cashin.TransactionService.Entity.Transaction;
import transaction.cashin.TransactionService.Repository.TransactionRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.dao.DataAccessResourceFailureException;
import transaction.cashin.TransactionService.Integration.Amdocs.Authentication.AuthenticationService;
import transaction.cashin.TransactionService.Integration.Amdocs.Quote.QuoteService;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    classes = TransactionServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CashInIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private AuthenticationService authenticationService;

    @MockitoSpyBean
    private QuoteService quoteService;

    @MockitoSpyBean
    private TransactionRepository repositorySpy;

    // ==========================================================
    // TEST 1: SUCCESSFUL CASH-IN TRANSACTION
    // ==========================================================

    @Test
    void shouldProcessCashInEndToEnd() throws Exception {

        String reference = "TXN-TEST-" + UUID.randomUUID();

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "%s",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """.formatted(reference);

        MvcResult result = mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").exists())
            .andExpect(jsonPath("$.transactionType").value("CASH_IN"))
            .andExpect(jsonPath("$.amount").value(1000.00))
            .andExpect(jsonPath("$.currency").value("MGA"))
            .andExpect(jsonPath("$.status").value("completed"))
            .andExpect(jsonPath("$.reference").value(reference))
            .andExpect(
                jsonPath("$.quotationReference").value("MOCK-QUOTE-001")
            )
            .andExpect(
                jsonPath("$.quotationReceipt").value("MOCK-RECEIPT-001")
            )
            .andReturn();

        // Extract the transaction ID returned by the API.
        String responseBody = result.getResponse().getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);

        long transactionId = responseJson.get("transactionId").asLong();

        assertTrue(
            transactionId > 0,
            "Transaction ID must be greater than zero"
        );

        // Retrieve the exact transaction created by this test.
        Transaction transaction = transactionRepository
            .findById(transactionId)
            .orElseThrow(
                () -> new AssertionError(
                    "Transaction not found in database: " + transactionId
                )
            );

        // Verify the saved transaction.
        assertEquals(
            0,
            transaction.getAmount()
                .compareTo(new BigDecimal("1000.00"))
        );

        assertEquals("MGA", transaction.getCurrency());
        assertEquals("cashin", transaction.getSubType());
        assertEquals("completed", transaction.getStatus());
        assertEquals(reference, transaction.getReference());

        assertEquals(
            "MOCK-QUOTE-001",
            transaction.getQuotationReference()
        );

        assertEquals(
            "MOCK-RECEIPT-001",
            transaction.getQuotationReceipt()
        );

        // Verify initiator.
        assertNotNull(transaction.getInitiator());

        assertEquals(
            "0341234567",
            transaction.getInitiator().getMsisdn()
        );

        // Verify receiver.
        assertNotNull(transaction.getReceiver());

        assertEquals(
            "0347654321",
            transaction.getReceiver().getMsisdn()
        );
    }

    // ==========================================================
    // TEST 2: REJECT INVALID CASH-IN AMOUNT
    // ==========================================================

    @Test
    void shouldRejectInvalidCashInAmount() throws Exception {

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 0,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "TXN-INVALID-001",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """;

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.error").value("VALIDATION_ERROR")
            )
            .andExpect(
                jsonPath("$.message").value(
                    "Transaction amount must be greater than zero"
                )
            );
    }

    // ==========================================================
    // TEST 3: REJECT INVALID CASH-IN SUBTYPE
    // ==========================================================

    @Test
    void shouldRejectInvalidCashInSubtype() throws Exception {

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "INVALID_SUBTYPE",
                "reference": "TXN-INVALID-002",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """;

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.error").value("VALIDATION_ERROR")
            )
            .andExpect(
                jsonPath("$.message").value(
                    "Invalid Cash-In subtype. "
                    + "Expected cashin or remote_cashin"
                )
            );
    }

    // ==========================================================
    // TEST 4: REJECT UNKNOWN INITIATOR
    // ==========================================================

    @Test
    void shouldRejectUnknownInitiator() throws Exception {

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "TXN-INVALID-003",
                "initiatorMsisdn": "0399999999",
                "receiverMsisdn": "0347654321"
            }
            """;

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(
                jsonPath("$.error").value("PARTY_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message").value(
                    "Party not found: 0399999999"
                )
            );
    }

    // ==========================================================
    // TEST 5: AMDOCS QUOTATION FAILURE
    // ==========================================================

    @Test
    void shouldRejectFailedAmdocsQuotation() throws Exception {

        // The mock must return HTTP 500 for references starting with TXN-FAILED-.
        String reference = "TXN-FAILED-" + UUID.randomUUID();

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "%s",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """.formatted(reference);

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(
                jsonPath("$.error").value("INTERNAL_SERVER_ERROR")
            )
            .andExpect(
                jsonPath("$.message").value(
                    "An unexpected error occurred"
                )
            );
    }

    // ==========================================================
    // TEST 6: AMDOCS CASH-IN EXECUTION FAILURE
    // ==========================================================

    @Test
    void shouldRejectFailedAmdocsCashInExecution() throws Exception {

        String reference = "TXN-CASHIN-FAILED-001";

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "TXN-CASHIN-FAILED-001",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """;

        // Count matching transactions before execution.
        long recordsBefore = transactionRepository.findAll()
            .stream()
            .filter(t -> reference.equals(t.getReference()))
            .count();

        // The mock Amdocs server should return HTTP 500
        // from the actual Cash-In transaction endpoint.
        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(
                jsonPath("$.error").value("INTERNAL_SERVER_ERROR")
            )
            .andExpect(
                jsonPath("$.message").value(
                    "An unexpected error occurred"
                )
            );

        // Check that no new transaction was saved.
        long recordsAfter = transactionRepository.findAll()
            .stream()
            .filter(t -> reference.equals(t.getReference()))
            .count();

        assertEquals(
            recordsBefore,
            recordsAfter,
            "A failed Cash-In execution must not save a completed transaction"
        );
    }
    @Test
void shouldPreventDuplicateCashInTransaction() throws Exception {

    String reference = "TXN-DUPLICATE-" + java.util.UUID.randomUUID();

    String request = """
        {
            "transactionType": "CASH_IN",
            "amount": 1000.00,
            "currency": "MGA",
            "subType": "cashin",
            "reference": "%s",
            "initiatorMsisdn": "0341234567",
            "receiverMsisdn": "0347654321"
        }
        """.formatted(reference);

    // STEP 1: Submit the first Cash-In request.
    mockMvc.perform(
            post("/api/v1/transactions")
                .contentType("application/json")
                .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("completed"));

    // STEP 2: Submit the exact same request again.
    mockMvc.perform(
            post("/api/v1/transactions")
                .contentType("application/json")
                .content(request)
        )
        .andExpect(status().isConflict());

    // STEP 3: Verify that only one transaction was saved.
    long transactionCount = transactionRepository.findAll()
            .stream()
            .filter(t -> reference.equals(t.getReference()))
            .count();

    assertEquals(
        1L,
        transactionCount,
        "Duplicate Cash-In must not create another transaction"
    );
}


    // ==========================================================
    // TEST 8: CONCURRENT DUPLICATE CASH-IN TRANSACTIONS
    // ==========================================================
    @Test
    void shouldPreventConcurrentDuplicateCashInTransactions() throws Exception {
        String reference = "TXN-CONCURRENT-" + UUID.randomUUID();
        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "%s",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """.formatted(reference);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Integer> sendRequest = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for concurrent start");
                }
                MvcResult response = mockMvc.perform(
                    post("/api/v1/transactions")
                        .contentType("application/json")
                        .content(request)
                ).andReturn();
                return response.getResponse().getStatus();
            };

            Future<Integer> first = executor.submit(sendRequest);
            Future<Integer> second = executor.submit(sendRequest);
            assertTrue(ready.await(10, TimeUnit.SECONDS),
                "Both requests must be ready before starting");
            start.countDown();

            int firstStatus = first.get(30, TimeUnit.SECONDS);
            int secondStatus = second.get(30, TimeUnit.SECONDS);
            System.out.println("Concurrent Cash-In statuses: "
                + firstStatus + ", " + secondStatus);

            long savedCount = transactionRepository.findAll().stream()
                .filter(transaction -> reference.equals(transaction.getReference()))
                .count();

            assertEquals(1L, savedCount,
                "Exactly one Cash-In transaction must be saved");
            assertTrue(
                (firstStatus == 200 && secondStatus == 409)
                    || (firstStatus == 409 && secondStatus == 200),
                "Expected one HTTP 200 and one HTTP 409, got "
                    + firstStatus + " and " + secondStatus
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                "Concurrent request threads did not terminate");
        }
    }

    // TEST 9: CURRENCY IS REQUIRED (current validator checks blank, not ISO code)
    @Test
    void shouldRejectMissingCurrency() throws Exception {
        String request = validRequest("TXN-CURRENCY-" + UUID.randomUUID())
            .replace("\"currency\": \"MGA\"", "\"currency\": \"\"");
        mockMvc.perform(post("/api/v1/transactions")
                .contentType("application/json").content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Currency is required"));
    }

    // TEST 10: UNKNOWN RECEIVER
    @Test
    void shouldRejectUnknownReceiver() throws Exception {
        String request = validRequest("TXN-RECEIVER-" + UUID.randomUUID())
            .replace("0347654321", "0399999998");
        mockMvc.perform(post("/api/v1/transactions")
                .contentType("application/json").content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("PARTY_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Party not found: 0399999998"));
    }

    // TEST 11: MISSING REQUIRED REFERENCE
    @Test
    void shouldRejectMissingRequiredReference() throws Exception {
        String request = validRequest("TXN-MISSING-" + UUID.randomUUID());
        JsonNode root = objectMapper.readTree(request);
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("reference", "");
        request = objectMapper.writeValueAsString(root);
        mockMvc.perform(post("/api/v1/transactions")
                .contentType("application/json").content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Transaction reference is required"));
    }

    // TEST 12: AUTHENTICATION FAILURE; no Amdocs Cash-In execution
    @Test
    void shouldHandleAmdocsAuthenticationFailure() throws Exception {
        String reference = "TXN-AUTH-FAILED-" + UUID.randomUUID();
        try {
            doThrow(new IllegalStateException("Simulated Amdocs auth failure"))
                .when(authenticationService).authenticate();
            mockMvc.perform(post("/api/v1/transactions")
                    .contentType("application/json").content(validRequest(reference)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
            assertEquals(0, countByReference(reference));
        } finally {
            reset(authenticationService);
        }
    }

    // TEST 13: SIMULATED QUOTE TIMEOUT; current handler returns HTTP 500
    @Test
    void shouldHandleAmdocsQuoteTimeout() throws Exception {
        String reference = "TXN-TIMEOUT-" + UUID.randomUUID();
        try {
            doThrow(new IllegalStateException("Simulated quotation timeout"))
                .when(quoteService).getQuote(any(), anyString(), anyString());
            mockMvc.perform(post("/api/v1/transactions")
                    .contentType("application/json").content(validRequest(reference)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
            assertEquals(0, countByReference(reference));
        } finally {
            reset(quoteService);
        }
    }

    // TEST 14: DATABASE SAVE FAILURE (does not prove no external charge)
    @Test
    void shouldHandleDatabaseSaveFailure() throws Exception {
        String reference = "TXN-DB-FAILED-" + UUID.randomUUID();
        try {
            doThrow(new DataAccessResourceFailureException("Simulated database write failure"))
                .when(repositorySpy).save(any());
            mockMvc.perform(post("/api/v1/transactions")
                    .contentType("application/json").content(validRequest(reference)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
        } finally {
            reset(repositorySpy);
        }
        assertEquals(0, countByReference(reference));
    }

    // TEST 15: FAILED QUOTATION MUST NOT PERSIST A COMPLETED TRANSACTION
    @Test
    void shouldNotPersistTransactionWhenQuotationFails() throws Exception {
        String reference = "TXN-FAILED-" + UUID.randomUUID();
        long before = countByReference(reference);
        mockMvc.perform(post("/api/v1/transactions")
                .contentType("application/json").content(validRequest(reference)))
            .andExpect(status().isInternalServerError());
        assertEquals(before, countByReference(reference),
            "Failed quotation must not create a transaction record");
    }

    private long countByReference(String reference) {
        return transactionRepository.findAll().stream()
            .filter(t -> reference.equals(t.getReference())).count();
    }

    private String validRequest(String reference) {
        return """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "%s",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """.formatted(reference);
    }

}
