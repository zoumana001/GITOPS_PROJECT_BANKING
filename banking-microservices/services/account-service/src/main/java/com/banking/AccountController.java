package com.banking;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private Map<String, Account> accounts = new HashMap<>();

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        account.setId(UUID.randomUUID().toString());
        account.setBalance(0.0);
        account.setStatus("ACTIVE");
        accounts.put(account.getId(), account);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        Account account = accounts.get(id);
        if (account == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(new ArrayList<>(accounts.values()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("account-service is UP");
    }
}