# Calculate repackaged duty change

Performs the calculation newDuty - oldDuty to calculate what the customer owes after repackaging.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/calculate-repackaged-duty-change`

**Method**: `POST`

**URL Params**

No parameters are required

**Request Body**

| Field Name | Description    | Data Type | Mandatory/Optional | Notes |
|------------|----------------|-----------|--------------------|-------|
| newDuty    | The new duty   | Numeric   | Mandatory          |       |
| oldDuty    | The old duty   | Numeric   | Mandatory          |       |

**Request Body Examples**

***An example duty change:***

```json
{
  "newDuty": 100.50,
  "oldDuty": 50.25
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

| Field Name | Description                             | Data Type | Mandatory/Optional | Notes                                                       |
|------------|-----------------------------------------|-----------|--------------------|-------------------------------------------------------------|
| duty       | The difference between old and new duty | Numeric   | Mandatory          | Positive if owing, negative if a refund (expected positive) |

**Response Body Examples**

***Fifty pounds twenty five is owing:***

```json
{"duty":50.25}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.