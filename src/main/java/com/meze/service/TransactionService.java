package com.meze.service;

import com.meze.domains.Transaction;
import com.meze.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void save (Transaction transaction){
        transactionRepository.save(transaction);
    }
}
