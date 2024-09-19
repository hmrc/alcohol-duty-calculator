# Calculate total duty

Performs the calculation dutyRate * pureAlcohol for each of the tax types presented and provides individual calculations and the sum

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/calculate-total-duty`

**Method**: `POST`

**URL Params**

No parameters are required

**Request Body**

| Field Name                  | Description                                      | Data Type    | Mandatory/Optional | Notes                                             |
|-----------------------------|--------------------------------------------------|--------------|--------------------|---------------------------------------------------|
| dutiesByTaxType             | The entries to perform calculations on           | Array(Items) | Mandatory          |                                                   |
| dutiesByTaxType.taxType     | The 3 digit tax code                             | String       | Mandatory          |                                                   |
| dutiesByTaxType.totalLitres | The total volume of the product in litres        | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType.pureAlcohol | The pure alcohol volume of the product in litres | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType.dutyRate    | The duty rate to be charged                      | Numeric      | Mandatory          |                                                   |

**Request Body Examples**

*** Two tax types to calculate duty on: ***
```json
{
  "dutiesByTaxType": [
    {
      "taxType": "380",
      "totalLitres": 100,
      "pureAlcohol": 10,
      "dutyRate": 0.12
    },
    {
      "taxType": "381",
      "totalLitres": 200,
      "pureAlcohol": 20,
      "dutyRate": 0.1
    }
  ]
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

| Field Name                  | Description                                      | Data Type    | Mandatory/Optional | Notes                                             |
|-----------------------------|--------------------------------------------------|--------------|--------------------|---------------------------------------------------|
| totalDuty                   | The total duty to be charged for all tax types   | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType             | The entries to perform calculations on           | Array(Items) | Mandatory          |                                                   |
| dutiesByTaxType.taxType     | The 3 digit tax code                             | String       | Mandatory          |                                                   |
| dutiesByTaxType.totalLitres | The total volume of the product in litres        | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType.pureAlcohol | The pure alcohol volume of the product in litres | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType.dutyRate    | The duty rate to be charged                      | Numeric      | Mandatory          |                                                   |
| dutiesByTaxType.dutyDue     | The duty to be charged                           | Numeric      | Mandatory          |                                                   |

**Response Body Examples**

***A single element (real return will contain many elements like this): ***

```json
{
  "totalDuty": 3.2,
  "dutiesByTaxType": [
    {
      "taxType": "380",
      "totalLitres": 100,
      "pureAlcohol": 10,
      "dutyRate": 0.12,
      "dutyDue": 1.2
    },
    {
      "taxType": "381",
      "totalLitres": 200,
      "pureAlcohol": 20,
      "dutyRate": 0.1,
      "dutyDue": 2
    }
  ]
}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.