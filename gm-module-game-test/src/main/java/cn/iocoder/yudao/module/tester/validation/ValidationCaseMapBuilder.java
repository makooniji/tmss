package cn.iocoder.yudao.module.tester.validation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ValidationCaseMapBuilder {

    public static Map<String, Map<String, List<ValidationMeta>>> build() {

        Map<String, Map<String, List<ValidationMeta>>> root = new LinkedHashMap<>();

        // ===============================
        // 1. 验证无效签章
        // ===============================
        Map<String, List<ValidationMeta>> invalidSignature = new LinkedHashMap<>();

        invalidSignature.put("1.1 Invalid Signature Wallet / Balance",
                List.of(new ValidationMeta("1.01.01", "wallet/balance", "成功")));

        invalidSignature.put("1.2 Invalid Signature Wallet / Bet",
                List.of(new ValidationMeta("1.02.01", "wallet/bet", "成功")));


        invalidSignature.put("1.3 Invalid Signature Wallet / Bet Result",
                List.of(new ValidationMeta("1.03.01", "wallet/bet_result", "成功")));

        invalidSignature.put("1.4 Invalid Signature Wallet / Rollback",
                List.of(new ValidationMeta("1.04.01", "wallet/rollback", "成功")));

        invalidSignature.put("1.5 Invalid Signature Wallet / Adjustment",
                List.of(new ValidationMeta("1.05.01", "wallet/adjustment", "成功")));
        invalidSignature.put("1.6 INVALID_SIGNATURE_WALLET/BET_DEBIT",
                List.of(new ValidationMeta("1.06.01", "wallet/bet_debit", "成功")));

        invalidSignature.put("1.7 INVALID_SIGNATURE_WALLET/BET_CREDIT",
                List.of(new ValidationMeta("1.07.01", "wallet/bet_credit", "成功")));

        root.put("1 Validate Invalid Signature", invalidSignature);

        // ===============================
        // 2. 验证无效用户名
        // ===============================
        Map<String, List<ValidationMeta>> invalidUsername = new LinkedHashMap<>();

        invalidUsername.put("2.1 Invalid Username Wallet / Balance",
                List.of(new ValidationMeta("2.01.01", "wallet/balance", "成功")));

        invalidUsername.put("2.2 Invalid Username Wallet / Bet",
                List.of(new ValidationMeta("2.02.01", "wallet/bet", "成功")));

        invalidUsername.put("2.3 Invalid Username Wallet / Bet Result",
                List.of(new ValidationMeta("2.03.01", "wallet/bet_result", "成功")));

        invalidUsername.put("2.4 Invalid Username Wallet / Rollback",
                List.of(new ValidationMeta("2.04.01", "wallet/rollback", "成功")));

        invalidUsername.put("2.5 Invalid Username Wallet / Adjustment",
                List.of(new ValidationMeta("2.05.01", "wallet/adjustment", "成功")));
        invalidUsername.put("2.6 INVALID_SIGNATURE_WALLET/BET_DEBIT",
                List.of(new ValidationMeta("2.06.01", "wallet/bet_debit", "成功")));

        invalidUsername.put("2.7 INVALID_SIGNATURE_WALLET/BET_CREDIT",
                List.of(new ValidationMeta("2.07.01", "wallet/bet_credit", "成功")));

        root.put("2 Validate Invalid Username", invalidUsername);

        // ===============================
        // 3. 钱包余额不足
        // ===============================
        Map<String, List<ValidationMeta>> insufficientFunds = new LinkedHashMap<>();

        insufficientFunds.put("3.1 Insufficient Balance Wallet / Bet",
                List.of(
                        new ValidationMeta("3.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.01.02", "wallet/bet", "成功")
                ));

        insufficientFunds.put("3.2 Insufficient Balance Wallet / Bet Result [Bet Win]",
                List.of(
                        new ValidationMeta("3.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.02.02", "wallet/bet_result", "成功")
                ));
        insufficientFunds.put("3.3 Insufficient Balance Wallet / Bet Result [Bet Lose]",
                List.of(
                        new ValidationMeta("3.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.03.02", "wallet/bet_result", "成功")
                ));

        insufficientFunds.put("3.4 Insufficient Balance Wallet / Rollback",
                List.of(
                        new ValidationMeta("3.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.04.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("3.04.03", "wallet/bet", "成功"),
                        new ValidationMeta("3.04.04", "wallet/rollback", "成功")
                ));
        insufficientFunds.put("3.5 Insufficient Balance Wallet / Adjustment",
                List.of(
                        new ValidationMeta("3.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.05.02", "wallet/bet", "成功"),
                        new ValidationMeta("3.05.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("3.05.04", "wallet/bet_result", "成功"),

                        new ValidationMeta("3.05.05", "wallet/adjustment", "成功")
                ));

        insufficientFunds.put("3.6 FISH_DEBIT_AMOUNT_WALLET/BET_DEBIT_INSUFFICIENT_BALANCE",
                List.of(
                        new ValidationMeta("3.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.06.02", "wallet/bet_debit", "成功")
                ));

        root.put("3 Validate Insufficient Balance", insufficientFunds);
        //4 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds4 = new LinkedHashMap<>();
        insufficientFunds4.put("4.1 Debit Amount Wallet / Bet Then [End]",
                List.of(
                        new ValidationMeta("4.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("4.01.03", "wallet/bet_result", "成功")
                ));
        insufficientFunds4.put("4.2 Debit Amount Wallet / Bet Result [BET_LOSE]",
                List.of(
                        new ValidationMeta("4.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.02.02", "wallet/bet_result", "成功")
                ));
        insufficientFunds4.put("4.3 FISH_DEBIT_AMOUNT_WALLET/BET_DEBIT_INTEGER_AMOUNT",
                List.of(
                        new ValidationMeta("4.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.03.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("4.03.03", "wallet/bet_credit", "成功")
                ));
        insufficientFunds4.put("4.4 FISH_DEBIT_AMOUNT_WALLET/BET_DEBIT_DECIMAL_AMOUNT",
                List.of(
                        new ValidationMeta("4.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.04.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("4.04.03", "wallet/bet_credit", "成功")
                ));

        root.put("4 Validate Debit Amount", insufficientFunds4);

        //5 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds5 = new LinkedHashMap<>();
        insufficientFunds5.put("5.1 Credit Amount Wallet / Bet Result [WIN]",
                List.of(
                        new ValidationMeta("5.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("5.01.03", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.2 Credit Amount Wallet / Bet Result [WIN] then [END]",
                List.of(
                        new ValidationMeta("5.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.02.02", "wallet/bet", "成功"),
                        new ValidationMeta("5.02.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("5.02.04", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.3 Credit Amount Wallet / Bet Result [BET_WIN]",
                List.of(
                        new ValidationMeta("5.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.03.02", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.4 Credit Amount Wallet / Bet Result [WIN] with 0 Bet Amount",
                List.of(
                        new ValidationMeta("5.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.04.02", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.5 Credit Amount Wallet / Bet Result [WIN] then [END]",
                List.of(
                        new ValidationMeta("5.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.05.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("5.05.03", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.6 Credit Amount Wallet / Bet Result [WIN] then [WIN] then [END]",
                List.of(
                        new ValidationMeta("5.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.06.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("5.06.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("5.06.04", "wallet/bet_result", "成功")
                ));
        insufficientFunds5.put("5.7 FISH_CREDIT_AMOUNT_WALLET/BET_CREDIT_INTEGER_AMOUNT",
                List.of(
                        new ValidationMeta("5.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.07.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("5.07.03", "wallet/bet_credit", "成功")
                ));
        insufficientFunds5.put("5.8 FISH_CREDIT_AMOUNT_WALLET/BET_CREDIT_INTEGER_AMOUNT",
                List.of(
                        new ValidationMeta("5.08.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.08.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("5.08.03", "wallet/bet_credit", "成功")
                ));
        root.put("5 Validate Credit Win Amount", insufficientFunds5);


        //6 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds6 = new LinkedHashMap<>();
        insufficientFunds6.put("6.1 Credit Amount Wallet / Bet Result [WIN] With Jackpot ONLY",
                List.of(
                        new ValidationMeta("6.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("6.01.03", "wallet/bet_result", "成功")
                ));
        insufficientFunds6.put("6.2 Credit Amount Wallet / Bet Result [WIN] With Jackpot ONLY then [END]",
                List.of(
                        new ValidationMeta("6.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.02.02", "wallet/bet", "成功"),
                        new ValidationMeta("6.02.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("6.02.04", "wallet/bet_result", "成功")
                ));
        insufficientFunds6.put("6.3 Credit Amount Wallet / Bet Result [BET_WIN] With Jackpot ONLY",
                List.of(
                        new ValidationMeta("6.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.03.02", "wallet/bet_result", "成功")

                ));
        insufficientFunds6.put("6.4 Credit Amount Wallet / Bet Result [BET_WIN] With Jackpot ONLY then [END]",
                List.of(
                        new ValidationMeta("6.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.04.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("6.04.03", "wallet/bet_result", "成功")
                ));
        insufficientFunds6.put("6.5 Credit Amount Wallet / Bet Result [BET_WIN] With Jackpot ONLY then [WIN] then [END]",
                List.of(
                        new ValidationMeta("6.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.05.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("6.05.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("6.05.04", "wallet/bet_result", "成功")
                ));

        insufficientFunds6.put("6.6 Credit Amount Wallet / Bet Result [WIN] With Jackpot",
                List.of(
                        new ValidationMeta("6.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.06.02", "wallet/bet", "成功"),
                        new ValidationMeta("6.06.03", "wallet/bet_result", "成功")

                ));
        insufficientFunds6.put("6.7 Credit Amount Wallet / Bet Result [BET_WIN] With Jackpot",
                List.of(
                        new ValidationMeta("6.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.07.02", "wallet/bet_result", "成功")

                ));

        root.put("6 Validate Credit Jackpot Amount", insufficientFunds6);


        //7 验证回滚
        Map<String, List<ValidationMeta>> insufficientFunds7 = new LinkedHashMap<>();
        insufficientFunds7.put("7.1 Rollback Debit Amount Wallet / Bet",
                List.of(
                        new ValidationMeta("7.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("7.01.03", "wallet/rollback", "成功")

                ));
        insufficientFunds7.put("7.2 Rollback Debit Amount Wallet / Bet then [END]",
                List.of(
                        new ValidationMeta("7.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.02.02", "wallet/bet", "成功"),
                        new ValidationMeta("7.02.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("7.02.04", "wallet/rollback", "成功")

                ));
        insufficientFunds7.put("7.3 Rollback Debit Amount Wallet / Bet result [BET_LOSE]",
                List.of(
                        new ValidationMeta("7.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.03.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("7.03.04", "wallet/rollback", "成功")

                ));

        insufficientFunds7.put("7.4 Rollback Debit Amount Wallet / Bet result [BET_WIN]",
                List.of(
                        new ValidationMeta("7.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.04.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("7.04.03", "wallet/rollback", "成功")

                ));
        insufficientFunds7.put("7.5 FISH_ROLLBACK_DEBIT_AMOUNT_WALLET/BET_DEBIT",
                List.of(
                        new ValidationMeta("7.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.05.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("7.05.03", "wallet/rollback", "成功")

                ));

        root.put("7 Validate RollBack", insufficientFunds7);

        //7 验证回滚
        Map<String, List<ValidationMeta>> insufficientFunds8 = new LinkedHashMap<>();
        insufficientFunds8.put("8.1 Idempotent Debit Amount Wallet / Bet",
                List.of(
                        new ValidationMeta("8.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.01.03", "wallet/bet", "成功")

                ));
        insufficientFunds8.put("8.2 Idempotent Wallet / Bet Result [WIN]",
                List.of(
                        new ValidationMeta("8.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.02.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.02.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.02.04", "wallet/bet_result", "成功")

                ));
        insufficientFunds8.put("8.3 Idempotent Wallet / Bet Result [BET_LOSE]",
                List.of(
                        new ValidationMeta("8.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.03.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.03.03", "wallet/bet_result", "成功")

                ));
        insufficientFunds8.put("8.4 Idempotent Wallet / Bet Result [BET_WIN]",
                List.of(
                        new ValidationMeta("8.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.04.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.04.03", "wallet/bet_result", "成功")

                ));
        insufficientFunds8.put("8.5 Idempotent Rollback Debit Amount Wallet / Bet",
                List.of(
                        new ValidationMeta("8.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.05.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.05.03", "wallet/rollback", "成功"),
                        new ValidationMeta("8.05.04", "wallet/rollback", "成功")

                ));
        insufficientFunds8.put("8.6 Idempotent Insufficient Balance Wallet / Bet",
                List.of(
                        new ValidationMeta("8.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.06.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.06.03", "wallet/bet", "成功")

                ));
        insufficientFunds8.put("8.7 Idempotent Insufficient Balance Wallet / Bet Result [BET_WIN]",
                List.of(
                        new ValidationMeta("8.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.07.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.07.03", "wallet/bet_result", "成功")

                ));
        insufficientFunds8.put("8.8 Idempotent Insufficient Balance Wallet / Bet Result [BET_LOSE]",
                List.of(
                        new ValidationMeta("8.08.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.08.02", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.08.03", "wallet/bet_result", "成功")

                ));
        insufficientFunds8.put("8.9 Idempotent Insufficient Balance Wallet / BET",
                List.of(
                        new ValidationMeta("8.09.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.09.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.09.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.09.04", "wallet/adjustment", "成功"),
                        new ValidationMeta("8.09.05", "wallet/adjustment", "成功")

                ));
        insufficientFunds8.put("8.10 Idempotent Insufficient Balance Wallet / Adjustment",
                List.of(
                        new ValidationMeta("8.10.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.10.02", "wallet/bet", "成功"),
                        new ValidationMeta("8.10.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.10.04", "wallet/bet_result", "成功"),
                        new ValidationMeta("8.10.05", "wallet/adjustment", "成功"),
                        new ValidationMeta("8.10.06", "wallet/adjustment", "成功")

                ));
        insufficientFunds8.put("8.11 IDEMPOTENT_FISH_DEBIT_AMOUNT_WALLET/BET_DEBIT",
                List.of(
                        new ValidationMeta("8.11.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.11.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("8.11.03", "wallet/bet_debit", "成功")

                ));
        insufficientFunds8.put("8.12 IDEMPOTENT_INSUFFICIENT_BALANCE_FISH_DEBIT_AMOUNT_WALLET/BET_DEBIT",
                List.of(
                        new ValidationMeta("8.12.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.12.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("8.12.03", "wallet/bet_debit", "成功")

                ));
        insufficientFunds8.put("8.13 IDEMPOTENT_FISH_CREDIT_AMOUNT_WALLET/BET_CREDIT",
                List.of(
                        new ValidationMeta("8.13.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.13.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("8.13.03", "wallet/bet_credit", "成功"),
                        new ValidationMeta("8.13.04", "wallet/bet_credit", "成功")

                ));
        insufficientFunds8.put("8.14 IDEMPOTENT_ROLLBACK_DEBIT_AMOUNT_WALLET/BET_DEBIT",
                List.of(
                        new ValidationMeta("8.14.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.14.02", "wallet/bet_debit", "成功"),
                        new ValidationMeta("8.14.03", "wallet/rollback", "成功"),
                        new ValidationMeta("8.14.04", "wallet/rollback", "成功")

                ));
        root.put("8 Validate Idempotency", insufficientFunds8);

        // 9 验证调整
        Map<String, List<ValidationMeta>> insufficientFunds9 = new LinkedHashMap<>();
        insufficientFunds9.put("9.1 Adjustment Debit Amount Wallet / Bet then [END]",
                List.of(
                        new ValidationMeta("9.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("9.01.02", "wallet/bet", "成功"),
                        new ValidationMeta("9.01.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("9.01.04", "wallet/adjustment", "成功")

                ));
        insufficientFunds9.put("9.2 Adjustment Debit Amount Wallet / Bet [WIN] then [END]",
                List.of(
                        new ValidationMeta("9.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("9.02.02", "wallet/bet", "成功"),
                        new ValidationMeta("9.02.03", "wallet/bet_result", "成功"),
                        new ValidationMeta("9.02.04", "wallet/bet_result", "成功"),
                        new ValidationMeta("9.02.04", "wallet/adjustment", "成功")

                ));

        root.put("9 Validate Adjustment", insufficientFunds9);


        // 10 未找到下注验证
        Map<String, List<ValidationMeta>> insufficientFunds10 = new LinkedHashMap<>();
        insufficientFunds10.put("10.1 Bet Not Found Wallet / Bet Result",
                List.of(
                        new ValidationMeta("10.01.01", "wallet/bet_result", "成功")

                ));
        insufficientFunds10.put("10.2 Bet Not Found Wallet / Rollback",
                List.of(
                        new ValidationMeta("10.02.01", "wallet/rollback", "成功")

                ));
        insufficientFunds10.put("10.3 Bet Not Found Wallet / Adjustment",
                List.of(
                        new ValidationMeta("10.03.01", "wallet/adjustment", "成功")

                ));
        root.put("10 Validate Bet Not Found", insufficientFunds10);


        Map<String, List<ValidationMeta>> insufficientFunds11 = new LinkedHashMap<>();
        insufficientFunds11.put("11.1 Invalid Currency Wallet / Balance",
                List.of(
                        new ValidationMeta("11.01.01", "wallet/balance", "成功")

                ));
        insufficientFunds11.put("11.2 Invalid Currency Wallet / Bet",
                List.of(
                        new ValidationMeta("11.02.01", "wallet/bet", "成功")

                ));
        insufficientFunds11.put("11.3 Invalid Currency Wallet / Bet Result",
                List.of(
                        new ValidationMeta("11.03.01", "wallet/bet_result", "成功")

                ));
        insufficientFunds11.put("11.4 Invalid Currency Wallet / Rollback",
                List.of(
                        new ValidationMeta("11.04.01", "wallet/rollback", "成功")

                ));

        insufficientFunds11.put("11.5 Invalid Currency Wallet / Adjustment",
                List.of(
                        new ValidationMeta("11.05.01", "wallet/adjustment", "成功")

                ));
        insufficientFunds11.put("11.6 INVALID_CURRENCY_WALLET/BET_DEBIT",
                List.of(
                        new ValidationMeta("11.04.01", "wallet/bet_debit", "成功")

                ));
        insufficientFunds11.put("11.7 INVALID_CURRENCY_WALLET/BET_CREDIT",
                List.of(
                        new ValidationMeta("11.04.01", "wallet/bet_credit", "成功")

                ));

        root.put("11 Validate Invalid Currency", insufficientFunds11);
        return root;
    }



    public static Map<String, Map<String, List<ValidationMeta>>> buildSport() {

        Map<String, Map<String, List<ValidationMeta>>> root = new LinkedHashMap<>();

        // ===============================
        // 1. 验证无效签章
        // ===============================
        Map<String, List<ValidationMeta>> invalidSignature = new LinkedHashMap<>();

        invalidSignature.put("1.1 Invalid Signature Sports / Balance",
                List.of(new ValidationMeta("1.01.01", "sports/balance", "成功")));

        invalidSignature.put("1.2 Invalid Signature Sports / Bet",
                List.of(new ValidationMeta("1.02.01", "sports/bet", "成功")));

        invalidSignature.put("1.3 Invalid Signature Sports / Update Bet",
                List.of(new ValidationMeta("1.03.01", "sports/update-bet", "成功")));

        invalidSignature.put("1.4 Invalid Signature Sports / Settle",
                List.of(new ValidationMeta("1.04.01", "sports/settled", "成功")));

        invalidSignature.put("1.5 Invalid Signature Sports / Refund",
                List.of(new ValidationMeta("1.05.01", "sports/refund", "成功")));
        invalidSignature.put("1.6 Invalid Signature Sports / Adjustment",
                List.of(new ValidationMeta("1.06.01", "sports/adjustment", "成功")));
        invalidSignature.put("1.7 Invalid Signature Sports / Resettle",
                List.of(new ValidationMeta("1.07.01", "sports/resettle", "成功")));


        root.put("1 Validate Invalid Signature", invalidSignature);

        // ===============================
        // 2. 验证无效用户名
        // ===============================
        Map<String, List<ValidationMeta>> invalidUsername = new LinkedHashMap<>();

        invalidUsername.put("2.1 Invalid Username Sports / Balance",
                List.of(new ValidationMeta("2.01.01", "wallet/balance", "成功")));

        invalidUsername.put("2.2 Invalid Username Sports / Bet",
                List.of(new ValidationMeta("2.02.01", "sports/bet", "成功")));

        invalidUsername.put("2.3 Invalid Username Sports / Update Bet",
                List.of(new ValidationMeta("2.03.01", "sports/update-bet", "成功")));

        invalidUsername.put("2.4 Invalid Username Sports / Settle",
                List.of(new ValidationMeta("2.04.01", "sports/settled", "成功")));

        invalidUsername.put("2.5 Invalid Username Sports / Refund",
                List.of(new ValidationMeta("2.05.01", "sports/refund", "成功")));
        invalidUsername.put("2.6 Invalid Username Sports / Resettle",
                List.of(new ValidationMeta("2.06.01", "sports/resettle", "成功")));

        invalidUsername.put("2.7 Invalid Username Sports / Adjustment",
                List.of(new ValidationMeta("2.07.01", "sports/adjustment", "成功")));

        root.put("2 Validate Invalid Username", invalidUsername);

        // ===============================
        // 3. 钱包余额不足
        // ===============================
        Map<String, List<ValidationMeta>> insufficientFunds = new LinkedHashMap<>();

        insufficientFunds.put("3.1 Invalid Username Sports / Bet",
                List.of(
                        new ValidationMeta("3.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.01.02", "sports/bet", "成功")
                ));

        insufficientFunds.put("3.2 Invalid Username Sports / Resettle",
                List.of(
                        new ValidationMeta("3.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.02.02", "sports/bet", "成功"),
                        new ValidationMeta("3.02.03", "sports/update-bet", "成功"),
                        new ValidationMeta("3.02.04", "sports/settled", "成功"),
                        new ValidationMeta("3.02.05", "wallet/balance", "成功"),
                        new ValidationMeta("3.02.06", "sports/bet", "成功"),
                        new ValidationMeta("3.02.07", "sports/resettle", "成功"),
                        new ValidationMeta("3.02.08", "sports/adjustment", "成功")
                ));
        insufficientFunds.put("3.3 Invalid Username Sports / Adjustment",
                List.of(
                        new ValidationMeta("3.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.03.02", "sports/adjustment", "成功")
                ));

        insufficientFunds.put("3.4 Invalid Username Sports / Unsettle",
                List.of(
                        new ValidationMeta("3.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("3.02.02", "sports/bet", "成功"),
                        new ValidationMeta("3.02.03", "sports/update-bet", "成功"),
                        new ValidationMeta("3.02.04", "sports/settled", "成功"),
                        new ValidationMeta("3.02.05", "wallet/balance", "成功"),
                        new ValidationMeta("3.02.06", "sports/bet", "成功"),
                        new ValidationMeta("3.02.07", "sports/resettle", "成功"),
                        new ValidationMeta("3.02.08", "sports/adjustment", "成功")
                ));


        root.put("3 Validate Insufficient Balance", insufficientFunds);
        //4 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds4 = new LinkedHashMap<>();
        insufficientFunds4.put("4.1 Normal Bet Debit Amount Sports / Bet Then [LOSE]",
                List.of(
                        new ValidationMeta("4.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.01.02", "sports/bet", "成功"),
                        new ValidationMeta("4.01.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.01.04", "sports/settled", "成功")
                ));
        insufficientFunds4.put("4.2 Parlay Bet Debit Amount Sports / Bet Then [LOSE]",
                List.of(
                        new ValidationMeta("4.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.02.02", "sports/bet", "成功"),
                        new ValidationMeta("4.02.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.02.04", "sports/settled", "成功")
                ));

        insufficientFunds4.put("4.3 Parlay Multiple Bet Debit Amount Sports / Bet Then [LOSE]",
                List.of(
                        new ValidationMeta("4.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.03.02", "sports/bet", "成功"),
                        new ValidationMeta("4.03.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.03.04", "sports/update-bet", "成功"),
                        new ValidationMeta("4.03.05", "sports/update-bet", "成功"),
                        new ValidationMeta("4.03.06", "sports/update-bet", "成功"),
                        new ValidationMeta("4.03.07", "sports/settled", "成功"),
                        new ValidationMeta("4.03.08", "sports/settled", "成功"),
                        new ValidationMeta("4.03.09", "sports/settled", "成功"),
                        new ValidationMeta("4.03.10", "sports/settled", "成功")
                ));
        insufficientFunds4.put("4.4 Normal Bet Debit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("4.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.04.02", "sports/bet", "成功"),
                        new ValidationMeta("4.04.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.04.04", "sports/settled", "成功"),
                        new ValidationMeta("4.04.05", "sports/unsettle", "成功")
                ));
        insufficientFunds4.put("4.5 Parlay Bet Debit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("4.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.05.02", "sports/bet", "成功"),
                        new ValidationMeta("4.05.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.05.04", "sports/settled", "成功"),
                        new ValidationMeta("4.05.05", "sports/unsettle", "成功")
                ));
        insufficientFunds4.put("4.6 Parlay Multiple Bet Debit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("4.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.06.02", "sports/bet", "成功"),
                        new ValidationMeta("4.06.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.06.04", "sports/update-bet", "成功"),
                        new ValidationMeta("4.06.05", "sports/update-bet", "成功"),
                        new ValidationMeta("4.06.06", "sports/update-bet", "成功"),
                        new ValidationMeta("4.06.07", "sports/settled", "成功"),
                        new ValidationMeta("4.06.08", "sports/settled", "成功"),
                        new ValidationMeta("4.06.09", "sports/settled", "成功"),
                        new ValidationMeta("4.06.10", "sports/settled", "成功"),
                        new ValidationMeta("4.06.11", "sports/unsettle", "成功"),
                        new ValidationMeta("4.06.12", "sports/unsettle", "成功"),
                        new ValidationMeta("4.06.13", "sports/unsettle", "成功"),
                        new ValidationMeta("4.06.14", "sports/unsettle", "成功")
                ));
        insufficientFunds4.put("4.7 Normal Bet Debit Amount Sports / Bet > Lose > Resettle",
                List.of(
                        new ValidationMeta("4.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.07.02", "sports/bet", "成功"),
                        new ValidationMeta("4.07.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.07.04", "sports/settled", "成功"),
                        new ValidationMeta("4.07.05", "sports/resettle", "成功")
                ));

        insufficientFunds4.put("4.8 Parlay Bet Debit Amount Sports / Bet > Lose > Resettle",
                List.of(
                        new ValidationMeta("4.08.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.08.02", "sports/bet", "成功"),
                        new ValidationMeta("4.08.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.08.04", "sports/settled", "成功"),
                        new ValidationMeta("4.08.05", "sports/resettle", "成功")
                ));
        insufficientFunds4.put("4.9 Parlay Multiple Bet Debit Amount Sports / Bet > Lose > Resettle",
                List.of(
                        new ValidationMeta("4.09.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.09.02", "sports/bet", "成功"),
                        new ValidationMeta("4.09.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.09.04", "sports/update-bet", "成功"),
                        new ValidationMeta("4.09.05", "sports/update-bet", "成功"),
                        new ValidationMeta("4.09.06", "sports/update-bet", "成功"),
                        new ValidationMeta("4.09.07", "sports/settled", "成功"),
                        new ValidationMeta("4.09.08", "sports/settled", "成功"),
                        new ValidationMeta("4.09.09", "sports/settled", "成功"),
                        new ValidationMeta("4.09.10", "sports/settled", "成功"),
                        new ValidationMeta("4.09.11", "sports/resettle", "成功"),
                        new ValidationMeta("4.09.12", "sports/resettle", "成功"),
                        new ValidationMeta("4.09.13", "sports/resettle", "成功"),
                        new ValidationMeta("4.09.14", "sports/resettle", "成功")
                ));
        insufficientFunds4.put("4.10 NORMAL_BET_DEBIT_AMOUNT_SPORTS/BET > LOSE > UNSETTLE > LOSE",
                List.of(
                        new ValidationMeta("4.10.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.10.02", "sports/bet", "成功"),
                        new ValidationMeta("4.10.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.10.04", "sports/settled", "成功"),
                        new ValidationMeta("4.10.05", "sports/unsettle", "成功"),
                        new ValidationMeta("4.10.06", "sports/settled", "成功")
                ));
        insufficientFunds4.put("4.11 PARLAY_BET_DEBIT_AMOUNT_SPORTS/BET > LOSE > UNSETTLE > LOSE",
                List.of(
                        new ValidationMeta("4.11.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.11.02", "sports/bet", "成功"),
                        new ValidationMeta("4.11.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.11.04", "sports/settled", "成功"),
                        new ValidationMeta("4.11.05", "sports/unsettle", "成功"),
                        new ValidationMeta("4.11.06", "sports/settled", "成功")
                ));

        insufficientFunds4.put("4.12 PARLAY_MULTIPLE_BET_DEBIT_AMOUNT_SPORTS/BET > LOSE > UNSETTLE > LOSE",
                List.of(
                        new ValidationMeta("4.12.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.12.02", "sports/bet", "成功"),
                        new ValidationMeta("4.12.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.12.04", "sports/update-bet", "成功"),
                        new ValidationMeta("4.12.05", "sports/update-bet", "成功"),
                        new ValidationMeta("4.12.06", "sports/update-bet", "成功"),
                        new ValidationMeta("4.12.07", "sports/settled", "成功"),
                        new ValidationMeta("4.12.08", "sports/settled", "成功"),
                        new ValidationMeta("4.12.09", "sports/settled", "成功"),
                        new ValidationMeta("4.12.10", "sports/settled", "成功"),
                        new ValidationMeta("4.12.11", "sports/unsettle", "成功"),
                        new ValidationMeta("4.12.12", "sports/unsettle", "成功"),
                        new ValidationMeta("4.12.13", "sports/unsettle", "成功"),
                        new ValidationMeta("4.12.14", "sports/unsettle", "成功"),
                        new ValidationMeta("4.12.15", "sports/settled", "成功"),
                        new ValidationMeta("4.12.16", "sports/settled", "成功"),
                        new ValidationMeta("4.12.17", "sports/settled", "成功"),
                        new ValidationMeta("4.12.18", "sports/settled", "成功")
                ));
        insufficientFunds4.put("4.13 CHANGE_LOWER_BET_AMOUNT_AT_UPDATE_BET",
                List.of(
                        new ValidationMeta("4.13.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.13.02", "sports/bet", "成功"),
                        new ValidationMeta("4.13.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.13.04", "sports/settled", "成功")
                ));

        insufficientFunds4.put("4.14 PARLAY_MULTIPLE_BET_CHANGE_LOWER_BET_AMOUNT_AT_UPDATE-BET",
                List.of(
                        new ValidationMeta("4.14.01", "wallet/balance", "成功"),
                        new ValidationMeta("4.14.02", "sports/bet", "成功"),
                        new ValidationMeta("4.14.03", "sports/update-bet", "成功"),
                        new ValidationMeta("4.14.04", "sports/update-bet", "成功"),
                        new ValidationMeta("4.14.05", "sports/update-bet", "成功"),
                        new ValidationMeta("4.14.06", "sports/update-bet", "成功"),
                        new ValidationMeta("4.14.07", "sports/settled", "成功"),
                        new ValidationMeta("4.14.08", "sports/settled", "成功"),
                        new ValidationMeta("4.14.09", "sports/settled", "成功"),
                        new ValidationMeta("4.14.10", "sports/settled", "成功")
                ));

        root.put("4 Validate Debit Amount", insufficientFunds4);

        //5 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds5 = new LinkedHashMap<>();
        insufficientFunds5.put("5.1 Normal Bet Credit Amount Sports / Bet Result [WIN]",
                List.of(
                        new ValidationMeta("5.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.01.02", "sports/bet", "成功"),
                        new ValidationMeta("5.01.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.01.04", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.2 Parlay Bet Credit Amount Sports / Bet Then [WIN]",
                List.of(
                        new ValidationMeta("5.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.02.02", "sports/bet", "成功"),
                        new ValidationMeta("5.02.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.02.04", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.3 Parlay Multiple Bet Credit Amount Sports / Bet Then [Lose]",
                List.of(
                        new ValidationMeta("5.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.03.02", "sports/bet", "成功"),
                        new ValidationMeta("5.03.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.03.04", "sports/update-bet", "成功"),
                        new ValidationMeta("5.03.05", "sports/update-bet", "成功"),
                        new ValidationMeta("5.03.06", "sports/update-bet", "成功"),
                        new ValidationMeta("5.03.07", "sports/settled", "成功"),
                        new ValidationMeta("5.03.08", "sports/settled", "成功"),
                        new ValidationMeta("5.03.09", "sports/settled", "成功"),
                        new ValidationMeta("5.03.10", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.4 Normal Bet Credit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("5.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.04.02", "sports/bet", "成功"),
                        new ValidationMeta("5.04.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.04.04", "sports/settled", "成功"),
                        new ValidationMeta("5.04.05", "sports/unsettle", "成功")
                ));
        insufficientFunds5.put("5.5 Parlay Bet Credit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("5.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.05.02", "sports/bet", "成功"),
                        new ValidationMeta("5.05.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.05.04", "sports/settled", "成功"),
                        new ValidationMeta("5.05.05", "sports/unsettle", "成功")
                ));
        insufficientFunds5.put("5.6 Parlay Multiple Bet Credit Amount Sports / Bet > Lose > Unsettle",
                List.of(
                        new ValidationMeta("5.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.06.02", "sports/bet", "成功"),
                        new ValidationMeta("5.06.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.06.04", "sports/update-bet", "成功"),
                        new ValidationMeta("5.06.05", "sports/update-bet", "成功"),
                        new ValidationMeta("5.06.06", "sports/update-bet", "成功"),
                        new ValidationMeta("5.06.07", "sports/settled", "成功"),
                        new ValidationMeta("5.06.08", "sports/settled", "成功"),
                        new ValidationMeta("5.06.09", "sports/settled", "成功"),
                        new ValidationMeta("5.06.10", "sports/settled", "成功"),
                        new ValidationMeta("5.06.11", "sports/unsettle", "成功"),
                        new ValidationMeta("5.06.12", "sports/unsettle", "成功"),
                        new ValidationMeta("5.06.13", "sports/unsettle", "成功"),
                        new ValidationMeta("5.06.14", "sports/unsettle", "成功")
                ));

        insufficientFunds5.put("5.7 NORMAL_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > RESETTLE",
                List.of(
                        new ValidationMeta("5.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.07.02", "sports/bet", "成功"),
                        new ValidationMeta("5.07.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.07.04", "sports/settled", "成功"),
                        new ValidationMeta("5.07.05", "sports/resettle", "成功")
                ));
        insufficientFunds5.put("5.8 PARLAY_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > RESETTLE",
                List.of(
                        new ValidationMeta("5.08.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.08.02", "sports/bet", "成功"),
                        new ValidationMeta("5.08.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.08.04", "sports/settled", "成功"),
                        new ValidationMeta("5.08.05", "sports/resettle", "成功")
                ));
        insufficientFunds5.put("5.9 PARLAY_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > RESETTLE",
                List.of(
                        new ValidationMeta("5.09.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.09.02", "sports/bet", "成功"),
                        new ValidationMeta("5.09.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.09.04", "sports/update-bet", "成功"),
                        new ValidationMeta("5.09.05", "sports/update-bet", "成功"),
                        new ValidationMeta("5.09.06", "sports/update-bet", "成功"),
                        new ValidationMeta("5.09.07", "sports/settled", "成功"),
                        new ValidationMeta("5.09.08", "sports/settled", "成功"),
                        new ValidationMeta("5.09.09", "sports/settled", "成功"),
                        new ValidationMeta("5.09.10", "sports/settled", "成功"),
                        new ValidationMeta("5.09.11", "sports/resettle", "成功"),
                        new ValidationMeta("5.09.12", "sports/resettle", "成功"),
                        new ValidationMeta("5.09.13", "sports/resettle", "成功"),
                        new ValidationMeta("5.09.14", "sports/resettle", "成功")
                ));
        insufficientFunds5.put("5.10 NORMAL_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > UNSETTLE > WIN",
                List.of(
                        new ValidationMeta("5.10.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.10.02", "sports/bet", "成功"),
                        new ValidationMeta("5.10.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.10.04", "sports/settled", "成功"),
                        new ValidationMeta("5.10.05", "sports/unsettle", "成功"),
                        new ValidationMeta("5.10.06", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.11 NORMAL_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > UNSETTLE > WIN",
                List.of(
                        new ValidationMeta("5.11.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.11.02", "sports/bet", "成功"),
                        new ValidationMeta("5.11.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.11.04", "sports/settled", "成功"),
                        new ValidationMeta("5.11.05", "sports/unsettle", "成功"),
                        new ValidationMeta("5.11.06", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.12 PARLAY_MULTIPLE_BET_DEBIT_AMOUNT_SPORTS/BET > WIN > UNSETTLE > WIN",
                List.of(
                        new ValidationMeta("5.12.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.12.02", "sports/bet", "成功"),
                        new ValidationMeta("5.12.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.12.04", "sports/update-bet", "成功"),
                        new ValidationMeta("5.12.05", "sports/update-bet", "成功"),
                        new ValidationMeta("5.12.06", "sports/update-bet", "成功"),
                        new ValidationMeta("5.12.07", "sports/settled", "成功"),
                        new ValidationMeta("5.12.08", "sports/settled", "成功"),
                        new ValidationMeta("5.12.09", "sports/settled", "成功"),
                        new ValidationMeta("5.12.10", "sports/settled", "成功"),
                        new ValidationMeta("5.12.11", "sports/unsettle", "成功"),
                        new ValidationMeta("5.12.12", "sports/unsettle", "成功"),
                        new ValidationMeta("5.12.13", "sports/unsettle", "成功"),
                        new ValidationMeta("5.12.14", "sports/unsettle", "成功"),
                        new ValidationMeta("5.12.15", "sports/settled", "成功"),
                        new ValidationMeta("5.12.16", "sports/settled", "成功"),
                        new ValidationMeta("5.12.17", "sports/settled", "成功"),
                        new ValidationMeta("5.12.18", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.13 CHANGE_LOWER_BET_AMOUNT_AT_UPDATE-BET",
                List.of(
                        new ValidationMeta("5.13.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.13.02", "sports/bet", "成功"),
                        new ValidationMeta("5.13.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.13.04", "sports/settled", "成功")
                ));
        insufficientFunds5.put("5.14 CHANGE_LOWER_BET_AMOUNT_AT_UPDATE-BET",
                List.of(
                        new ValidationMeta("5.14.01", "wallet/balance", "成功"),
                        new ValidationMeta("5.14.02", "sports/bet", "成功"),
                        new ValidationMeta("5.14.03", "sports/update-bet", "成功"),
                        new ValidationMeta("5.14.04", "sports/update-bet", "成功"),
                        new ValidationMeta("5.14.05", "sports/update-bet", "成功"),
                        new ValidationMeta("5.14.06", "sports/update-bet", "成功"),
                        new ValidationMeta("5.14.07", "sports/settled", "成功"),
                        new ValidationMeta("5.14.08", "sports/settled", "成功"),
                        new ValidationMeta("5.14.09", "sports/settled", "成功"),
                        new ValidationMeta("5.14.10", "sports/settled", "成功")
                ));

        root.put("5 Validate Credit Win Amount", insufficientFunds5);


        //6 验证借记金额
        Map<String, List<ValidationMeta>> insufficientFunds6 = new LinkedHashMap<>();
        insufficientFunds6.put("6.1 Normal Bet > Confirm Bet > Refund",
                List.of(
                        new ValidationMeta("6.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.01.02", "sports/bet", "成功"),
                        new ValidationMeta("6.01.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.01.04", "sports/refund", "成功")
                ));
        insufficientFunds6.put("6.2 Parlay Bet > Confirm Bet > Refund",
                List.of(
                        new ValidationMeta("6.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.02.02", "sports/bet", "成功"),
                        new ValidationMeta("6.02.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.02.04", "sports/refund", "成功")
                ));
        insufficientFunds6.put("6.3 Parlay Multiple Bet > Confirm Bet > Refund",
                List.of(
                        new ValidationMeta("6.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.03.02", "sports/bet", "成功"),
                        new ValidationMeta("6.03.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.03.04", "sports/update-bet", "成功"),
                        new ValidationMeta("6.03.05", "sports/update-bet", "成功"),
                        new ValidationMeta("6.03.06", "sports/update-bet", "成功"),
                        new ValidationMeta("6.03.07", "sports/refund", "成功"),
                        new ValidationMeta("6.03.08", "sports/refund", "成功"),
                        new ValidationMeta("6.03.09", "sports/refund", "成功"),
                        new ValidationMeta("6.03.10", "sports/refund", "成功")

                ));
        insufficientFunds6.put("6.4 NORMAL_BET > REFUND",
                List.of(
                        new ValidationMeta("6.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.04.02", "sports/bet", "成功"),
                        new ValidationMeta("6.04.03", "sports/refund", "成功")
                ));
        insufficientFunds6.put("6.5 PARLAY_BET > REFUND",
                List.of(
                        new ValidationMeta("6.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.05.02", "sports/bet", "成功"),
                        new ValidationMeta("6.05.03", "sports/refund", "成功")
                ));

        insufficientFunds6.put("6.6 Parlay Multiple Bet > Refund",
                List.of(
                        new ValidationMeta("6.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.06.02", "sports/bet", "成功"),
                        new ValidationMeta("6.06.03", "sports/refund", "成功"),
                        new ValidationMeta("6.06.04", "sports/refund", "成功"),
                        new ValidationMeta("6.06.05", "sports/refund", "成功"),
                        new ValidationMeta("6.06.06", "sports/refund", "成功")

                ));
        insufficientFunds6.put("6.7 NORMAL_BET_REFUND > UNSETTLE > SETTLE",
                List.of(
                        new ValidationMeta("6.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.07.02", "sports/bet", "成功"),
                        new ValidationMeta("6.07.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.07.04", "sports/refund", "成功"),
                        new ValidationMeta("6.07.05", "sports/unsettle", "成功"),
                        new ValidationMeta("6.07.06", "sports/settled", "成功")

                ));
        insufficientFunds6.put("6.8 PARLAY_BET_REFUND > UNSETTLE > SETTLE",
                List.of(
                        new ValidationMeta("6.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.07.02", "sports/bet", "成功"),
                        new ValidationMeta("6.07.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.07.04", "sports/refund", "成功"),
                        new ValidationMeta("6.07.05", "sports/unsettle", "成功"),
                        new ValidationMeta("6.07.06", "sports/settled", "成功")

                ));
        insufficientFunds6.put("6.9 PARLAY_MULTIPLE_BET_REFUND > UNSETTLE > SETTLE",
                List.of(
                        new ValidationMeta("6.09.01", "wallet/balance", "成功"),
                        new ValidationMeta("6.09.02", "sports/bet", "成功"),
                        new ValidationMeta("6.09.03", "sports/update-bet", "成功"),
                        new ValidationMeta("6.09.04", "sports/update-bet", "成功"),
                        new ValidationMeta("6.09.05", "sports/update-bet", "成功"),
                        new ValidationMeta("6.09.06", "sports/update-bet", "成功"),
                        new ValidationMeta("6.09.07", "sports/refund", "成功"),
                        new ValidationMeta("6.09.08", "sports/refund", "成功"),
                        new ValidationMeta("6.09.09", "sports/refund", "成功"),
                        new ValidationMeta("6.09.10", "sports/refund", "成功"),
                        new ValidationMeta("6.09.11", "sports/unsettle", "成功"),
                        new ValidationMeta("6.09.12", "sports/unsettle", "成功"),
                        new ValidationMeta("6.09.13", "sports/unsettle", "成功"),
                        new ValidationMeta("6.09.14", "sports/unsettle", "成功"),
                        new ValidationMeta("6.09.15", "sports/settled", "成功"),
                        new ValidationMeta("6.09.16", "sports/settled", "成功"),
                        new ValidationMeta("6.09.17", "sports/settled", "成功"),
                        new ValidationMeta("6.09.18", "sports/settled", "成功")

                ));


        root.put("6 Validate Refund", insufficientFunds6);


        //7 验证幂等性
        Map<String, List<ValidationMeta>> insufficientFunds7 = new LinkedHashMap<>();
        insufficientFunds7.put("7.1 Idempotent Debit Amount Sports / Bet",
                List.of(
                        new ValidationMeta("7.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.01.02", "sports/bet", "成功"),
                        new ValidationMeta("7.01.03", "sports/bet", "成功")

                ));
        insufficientFunds7.put("7.2 Idempotent Debit Amount Sports / Bet Single Parlay",
                List.of(
                        new ValidationMeta("7.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.02.02", "sports/bet", "成功"),
                        new ValidationMeta("7.02.03", "sports/bet", "成功")

                ));
        insufficientFunds7.put("7.3 Idempotent Debit Amount Sports / Bet Multiple Parlay",
                List.of(
                        new ValidationMeta("7.03.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.03.02", "sports/bet", "成功"),
                        new ValidationMeta("7.03.03", "sports/bet", "成功")

                ));

        insufficientFunds7.put("7.4 Idempotent Debit Amount Sports / Update Bet",
                List.of(
                        new ValidationMeta("7.04.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.04.02", "sports/bet", "成功"),
                        new ValidationMeta("7.04.03", "sports/update-bet", "成功"),
                        new ValidationMeta("7.04.04", "sports/update-bet", "成功")

                ));
        insufficientFunds7.put("7.5 Idempotent Debit Amount Sports / Settle",
                List.of(
                        new ValidationMeta("7.05.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.05.02", "sports/bet", "成功"),
                        new ValidationMeta("7.05.03", "sports/update-bet", "成功"),
                        new ValidationMeta("7.05.04", "sports/settled", "成功"),
                        new ValidationMeta("7.05.05", "sports/settled", "成功")

                ));
        insufficientFunds7.put("7.6 Idempotent Debit Amount Sports / Refund",
                List.of(
                        new ValidationMeta("7.06.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.06.02", "sports/bet", "成功"),
                        new ValidationMeta("7.06.03", "sports/update-bet", "成功"),
                        new ValidationMeta("7.06.04", "sports/refund", "成功"),
                        new ValidationMeta("7.06.05", "sports/refund", "成功")

                ));
        insufficientFunds7.put("7.7 Idempotent Debit Amount Sports / Resettle",
                List.of(
                        new ValidationMeta("7.07.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.07.02", "sports/bet", "成功"),
                        new ValidationMeta("7.07.03", "sports/update-bet", "成功"),
                        new ValidationMeta("7.07.04", "sports/settled", "成功"),
                        new ValidationMeta("7.07.05", "sports/resettle", "成功"),
                        new ValidationMeta("7.07.06", "sports/resettle", "成功")

                ));
        insufficientFunds7.put("7.8 IDEMPOTENT_DEBIT_AMOUNT_SPORTS/UNSETTLE",
                List.of(
                        new ValidationMeta("7.08.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.08.02", "sports/bet", "成功"),
                        new ValidationMeta("7.08.03", "sports/update-bet", "成功"),
                        new ValidationMeta("7.08.04", "sports/settled", "成功"),
                        new ValidationMeta("7.08.05", "sports/unsettle", "成功"),
                        new ValidationMeta("7.08.06", "sports/unsettle", "成功")

                ));
        insufficientFunds7.put("7.9 IDEMPOTENT_DEBIT_AMOUNT_SPORTS/ADJUSTMENT",
                List.of(
                        new ValidationMeta("7.09.01", "wallet/balance", "成功"),
                        new ValidationMeta("7.09.02", "sports/adjustment", "成功"),
                        new ValidationMeta("7.09.03", "sports/adjustment", "成功")

                ));


        root.put("7 Validate Idempotency", insufficientFunds7);

        //8 验证调整
        Map<String, List<ValidationMeta>> insufficientFunds8 = new LinkedHashMap<>();
        insufficientFunds8.put("8.1 Adjustment Increase",
                List.of(
                        new ValidationMeta("8.01.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.01.02", "sports/adjustment", "成功")

                ));
        insufficientFunds8.put("8.2 Adjustment Decrease",
                List.of(
                        new ValidationMeta("8.02.01", "wallet/balance", "成功"),
                        new ValidationMeta("8.02.02", "sports/adjustment", "成功")

                ));

        root.put("8 Validate Adjustment", insufficientFunds8);

        // 9 未找到下注验证
        Map<String, List<ValidationMeta>> insufficientFunds9 = new LinkedHashMap<>();
        insufficientFunds9.put("9.1 BET_NOT_FOUND_SPOTS/UPDATE_BET",
                List.of(
                        new ValidationMeta("9.01.01", "sports/update-bet", "成功")


                ));
        insufficientFunds9.put("9.2 BET_NOT_FOUND_SPOTS/SETTLE",
                List.of(
                        new ValidationMeta("9.02.01", "sports/settled", "成功")

                ));
        insufficientFunds9.put("9.3 BET_NOT_FOUND_SPOTS/REFUND",
                List.of(
                        new ValidationMeta("9.03.01", "sports/refund", "成功")

                ));
        insufficientFunds9.put("9.4 BET_NOT_FOUND_SPOTS/UNSETTLE",
                List.of(
                        new ValidationMeta("9.04.01", "sports/unsettle", "成功")

                ));
        insufficientFunds9.put("9.5 BET_NOT_FOUND_SPOTS/RESETTLE",
                List.of(
                        new ValidationMeta("9.05.01", "sports/resettle", "成功")

                ));

        root.put("9 Validate Bet Not Found", insufficientFunds9);

        return root;
    }
}