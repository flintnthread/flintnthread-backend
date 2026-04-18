package com.ecommerce.authdemo.service;



    public interface WalletService {

        void createWallet(Integer userId);

        void addMoney(Integer userId, Double amount);

        void deductMoney(Integer userId, Double amount);
    }

