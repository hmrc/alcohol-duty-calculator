# Get Rate Bands

Returns a list of tax rates, bands and description for a specific list of tax codes for specific periods.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/rate-bands`

**Method**: `GET`

**URL Params**

No parameters are required

**Query Params**

| Parameter Name | Description               | Data Type     | Mandatory/Optional | Notes   |
|----------------|---------------------------|---------------|--------------------|---------|
| ratePeriods    | The periods               | YearMonth CSV | Mandatory          | YYYY-MM |
| taxTypeCodes   | The three digit tax codes | String CSV    | Mandatory          |         |

Note the number of rate periods must match the number of tax codes

***Example request:***

/alcohol-duty-calculator/rate-band?ratePeriod="2024-07","2024-08"&taxTypeCode=311,312

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns a list of details for each rate period, tax code in the same order as the parameter rate period, tax code pairs

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
| repackagedTaxTypeCode              | The repackaged tax code type mapping                             | String        | Optional           | Only set for repackaged draught tax code types                          |

**Response Body Examples**

***An example single rate band:***

```json
[
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
    "rate": 19.08,
    "repackagedTaxTypeCode": "324"
  }
]
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed, or the number of rate periods and tax codes do not match

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `404 NOT_FOUND`
This response can occur if one or more the tax codes couldn't be found in the rates file for the respective rate periods

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the rates file couldn't be read

