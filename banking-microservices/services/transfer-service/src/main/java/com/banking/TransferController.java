package com.banking;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private Map<String, Transfer> transfers = new HashMap<>();

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer) {
        transfer.setId(UUID.randomUUID().toString());
        transfer.setStatus("COMPLETED");
        transfer.setCreatedAt(new Date().toString());
        transfers.put(transfer.getId(), transfer);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransfer(@PathVariable String id) {
        Transfer transfer = transfers.get(id);
        if (transfer == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    public ResponseEntity<List<Transfer>> getAllTransfers() {
        return ResponseEntity.ok(new ArrayList<>(transfers.values()));
    }

    @GetMapping("/from/{accountId}")
    public ResponseEntity<List<Transfer>> getByFromAccount(@PathVariable String accountId) {
        List<Transfer> result = new ArrayList<>();
        for (Transfer t : transfers.values()) {
            if (accountId.equals(t.getFromAccountId())) result.add(t);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("transfer-service is UP");
    }
}