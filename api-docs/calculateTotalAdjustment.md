# Calculate total adjustment

Performs the sum of the various adjustments

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/calculate-total-adjustment`

**Method**: `POST`

**URL Params**

No parameters are required

**Request Body**

| Field Name | Description                   | Data Type      | Mandatory/Optional | Notes |
|------------|-------------------------------|----------------|--------------------|-------|
| dutyList   | An array of the duties to sum | Array(Numeric) | Mandatory          |       |

**Request Body Examples**

*** Three adjustments to be summed: ***
```json
{
  "dutyList": [100.01, -200.02, 300.03]
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

| Field Name | Description          | Data Type | Mandatory/Optional | Notes                                   |
|------------|----------------------|-----------|--------------------|-----------------------------------------|
| duty       | The total adjustment | Numeric   | Mandatory          | Positive if owing, negative if a refund |

**Response Body Examples**

***Two hundred pounds and two pence is owing: ***

```json
{"duty":200.02}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.