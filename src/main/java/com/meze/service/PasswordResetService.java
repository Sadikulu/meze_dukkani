package com.meze.service;

import com.meze.domains.PasswordResetToken;
import com.meze.repository.PasswordResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetRepository passwordResetRepository;

    public void savePasswordResetToken(PasswordResetToken token){
        passwordResetRepository.save(token);
    }
    public Optional<PasswordResetToken> getToken(String token) {
        return passwordResetRepository.findByToken(token);
    }
    public void setUsedAt(String token) {
        passwordResetRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }
}
