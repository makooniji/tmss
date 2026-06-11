# Sportsbook API

## A bet transaction to deduct amount from the wallet balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/bet`

Triggered by the Game Aggregator when the user places a bet.\
The Operator is expected to deduct the specified `betAmount` from the user's wallet and return the updated balance.

Each bet transaction is identified by a `transactionId`. The Operator is expected to validate that the bet transaction has not been processed previously.

For Parlay bet types, the request includes a `multipleBetIds` array, which consists of objects that each contain a `betId` and `betAmount`. The Operator must handle each bet in this array appropriately, ensuring that all specified bet amounts are deducted and the respective `betIds` are processed.

<mark style="color:red;">**Important**</mark>: This API call is idempotent. Multiple calls with the same `transactionId` should not be processed again. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type                                               | Description                                                                                                                                                                                                                                                                                   |
| ------------------------------------------------------- | -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String                                             | A Universally Unique Identifier (UUID) is assigned by the Game Aggregator's system to each API request.                                                                                                                                                                                       |
| username<mark style="color:red;">\*</mark>              | String                                             | The username of the user in the Operator's system.                                                                                                                                                                                                                                            |
| transactionId<mark style="color:red;">\*</mark>         | String                                             | A unique ID identifying this transaction.                                                                                                                                                                                                                                                     |
| externalTransactionId<mark style="color:red;">\*</mark> | String                                             | An external transaction ID provided by Game Vendors.                                                                                                                                                                                                                                          |
| betId<mark style="color:red;">\*</mark>                 | String                                             | A unique ID identifying this bet request transaction.                                                                                                                                                                                                                                         |
| roundId<mark style="color:red;">\*</mark>               | String                                             | The Game Round ID for grouping all bets and wins in a single round.                                                                                                                                                                                                                           |
| betAmount<mark style="color:red;">\*</mark>             | Decimal                                            | Amount of the bet transaction                                                                                                                                                                                                                                                                 |
| gameCode<mark style="color:red;">\*</mark>              | String                                             | A unique game identifier in the Game Aggregator system.                                                                                                                                                                                                                                       |
| currency<mark style="color:red;">\*</mark>              | String                                             | ISO-4217 currency code representing the currency used for this transaction.                                                                                                                                                                                                                   |
| betType<mark style="color:red;">\*</mark>               | Integer                                            | <p>1: Normal Bet<br>2: Parlay Bet</p>                                                                                                                                                                                                                                                         |
| oddsType                                                | Integer                                            | <p>Odds type of the bets.<br>0 : Special<br>1 : Malay<br>2 : China<br>3 : Decimal<br>4 : Indo<br>5 : American<br>6 : Euro<br>7 : HongKong<br>999 : Unknown</p>                                                                                                                                |
| odds                                                    | Big Decimal                                        | <p>Odds of the bet.<br>When <code>betType</code> = 2 (Parlay Bet), <code>odds</code> will be given as total Odds.</p>                                                                                                                                                                         |
| timestamp<mark style="color:red;">\*</mark>             | Number                                             | Unix timestamp of this transaction in milliseconds.                                                                                                                                                                                                                                           |
| multipleBetIds<mark style="color:red;">\*</mark>        | Array \[ { betId (String), betAmount (Decimal) } ] | <p>An array containing multiple bet records. Each object in the array should have the following properties:<br></p><ul><li><code>betId</code>: The unique identifier for a bet.</li><li><code>betAmount</code>: The amount to be deducted for the corresponding <code>betId</code>.</li></ul> |

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

## A bet transaction updates wallet balance with added credit.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/update-bet`

Triggered by the Game Aggregator when the user confirms a bet, the Operator is expected to add the specified `creditAmount` to the user's wallet and return the updated balance.<br>

Each bet transaction is identified by a `transactionId`, and the Operator is expected to validate that the bet transaction has not been processed previously.<br>

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be processed again. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.&#x20;

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type        | Description                                                                                                                                                    |
| ------------------------------------------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String      | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                                                          |
| username<mark style="color:red;">\*</mark>              | String      | The username of the user in the Operator's system.                                                                                                             |
| transactionId<mark style="color:red;">\*</mark>         | String      | A unique ID that identifies this transaction.                                                                                                                  |
| externalTransactionId<mark style="color:red;">\*</mark> | String      | An external transaction ID provided by Game Vendors.                                                                                                           |
| betId<mark style="color:red;">\*</mark>                 | String      | A unique ID that identifies this bet request transaction.                                                                                                      |
| roundId<mark style="color:red;">\*</mark>               | String      | Game round ID for grouping all bets and wins in a single round.                                                                                                |
| betAmount<mark style="color:red;">\*</mark>             | Decimal     | Original bet amount of the bet transaction.                                                                                                                    |
| newBetAmount<mark style="color:red;">\*</mark>          | Decimal     | Updated amount of the bet transaction                                                                                                                          |
| creditAmount<mark style="color:red;">\*</mark>          | Decimal     | The difference between betAmount and newBetAmount that needs to be credited to the user.                                                                       |
| gameCode<mark style="color:red;">\*</mark>              | String      | A unique game identifier in the Game Aggregator system.                                                                                                        |
| currency<mark style="color:red;">\*</mark>              | String      | ISO-4217 currency code representing the currency used in this transaction.                                                                                     |
| oddsType                                                | Integer     | <p>Odds type of the bets.<br>0 : Special<br>1 : Malay<br>2 : China<br>3 : Decimal<br>4 : Indo<br>5 : American<br>6 : Euro<br>7 : HongKong<br>999 : Unknown</p> |
| odds                                                    | Big Decimal | Odds of the bet.                                                                                                                                               |
| timestamp<mark style="color:red;">\*</mark>             | Number      | Unix timestamp of this transaction in milliseconds.                                                                                                            |

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

## A rollback action for a bet transaction.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/refund`

Triggered by the Game Aggregator, this action reverses a previous bet transaction. The Operator is expected to locate the prior bet transaction using the provided `betId`, undoing its impact (credit/debit) on the user balance, and then return the updated balance.&#x20;

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be reprocessed. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.&#x20;

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type   | Description                                                                                           |
| ------------------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| username<mark style="color:red;">\*</mark>              | String | The username of the user in the Operator's system.                                                    |
| transactionId<mark style="color:red;">\*</mark>         | String | A unique ID that identifies this transaction.                                                         |
| externalTransactionId<mark style="color:red;">\*</mark> | String | An external transaction ID provided by Game Vendors.                                                  |
| betId<mark style="color:red;">\*</mark>                 | String | A bet transaction ID to rollback its impact (credit/debit) on the user balance.                       |
| roundId<mark style="color:red;">\*</mark>               | String | Game round ID for grouping all bets and wins in a single round.                                       |
| gameCode<mark style="color:red;">\*</mark>              | String | A unique game identifier in the Game Aggregator system.                                               |
| currency<mark style="color:red;">\*</mark>              | String | ISO-4217 currency code representing the currency used in this transaction.                            |
| timestamp<mark style="color:red;">\*</mark>             | Number | Unix timestamp of this transaction in milliseconds.                                                   |

{% tabs %}
{% tab title="200: OK Response success" %}

```javascript
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

## A bet transaction request to add funds to the user's balance or leave it unchanged.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/settled`

Initiated by the Game Aggregator upon receiving the user's bet result, the Operator should adjust the user's wallet balance according to the `winAmount` and provide the final balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be reprocessed. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type    | Description                                                                                                              |
| ------------------------------------------------------- | ------- | ------------------------------------------------------------------------------------------------------------------------ |
| traceId<mark style="color:red;">\*</mark>               | String  | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                    |
| username<mark style="color:red;">\*</mark>              | String  | The username of the user in the Operator's system.                                                                       |
| transactionId<mark style="color:red;">\*</mark>         | String  | A unique ID that identifies this transaction.                                                                            |
| externalTransactionId<mark style="color:red;">\*</mark> | String  | An external transaction ID provided by Game Vendors.                                                                     |
| betId<mark style="color:red;">\*</mark>                 | String  | A unique ID that identifies this bet request transaction.                                                                |
| roundId<mark style="color:red;">\*</mark>               | String  | Game round ID for grouping all bets and wins in a single round.                                                          |
| betAmount<mark style="color:red;">\*</mark>             | Decimal | Amount of the bet transaction.                                                                                           |
| winAmount<mark style="color:red;">\*</mark>             | Decimal | The win amount: The Operator is expected to add the `winAmount` to the user's wallet when `winAmount` is greater than 0. |
| effectiveTurnover<mark style="color:red;">\*</mark>     | Decimal | The effective turnover amount.                                                                                           |
| winLoss<mark style="color:red;">\*</mark>               | Decimal | The absolute win or loss amount.                                                                                         |
| gameCode<mark style="color:red;">\*</mark>              | String  | A unique game identifier in the Game Aggregator system.                                                                  |
| currency<mark style="color:red;">\*</mark>              | String  | ISO-4217 currency code representing the currency used in this transaction.                                               |
| timestamp<mark style="color:red;">\*</mark>             | Number  | The bet settlement Unix timestamp of this transaction in milliseconds.                                                   |

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

## An unsettle action for a bet transaction

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/unsettle`

Triggered by the Game Aggregator, this action reverses and unsettles a previous bet transaction. The Operator is expected to locate the prior bet transaction using the provided `betId`, undoing its impact (credit/debit) on the user balance, and then return the updated balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be reprocessed. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type   | Description                                                                                           |
| ------------------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| username<mark style="color:red;">\*</mark>              | String | The username of the user in the Operator's system.                                                    |
| transactionId<mark style="color:red;">\*</mark>         | String | A unique ID that identifies this transaction.                                                         |
| externalTransactionId<mark style="color:red;">\*</mark> | String | An external transaction ID provided by Game Vendors.                                                  |
| betId<mark style="color:red;">\*</mark>                 | String | A bet transaction ID to revert its impact (credit/debit) on the user balance.                         |
| roundId<mark style="color:red;">\*</mark>               | String | Game round ID for grouping all bets and wins in a single round.                                       |
| gameCode<mark style="color:red;">\*</mark>              | String | A unique game identifier in the Game Aggregator system.                                               |
| currency<mark style="color:red;">\*</mark>              | String | ISO-4217 currency code representing the currency used in this transaction.                            |
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

## An updated bet transaction request to add and/or deduct funds from the user's balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/resettle`

Initiated by the Game Aggregator when there are transaction updates on a previous bet transaction, the Operator is expected to adjust the user's wallet balance based on the `creditAmount`, and return the final balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be reprocessed. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type    | Description                                                                                                                   |
| ------------------------------------------------------- | ------- | ----------------------------------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String  | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request.                         |
| username<mark style="color:red;">\*</mark>              | String  | The username of the user in the Operator's system.                                                                            |
| transactionId<mark style="color:red;">\*</mark>         | String  | A unique ID that identifies this transaction.                                                                                 |
| externalTransactionId<mark style="color:red;">\*</mark> | String  | An external transaction ID provided by Game Vendors.                                                                          |
| betId<mark style="color:red;">\*</mark>                 | String  | A unique ID that identifies this bet request transaction.                                                                     |
| roundId<mark style="color:red;">\*</mark>               | String  | Game round ID for grouping all bets and wins in a single round.                                                               |
| betAmount<mark style="color:red;">\*</mark>             | Decimal | Amount of the bet transaction.                                                                                                |
| winAmount<mark style="color:red;">\*</mark>             | Decimal | The win amount.                                                                                                               |
| newWinAmount<mark style="color:red;">\*</mark>          | Decimal | Updated win amount for the new transaction.                                                                                   |
| winLoss<mark style="color:red;">\*</mark>               | Decimal | The absolute win or loss amount.                                                                                              |
| creditAmount<mark style="color:red;">\*</mark>          | Decimal | Credited amount: The Operator is expected to add `creditAmount` to the user's wallet when `creditAmount` is greater than 0.   |
| gameCode<mark style="color:red;">\*</mark>              | String  | A unique game identifier in the Game Aggregator system.                                                                       |
| currency<mark style="color:red;">\*</mark>              | String  | ISO-4217 currency code representing the currency used in this transaction.                                                    |
| timestamp<mark style="color:red;">\*</mark>             | Number  | The bet settlement Unix timestamp of this transaction in milliseconds.                                                        |

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

## A bet and result transaction request to add and/or deduct funds from the user's balance.

<mark style="color:green;">`POST`</mark> `https://<operator_site>/sports/adjustment`

Initiated by the Game Aggregator when there's a bet and result transaction, the Operator is expected to adjust the user's wallet balance based on the `creditAmount`, and return the final balance.

<mark style="color:red;">**Important**</mark>: This API call is idempotent; multiple calls with the same `transactionId` should not be reprocessed. Instead, it should return the latest user balance (SC\_OK) if the Operator has successfully processed the transaction before.

#### Headers

| Name                                           | Type   | Description                                                                                                                         |
| ---------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Content-Type<mark style="color:red;">\*</mark> | String | application/json                                                                                                                    |
| X-Signature<mark style="color:red;">\*</mark>  | String | A digital signature is generated using the HMAC-SHA256 algorithm and signed with the operator's API secret key on the request body. |
| X-Api-Key                                      | String | Operator's API key                                                                                                                  |

#### Request Body

| Name                                                    | Type    | Description                                                                                           |
| ------------------------------------------------------- | ------- | ----------------------------------------------------------------------------------------------------- |
| traceId<mark style="color:red;">\*</mark>               | String  | A Universally Unique Identifier (UUID) provided by the Game Aggregator's system for each API request. |
| username<mark style="color:red;">\*</mark>              | String  | The username of the user in the Operator's system.                                                    |
| transactionId<mark style="color:red;">\*</mark>         | String  | A unique ID that identifies this transaction.                                                         |
| externalTransactionId<mark style="color:red;">\*</mark> | String  | An external transaction ID provided by Game Vendors.                                                  |
| roundId<mark style="color:red;">\*</mark>               | String  | Game round ID for grouping all bets and wins in a single round.                                       |
| amount<mark style="color:red;">\*</mark>                | Decimal | Amount of the bet transaction.                                                                        |
| currency<mark style="color:red;">\*</mark>              | String  | ISO-4217 currency code representing the currency used in this transaction.                            |
| timestamp<mark style="color:red;">\*</mark>             | Number  | The bet settlement Unix timestamp of this transaction in milliseconds.                                |

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