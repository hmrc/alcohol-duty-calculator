# Calculate due due by tax type

Groups the various entries (of duty and adjustment values) by tax type and sums each

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/calculate-duty-due-by-tax-type`

**Method**: `POST`

**URL Params**

No parameters are required

**Request Body**

| Field Name                           | Description                                      | Data Type    | Mandatory/Optional | Notes                                   |
|--------------------------------------|--------------------------------------------------|--------------|--------------------|-----------------------------------------|
| declarationOrAdjustmentItems         | An array of the duty declarations or adjustments | Array(Items) | Mandatory          |                                         |
| declarationOrAdjustmentItems.taxType | The 3 digit tax code                             | String       | Mandatory          |                                         |
| declarationOrAdjustmentItems.dutyDue | The duty or adjustment value                     | Numeric      | Mandatory          | Positive if owing, negative if a refund |

**Request Body Examples**

*** Three elements to be summed, one sharing a tax code: ***
```json
{
  "declarationOrAdjustmentItems": [
    {
      "taxType": "380",
      "dutyDue": 100.01
    },
    {
      "taxType": "381",
      "dutyDue": 200.02
    },
    {
      "taxType": "380",
      "dutyDue": -50
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

| Field Name                        | Description                           | Data Type    | Mandatory/Optional | Notes                                   |
|-----------------------------------|---------------------------------------|--------------|--------------------|-----------------------------------------|
| totalDueDueByTaxType              | The totals for each tax code          | Array(Items) | Mandatory          |                                         |
| totalDueDueByTaxType.taxType      | The 3 digit tax code                  | String       | Mandatory          |                                         |
| totalDueDueByTaxType.totalDutyDue | The totals duty due for the tax code  | Numeric      | Mandatory          | Positive if owing, negative if a refund | 

**Response Body Examples**

***Duty due for two tax types: ***

```json
{
  "totalDutyDueByTaxType": [
    {
      "taxType": "381",
      "totalDutyDue": 200.02
    },
    {
      "taxType": "380",
      "totalDutyDue": 50.01
    }
  ]
}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.