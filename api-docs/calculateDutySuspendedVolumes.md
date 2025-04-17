# Calculate Duty Suspended Volumes

This endpoint takes the list of values the user has inputted on the duy suspended journey, and calculates the total litres of alcohol Duty Suspended, and total litres of pure alcohol Duty Suspended over a given period.

The calculation uses the following figures entered by the user:
- Total litres delivered inside the UK
- Pure alcohol delivered inside the UK
- Total litres delivered outside the UK
- Pure alcohol delivered outside the UK
- Total litres received under Duty Suspense
- Pure alcohol received under Duty Suspense


Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/calculate-duty-suspended-volumes`

**Method**: `POST`

**URL Params**

No parameters are required

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

POST /alcohol-duty-calculator/calculate-duty-suspended-volumes

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body the Duty Suspended totals with the following fields

| Field Name                         | Description                                          | Data Type  | Mandatory/Optional | Notes                                                                                                                                                                                                                                                  |
|------------------------------------|------------------------------------------------------|------------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| totalLitres                        | The total litres of alcohol under Duty Suspense      | BigDecimal | Mandatory          | Calculated by: <br/> - Adding the total litres of **alcohol** delivered in the UK under duty suspense to the total delivered outside the UK under duty suspense<br/> - Subtracting the total litres of **alcohol** received under duty suspense            |
| pureAlcohol                        | The total litres of pure alcohol under Duty Suspense | BigDecimal | Mandatory          | Calculated by: <br/> - Adding the total litres of **pure alcohol** delivered in the UK under duty suspense to the total delivered outside the UK under duty suspense<br/> - Subtracting the total litres of **pure alcohol** received under duty suspense |

**Response Body Examples**

***A single element (real response will contain many elements like this):***

```json
[
  {
    "totalLitres": 100.55,
    "pureAlcohol": 15.23
  }
]
```

### Error responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.