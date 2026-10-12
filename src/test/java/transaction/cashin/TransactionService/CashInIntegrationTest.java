
package transaction.cashin.TransactionService;
import transaction.cashin.TransactionService.Entity.Transaction;
import transaction.cashin.TransactionService.Repository.TransactionRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;



import transaction.cashin.TransactionService.Entity.Transaction;
import transaction.cashin.TransactionService.Repository.TransactionRepository;

@SpringBootTest(
    classes = {
        TransactionServiceApplication.class,
        transaction.cashin.TransactionService.Mock.MockAmdocsController.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)




@AutoConfigureMockMvc
@ActiveProfiles("test")
class CashInIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TransactionRepository transactionRepository;

    
    @Test
    void shouldProcessCashInEndToEnd() throws Exception {

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "TXN-TEST-001",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """;

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").exists())
            .andExpect(jsonPath("$.transactionType")
                    .value("CASH_IN"))
            .andExpect(jsonPath("$.amount")
                    .value(1000.00))
            .andExpect(jsonPath("$.currency")
                    .value("MGA"))
            .andExpect(jsonPath("$.status")
                    .value("RECEIVED"))
            .andExpect(jsonPath("$.reference")
                    .value("TXN-TEST-001"))
            .andExpect(jsonPath("$.quotationReference")
                    .exists())
            .andExpect(jsonPath("$.quotationReceipt")
                    .value("MOCK-RECEIPT"));
        
        
        Transaction transaction =
                transactionRepository
                        .findAll()
                        .stream()
                        .filter(t ->
                                "TXN-TEST-001".equals(t.getReference()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                0,
                transaction.getAmount()
                        .compareTo(new java.math.BigDecimal("1000.00")));

        assertEquals("MGA", transaction.getCurrency());
        assertEquals("cashin", transaction.getSubType());
        assertEquals("RECEIVED", transaction.getStatus());
        assertEquals("MOCK-RECEIPT", transaction.getQuotationReceipt());

        assertNotNull(transaction.getQuotationReference());

        assertNotNull(transaction.getInitiator());
        assertEquals(
                "0341234567",
                transaction.getInitiator().getMsisdn());

        assertNotNull(transaction.getReceiver());
        assertEquals(
                "0347654321",
                transaction.getReceiver().getMsisdn());
    }
    
    
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
            .andExpect(jsonPath("$.status")
                    .value(400))
            .andExpect(jsonPath("$.error")
                    .value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message")
                    .value(
                        "Transaction amount must be greater than zero"
                    ));
    }
    
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
            .andExpect(jsonPath("$.error")
                    .value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message")
                    .value(
                        "Invalid Cash-In subtype. " +
                        "Expected cashin or remote_cashin"
                    ));
    }
    
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
            .andExpect(jsonPath("$.error")
                    .value("PARTY_NOT_FOUND"))
            .andExpect(jsonPath("$.message")
                    .value("Party not found: 0399999999"));
    }
    
    @Test
    void shouldRejectFailedAmdocsCashIn() throws Exception {

        String request = """
            {
                "transactionType": "CASH_IN",
                "amount": 1000.00,
                "currency": "MGA",
                "subType": "cashin",
                "reference": "TXN-FAILED-001",
                "initiatorMsisdn": "0341234567",
                "receiverMsisdn": "0347654321"
            }
            """;

        mockMvc.perform(
                post("/api/v1/transactions")
                    .contentType("application/json")
                    .content(request)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error")
                    .value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message")
                    .value("An unexpected error occurred"));
    }
    
}
