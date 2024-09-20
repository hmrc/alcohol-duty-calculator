# Calculate adjustment duty

Performs the calculation rate * pureAlcoholVolume for the adjustment provided and applies the appropriate sign depending if it's owing or a refund.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/calculate-adjustment-duty`

**Method**: `POST`

**URL Params**

No parameters are required

**Request Body**

| Field Name                  | Description                                      | Data Type | Mandatory/Optional | Notes                                                                          |
|-----------------------------|--------------------------------------------------|-----------|--------------------|--------------------------------------------------------------------------------|
| adjustmentType              | The entries to perform calculations on           | Enum      | Mandatory          | Underdeclaration, Overdeclaration, Spoilt, RepackagedDraughtProducts, Drawback |
| pureAlcoholVolume           | The pure alcohol volume of the product in litres | Numeric   | Mandatory          |                                                                                |
| rate                        | The duty rate charged                            | Numeric   | Mandatory          |                                                                                |

**Request Body Examples**

***An under-declaration:***
```json
{
  "adjustmentType": "Underdeclaration",
  "pureAlcoholVolume": 10,
  "rate": 0.1
}
```

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns the following fields

| Field Name | Description     | Data Type | Mandatory/Optional | Notes                                   |
|------------|-----------------|-----------|--------------------|-----------------------------------------|
| duty       | The adjustment  | Numeric   | Mandatory          | Positive if owing, negative if a refund |

**Response Body Examples**

***One thousand pounds and twenty five pence is owing:***

```json
{"duty":1000.25}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.