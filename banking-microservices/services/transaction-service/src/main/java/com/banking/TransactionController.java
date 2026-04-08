package com.banking;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private Map<String, Transaction> transactions = new HashMap<>();

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        transaction.setId(UUID.randomUUID().toString());
        transaction.setStatus("COMPLETED");
        transaction.setCreatedAt(new Date().toString());
        transactions.put(transaction.getId(), transaction);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(new ArrayList<>(transactions.values()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("transaction-service is UP");
    }
}