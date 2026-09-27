package org.example.merchantservice.service;

import org.example.merchantservice.entity.Merchant;
import org.example.merchantservice.exception.MerchantNotFoundException;
import org.example.merchantservice.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public Merchant createMerchant(UUID userId, String businessName) {
        Optional<Merchant> existing = merchantRepository.findByUserId(userId);

        if(existing.isPresent()) {
            return existing.get();
        }

        Merchant merchant = new Merchant(userId, businessName);
        return merchantRepository.save((merchant));
    }

    @Transactional
    public BigDecimal getMerchantBalance(UUID userId, String merchantId) {
        Merchant merchant = merchantRepository.findByIdAndUserId(merchantId, userId)
                .orElseThrow(() -> new MerchantNotFoundException("No Merchant account found: " + merchantId));
        return merchant.getBalance();
    }

    @Transactional
    public void creditMerchant(UUID userId, String merchantId, BigDecimal amount) {
      if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Credit amount has to be positive");
      }
      Merchant merchant = merchantRepository.findByIdAndUserId(merchantId, userId)
              .orElseThrow(() -> new MerchantNotFoundException("Merchant not found: " + merchantId));
      merchant.setBalance(merchant.getBalance().add(amount));
      merchantRepository.save(merchant);
    }
}
