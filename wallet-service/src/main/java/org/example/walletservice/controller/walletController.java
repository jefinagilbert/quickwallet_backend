package org.example.walletservice.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class walletController {

    @GetMapping("/getBalance")
    public ResponseEntity<String> getWalletBalance(@RequestHeader("X-Auth-User-Id") UUID userId) {
        return ResponseEntity.ok("Secured connection and Fetched balance - " + userId);
    }
}
