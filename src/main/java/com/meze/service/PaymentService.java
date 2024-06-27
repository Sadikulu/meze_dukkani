package com.meze.service;

import com.meze.domains.Payment;
import com.meze.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void save(Payment payment){
        paymentRepository.save(payment);
    }
}
