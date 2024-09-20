# Get Rate Band

Returns tax rates, bands and description for a specific tax code for a specific period.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/rate-band`

**Method**: `GET`

**URL Params**

No parameters are required

**Query Params**

| Parameter Name | Description               | Data Type | Mandatory/Optional | Notes   |
|----------------|---------------------------|-----------|--------------------|---------|
| ratePeriod     | The period                | YearMonth | Mandatory          | YYYY-MM |
| taxTypeCode    | The three digit tax code  | String    | Mandatory          |         |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns details for a single tax code

| Field Name                         | Description                                                      | Data Type     | Mandatory/Optional | Notes                                                                   |
|------------------------------------|------------------------------------------------------------------|---------------|--------------------|-------------------------------------------------------------------------|
| taxTypeCode                        | The 3 digit tax code                                             | String        | Mandatory          |                                                                         |
| description                        | A description of the tax code                                    | String        | Mandatory          | Only those paid or part paid (amountPaid > 0)                           |
| rateType                           | The type of rate                                                 | Enum          | Mandatory          | Core, DraughtRelief, SmallProducerRelief, DraughtAndSmallProducerRelief |
| rangeDetails                       | Ranges of alcohol regimes and alcohol types covered by this rate | Object        | Mandatory          |                                                                         |
| rangeDetails.alcoholRegime         | The alcohol regime covered by this range                         | Enum          | Mandatory          | Beer, Cider, Wine, Spirits, OtherFermentedProduct                       |
| rangeDetails.abvRanges             | The ABV ranges and alcohol types covered                         | Array(Object) | Mandatory          |                                                                         |
| rangeDetails.abvRanges.alcoholType | The alcohol type                                                 | Enum          | Mandatory          | Beer, Cider, SparklingCider, Wine, Spirits, OtherFermentedProduct       |
| rangeDetails.abvRanges.minABV      | The min ABV                                                      | Numeric       | Mandatory          | Between 0 and 100                                                       |
| rangeDetails.abvRanges.maxABV      | The max ABV                                                      | Numeric       | Mandatory          | Between 0 and 100                                                       |
| rate                               | The rate                                                         | Numeric       | Optional           |                                                                         |

**Response Body Examples**

***An example rate band:***

```json
{
  "taxTypeCode": "359",
  "description": "Other fermented products like fruit ciders from 3.5% to 8.4% or Sparkling cider from 5.6% to 8.4%, eligible for draught relief",
  "rateType": "DraughtRelief",
  "rangeDetails": [
    {
      "alcoholRegime": "OtherFermentedProduct",
      "abvRanges": [
        {
          "alcoholType": "OtherFermentedProduct",
          "minABV": 3.5,
          "maxABV": 8.4
        },
        {
          "alcoholType": "SparklingCider",
          "minABV": 5.6,
          "maxABV": 8.4
        }
      ]
    }
  ],
  "rate": 19.08
}
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
This response can occur if the tax code couldn't be found in the rates file

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the rates file couldn't be read

