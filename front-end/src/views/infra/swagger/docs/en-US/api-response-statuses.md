# API Response Statuses

<table><thead><tr><th width="313">Status Code</th><th>Description</th></tr></thead><tbody><tr><td>OK</td><td>Successful response.</td></tr><tr><td>UNKNOWN_ERROR</td><td>Generic status code for unknown errors.</td></tr><tr><td>INVALID_REQUEST</td><td>Wrong/missing parameters sent in request body.</td></tr><tr><td>AUTHENTICATION_FAILED</td><td>Authentication failed. X-API-Key is missing or invalid.</td></tr><tr><td>INVALID_SIGNATURE</td><td>X-Signature verification failed.</td></tr><tr><td>INVALID_TOKEN</td><td>Invalid token on Operator's system.</td></tr><tr><td>INVALID_GAME</td><td>Not a valid game.</td></tr><tr><td>DUPLICATE_REQUEST</td><td>Duplicate request.</td></tr><tr><td>CURRENCY_NOT_SUPPORTED</td><td>Currency is not supported.</td></tr><tr><td>WRONG_CURRENCY</td><td>Transaction's currency is different from user's wallet currency.</td></tr><tr><td>INSUFFICIENT_FUNDS</td><td>User's wallet does not have enough funds.</td></tr><tr><td>USER_NOT_EXISTS</td><td>User does not exists in Operator's system</td></tr><tr><td>USER_DISABLED</td><td>User is disabled and not allowed to place bets.</td></tr><tr><td>TRANSACTION_DUPLICATED</td><td>Duplicate transaction Id was sent.</td></tr><tr><td>TRANSACTION_NOT_EXISTS</td><td>Corresponding bet transaction cannot be found.</td></tr><tr><td>VENDOR_ERROR</td><td>Error encountered on game vendor</td></tr><tr><td>UNDER_MAINTENANCE</td><td>Game is under maintenance.</td></tr><tr><td>MISMATCHED_DATA_TYPE</td><td>Invalid data type.</td></tr><tr><td>INVALID_RESPONSE</td><td>Invalid response.</td></tr><tr><td>INVALID_VENDOR</td><td>Vendor is not supported</td></tr><tr><td>INVALID_LANGUAGE</td><td>Language is not supported.</td></tr><tr><td>GAME_DISABLED</td><td>Game is disabled.</td></tr><tr><td>INVALID_PLATFORM</td><td>Platform is not supported.</td></tr><tr><td>GAME_LANGUAGE_NOT_SUPPORTED</td><td>Game language is not supported.</td></tr><tr><td>GAME_PLATFORM_NOT_SUPPORTED</td><td>Game platform is not supported.</td></tr><tr><td>GAME_CURRENCY_NOT_SUPPORTED</td><td>Game currency is not supported.</td></tr><tr><td>VENDOR_LINE_DISABLED</td><td>Vendor line is disabled.</td></tr><tr><td>VENDOR_CURRENCY_NOT_SUPPORTED</td><td>Vendor currency is not supported.</td></tr><tr><td>VENDOR_LANGUAGE_NOT_SUPPORTED</td><td>Vendor language is not supported.</td></tr><tr><td>VENDOR_PLATFORM_NOT_SUPPORTED</td><td>Vendor platform is not supported.</td></tr><tr><td>TRANSACTION_STILL_PROCESSING</td><td>Transaction is still processing, please retry.</td></tr><tr><td>EXCEEDED_NUMBER_OF_RETRIES</td><td>Exceeded number of retries.</td></tr><tr><td>OPERATOR_TIMEOUT</td><td>Operator timed out</td></tr><tr><td>INVALID_FROM_TIME</td><td>Data only available last 60 days</td></tr><tr><td>INVALID_DATE_RANGE</td><td>Date range should be within one day.</td></tr><tr><td>REFERENCE_ID_DUPLICATED</td><td>Duplicate reference Id was sent.</td></tr><tr><td>TRANSACTION_DOES_NOT_EXIST</td><td>Corresponding reference Id cannot be found.</td></tr><tr><td>INTERNAL_ERROR</td><td>Internal error. please checked in relevant support channel</td></tr><tr><td>WALLET_NOT_SUPPORTED</td><td>Wallet Type is not supported.</td></tr></tbody></table>

<details>

<summary>Sample response format</summary>

```json
{
    "traceId": "e62ceec8-1c18-48fc-af0e-9f9f943b2554",
    "status": "INVALID_REQUEST",
    "message": "Wrong/missing parameters sent in request body.",
    "validation": {
        "platform": "Platform is not supported."
    }
}
```

</details>


# Endpoint Compatibility Guide for Integrated Products

| API Endpoint              | Supported Products                                                                                                                                                                                  |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `/wallet/bet_result` only | <ul><li><strong>PG Soft</strong> </li><li><strong>Booongo</strong></li><li><strong>JILI</strong></li><li><strong>ILoveU</strong></li><li><strong>CP Game</strong></li></ul>                         |
| `/wallet/adjustment`      | <ul><li><strong>PragmaticPlay</strong> Live Casino Game</li></ul>                                                                                                                                   |
| `/wallet/bet_debit`       | <ul><li><strong>CQ9</strong> Fish Game</li><li><strong>Spadegaming</strong> Fish Game (<em>coming soon</em>)</li><li><strong>King Midas</strong>' Ludo Game (<em>coming soon</em>)</li></ul>        |
| `/wallet/bet_credit`      | <p></p><ul><li><strong>CQ9</strong> Fish Game</li><li><strong>Spadegaming</strong> Fish Game (<em>coming soon</em>)</li><li><strong>King Midas</strong>' Ludo Game (<em>coming soon</em>)</li></ul> |
|                           |                                                                                                                                                                                                     |