# Wallet API

## Retrieve user's latest balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/balance`

Implemented by Operator. The Game Aggregator will call this endpoint to retrieve the user's latest wallet balance.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                       | Type   | Description                                                                                           |
| ------------------------------------------ | ------ | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>  | String | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| username<mark style="color:red;">\*</mark> | String | The username of the user in the Operator's system.                                                    |
| currency<mark style="color:red;">\*</mark> | String | ISO-4217 currency code (eg. USD)                                                                      |
| timestamp<mark style="color:red;">\*</mark>             | Number  | Unix timestamp of this transaction in milliseconds.                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}

{% tab title="200: OK User does not exists" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "USER_NOT_EXISTS"
}
```

{% endtab %}

{% tab title="200: OK Invalid token" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INVALID_TOKEN"
}
```

{% endtab %}

{% tab title="200: OK Invalid signature" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INVALID_SIGNATURE"
}
```

{% endtab %}

{% tab title="200: OK Wrong currency" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "WRONG_CURRENCY"
}
```

{% endtab %}

{% tab title="200: OK Invalid request" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "WRONG_PARAMETERS"
}
```

{% endtab %}
{% endtabs %}

## A bet transaction to deduct amount from the wallet balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet`

Called by the Game Aggregator when the user performs a bet.\
The Operator is expected to deduct the given `amount` from user's wallet and returns the latest balance.

Each bet transaction is identified by `transactionId`, Operator is expected to validate that the bet transaction is not processed before.

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                                    | Type    | Description                                                                                           |
| ------------------------------------------------------- | ------- | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String  | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| username<mark style="color:red;">\*</mark>              | String  | The username of the user in the Operator's system.                                                    |
| transactionId<mark style="color:red;">\*</mark>         | String  | A unique Id that identifies this transaction.                                                         |
| betId<mark style="color:red;">\*</mark>                 | String  | A unique Id that identifies this bet request transaction                                              |
| externalTransactionId<mark style="color:red;">\*</mark> | String  | An external transaction Id provided by Game Vendors.                                                  |
| amount<mark style="color:red;">\*</mark>                | Decimal | Amount of the bet transaction                                                                         |
| currency<mark style="color:red;">\*</mark>              | String  | ISO-4217 currency code representing the currency used for this transaction.                           |
| token<mark style="color:red;">\*</mark>                 | String  | User's session token generated by Game Aggregator.                                                    |
| gameCode<mark style="color:red;">\*</mark>              | String  | A unique game identifier in Game Aggregator system.                                                   |
| roundId<mark style="color:red;">\*</mark>               | String  | Game round Id for grouping all bets and wins in a single round.                                       |
| timestamp<mark style="color:red;">\*</mark>             | Number  | Unix timestamp of this transaction in milliseconds.                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}

{% tab title="200: OK Invalid signature" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INVALID_SIGNATURE"
}
```

{% endtab %}

{% tab title="200: OK Invalid token" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INVALID_TOKEN"
}
```

{% endtab %}

{% tab title="200: OK User does not exists" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "USER_NOT_EXISTS"
}
```

{% endtab %}

{% tab title="200: OK Wrong currency" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456,
    "status": "WRONG_CURRENCY"
}
```

{% endtab %}

{% tab title="200: OK Invalid request" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INVALID_REQUEST"
}
```

{% endtab %}

{% tab title="200: OK Insufficient funds" %}

```javascript
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "INSUFFICIENT_FUNDS"
}
```

{% endtab %}
{% endtabs %}

## A bet transaction request to add and/or deduct funds from the user balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_result`

Called by the Game Aggregator when the user performs a bet or receives a bet result.\
The Operator is expected to update the user's wallet balance based on the `resultType` and return the final balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                                    | Type          | Description                                                                                                                                                                                                                                                                                                                               |
| ------------------------------------------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String        | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                                                                                                                                                                                                                                     |
| username<mark style="color:red;">\*</mark>              | String        | The username of the user in the Operator's system.                                                                                                                                                                                                                                                                                        |
| transactionId<mark style="color:red;">\*</mark>         | String        | A unique Id that identifies this transaction.                                                                                                                                                                                                                                                                                             |
| betId<mark style="color:red;">\*</mark>                 | String        | A unique Id that identifies this bet request transaction                                                                                                                                                                                                                                                                                  |
| externalTransactionId<mark style="color:red;">\*</mark> | String        | An external transaction Id provided by Game Vendors.                                                                                                                                                                                                                                                                                      |
| roundId<mark style="color:red;">\*</mark>               | String        | Game round Id for grouping all bets and wins in a single round.                                                                                                                                                                                                                                                                           |
| betAmount<mark style="color:red;">\*</mark>             | Decimal       | Amount of the bet transaction.                                                                                                                                                                                                                                                                                                            |
| winAmount<mark style="color:red;">\*</mark>             | Decimal       | Amount of the win, Operator is expected to add `winAmount` to user's wallet when the `winAmount` is more than 0.                                                                                                                                                                                                                          |
| effectiveTurnover<mark style="color:red;">\*</mark>     | Decimal       | Amount of the effective turnover                                                                                                                                                                                                                                                                                                          |
| winLoss<mark style="color:red;">\*</mark>               | Decimal       | Amount of the absolute Win or Loss                                                                                                                                                                                                                                                                                                        |
| jackpotAmount                                           | Decimal       | Amount of the Jackpot, Operator is expected to add `jackpotAmount` to user's wallet when `jackpotAmount` is more than 0.                                                                                                                                                                                                                  |
| resultType<mark style="color:red;">\*</mark>            | String        | <p>The type of transaction process</p><p>1) "WIN" - (Player wins a bet)</p><p>2) "BET\_WIN" - (Player places a bet and win)</p><p>3) "BET\_LOSE" - (Player places a bet and lose)</p><p>4) "LOSE" - (Player loses a bet)</p><p>5) "END" - (To notify Operator that the round has ended for a bet, no operation on wallet is required)</p> |
| isFreespin<mark style="color:red;">\*</mark>            | Integer (0,1) | The status to indicate the bet is a free spin bet                                                                                                                                                                                                                                                                                         |
| isEndRound<mark style="color:red;">\*</mark>            | Integer (0,1) | The status to indicate the bet is completed                                                                                                                                                                                                                                                                                               |
| currency<mark style="color:red;">\*</mark>              | String        | ISO-4217 currency code representing the currency used for this transaction.                                                                                                                                                                                                                                                               |
| token<mark style="color:red;">\*</mark>                 | String        | User's session token generated by Game Aggregator.                                                                                                                                                                                                                                                                                        |
| gameCode<mark style="color:red;">\*</mark>              | String        | A unique game identifier in Game Aggregator system.                                                                                                                                                                                                                                                                                       |
| betTime<mark style="color:red;">\*</mark>               | Number        | The initial request Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                                                   |
| settledTime                                             | Number        | The bet settlement Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                                                    |
| timestamp<mark style="color:red;">\*</mark>             | Number  | Unix timestamp of this transaction in milliseconds.                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}
{% endtabs %}

## A rollback action on a bet transaction

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/rollback`

Called by the Game Aggregator to reverse a previous bet transaction.\
The Operator is expected to find the previous bet transaction based on the given `betId` to roll back its effect (credit/debit) to the user balance and return the latest balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                                    | Type   | Description                                                                                           |
| ------------------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| transactionId<mark style="color:red;">\*</mark>         | String | A unique Id that identifies this transaction.                                                         |
| betId<mark style="color:red;">\*</mark>                 | String | A bet transaction Id to roll back its affected (credit/debit) to the user balance.                    |
| externalTransactionId<mark style="color:red;">\*</mark> | String | An external transaction Id provided by Game Vendors.                                                  |
| roundId<mark style="color:red;">\*</mark>               | String | Game round Id for grouping all bets and wins in a single round.                                       |
| gameCode<mark style="color:red;">\*</mark>              | String | A unique game identifier in Game Aggregator system.                                                   |
| username<mark style="color:red;">\*</mark>              | String | The username of the user in the Operator's system.                                                    |
| currency<mark style="color:red;">\*</mark>              | String | ISO-4217 currency code representing the currency used for this transaction.                           |
| timestamp<mark style="color:red;">\*</mark>             | Number | Unix timestamp of this transaction in milliseconds.                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "status": "OK",
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}
{% endtabs %}

## An adjustment on the win amount of a game round.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/adjustment`

Called by the Game Aggregator to adjust a previous round of the bet transaction win/loss result. The Operator is expected to **add** or **deduct** the given amount on the user's wallet and returns the latest balance.

This endpoint supports specific game products; see the [**Endpoint Compatibility Guide**](https://apidoc-gasea.gitbook.io/operator-seamless-api-documentation-v3/api-reference/endpoint-compatibility-guide-for-integrated-products) for the full list.&#x20;

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                                    | Type    | Description                                                                                                          |
| ------------------------------------------------------- | ------- | -------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String  | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                |
| username<mark style="color:red;">\*</mark>              | String  | The username of the user in the Operator's system.                                                                   |
| transactionId<mark style="color:red;">\*</mark>         | String  | A unique Id that identifies this transaction.                                                                        |
| externalTransactionId<mark style="color:red;">\*</mark> | String  | An external transaction Id provided by Game Vendors.                                                                 |
| roundId<mark style="color:red;">\*</mark>               | String  | Game round Id for the previous transaction to be adjusted                                                            |
| amount<mark style="color:red;">\*</mark>                | Decimal | <p>Amount to be adjusted for this transaction</p><p>1) positive (add balance)</p><p>2) negative (deduct balance)</p> |
| currency<mark style="color:red;">\*</mark>              | String  | ISO-4217 currency code representing the currency used for this transaction.                                          |
| gameCode<mark style="color:red;">\*</mark>              | String  | A unique game identifier in Game Aggregator system.                                                                  |
| timestamp<mark style="color:red;">\*</mark>             | Number  | Unix timestamp of this transaction in milliseconds.                                                                  |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}
{% endtabs %}

## Debit: Deduct Balance for Game Room Entry

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_debit`

Called by the Game Aggregator when a player enters a game room and transfers amount into the game. The operator must deduct the specified amount from the player's wallet and return the player's updated balance.&#x20;

This API endpoint applies to several game room-type products. For a complete list of supported products, please refer to the [**Endpoint Compatibility Guide**](https://apidoc-gasea.gitbook.io/operator-seamless-api-documentation-v3/api-reference/endpoint-compatibility-guide-for-integrated-products) pag&#x65;**.**

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                            | Type          | Description                                                                                                                                                                                                                                                                                                                                           |
| ----------------------------------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>       | String        | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                                                                                                                                                                                                                                                 |
| username<mark style="color:red;">\*</mark>      | String        | The username of the user in the Operator's system.                                                                                                                                                                                                                                                                                                    |
| transactionId<mark style="color:red;">\*</mark> | String        | A unique Id that identifies this transaction.                                                                                                                                                                                                                                                                                                         |
| roundId<mark style="color:red;">\*</mark>       | String        | Game round Id for grouping all bets and wins in a single round.                                                                                                                                                                                                                                                                                       |
| takeAll                                         | Integer (0,1) | <p>Status to indicate full amount deduction or partial deduction from the player’s balance.</p><ul><li><strong>true</strong> (<code>1</code>), deducts full wallet balance, with <code>amount</code> set to 0 in the request. </li><li><strong>false</strong> (<code>0</code>), <code>amount</code> contains a value for partial deduction.</li></ul> |
| amount<mark style="color:red;">\*</mark>        | Decimal       | Amount to deduct from the player's wallet for transferring to the game room.                                                                                                                                                                                                                                                                          |
| currency<mark style="color:red;">\*</mark>      | String        | ISO-4217 currency code representing the currency used for this transaction.                                                                                                                                                                                                                                                                           |
| gameCode<mark style="color:red;">\*</mark>      | String        | A unique game identifier in Game Aggregator system.                                                                                                                                                                                                                                                                                                   |
| token<mark style="color:red;">\*</mark>         | String        | User's session token generated by Game Aggregator.                                                                                                                                                                                                                                                                                                    |
| timestamp<mark style="color:red;">\*</mark>     | Number        | Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                                                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}
{% endtabs %}

## Credit: Settle Bet and Update Balance

<mark style="color:green;">`POST`</mark> `https://<operator_site>/wallet/bet_credit`

Called by the Game Aggregator when a game round concludes or a balance adjustment is required. This endpoint is used to update the player's wallet balance, either by finalizing betting and winning records or by processing adjustments. The operator must add back the remaining balance specified in the `amount` parameter to the player's wallet. The `betAmount` and `winAmount` parameters are for reference only and do not require processing.

This API endpoint applies to several game room-type products. For a complete list of supported products, please refer to the [**Endpoint Compatibility Guide**](https://apidoc-gasea.gitbook.io/operator-seamless-api-documentation-v3/api-reference/endpoint-compatibility-guide-for-integrated-products) page.

<mark style="color:red;">**Important**</mark>: This API call is idempotent, multiple calls with the same `transactionId` should not be processed again and return the latest balance of the user (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                               |
| ---------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                          |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature generated using HMAC-SHA256 algorithm signed using the operator's API secret key on the request body. |

#### Request Body

| Name                                                | Type          | Description                                                                                                                                                                                                                                                                                                   |
| --------------------------------------------------- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>           | String        | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                                                                                                                                                                                                         |
| username<mark style="color:red;">\*</mark>          | String        | The username of the user in the Operator's system.                                                                                                                                                                                                                                                            |
| transactionId<mark style="color:red;">\*</mark>     | String        | A unique Id that identifies this transaction.                                                                                                                                                                                                                                                                 |
| betId<mark style="color:red;">\*</mark>             | String        | A unique Id that identifies this bet request transaction                                                                                                                                                                                                                                                      |
| roundId<mark style="color:red;">\*</mark>           | String        | Game round Id for grouping all bets and wins in a single round.                                                                                                                                                                                                                                               |
| isRefund<mark style="color:red;">\*</mark>          | Integer (0,1) | <p>Status to indicate whether this request is a refund of a previous debit transaction.</p><ul><li><strong>true</strong> (<code>1</code>): Indicates that this is a refund request for a prior debit.</li><li><strong>false</strong> (<code>0</code>): Indicates that this is not a refund request.</li></ul> |
| amount<mark style="color:red;">\*</mark>            | Decimal       | Remaining balance in the game to be added back to the player's wallet                                                                                                                                                                                                                                         |
| betAmount<mark style="color:red;">\*</mark>         | Decimal       | Total bet amount placed by the player                                                                                                                                                                                                                                                                         |
| winAmount<mark style="color:red;">\*</mark>         | Decimal       | Total amount won by the player                                                                                                                                                                                                                                                                                |
| effectiveTurnover<mark style="color:red;">\*</mark> | Decimal       | Amount of the effective turnover                                                                                                                                                                                                                                                                              |
| winLoss<mark style="color:red;">\*</mark>           | Decimal       | Amount of the absolute Win or Loss                                                                                                                                                                                                                                                                            |
| jackpotAmount                                       | Decimal       | Amount of the Jackpot, Operator is expected to add `jackpotAmount` to user's wallet when `jackpotAmount` is more than 0.                                                                                                                                                                                      |
| currency<mark style="color:red;">\*</mark>          | String        | ISO-4217 currency code representing the currency used for this transaction.                                                                                                                                                                                                                                   |
| token<mark style="color:red;">\*</mark>             | String        | User's session token generated by Game Aggregator.                                                                                                                                                                                                                                                            |
| gameCode<mark style="color:red;">\*</mark>          | String        | A unique game identifier in Game Aggregator system.                                                                                                                                                                                                                                                           |
| betTime<mark style="color:red;">\*</mark>           | Number        | The initial request Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                       |
| settledTime                                         | Number        | The bet settlement Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                        |
| timestamp<mark style="color:red;">\*</mark>         | Number        | Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                                           |

{% tabs %}
{% tab title="200: OK Response success" %}

```json
{
    "traceId": "f8c3de3d-1fea-4d7c-a8b0-29f63c4c3456",
    "status": "OK",
    "data": {
        "username": "bob12345",
        "currency": "USD",
        "balance": 100.00,
        "timestamp": 1719792000000 // wallet transaction unix timestamp in milliseconds
    }
}
```

{% endtab %}
{% endtabs %}