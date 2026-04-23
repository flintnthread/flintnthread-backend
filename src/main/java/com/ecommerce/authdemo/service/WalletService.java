package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.WalletResponse;

public interface WalletService {

    void createWallet(Integer userId);

    WalletResponse getWallet(Integer userId);

    void addMoney(Integer userId, Double amount);

    void deductMoney(Integer userId, Double amount);
}
