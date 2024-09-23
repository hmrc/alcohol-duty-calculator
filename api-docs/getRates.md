# Get Rates

Returns tax rates, bands and description for the various tax type codes of specific regimes for a specific period.

Calls to this API must be made by an authenticated and authorised user with an ADR enrolment in order for the data to be returned.

**URL**: `/alcohol-duty-calculator/rates`

**Method**: `GET`

**URL Params**

No parameters are required

**Query Params**

| Parameter Name  | Description                                  | Data Type | Mandatory/Optional | Notes                                             |
|-----------------|----------------------------------------------|-----------|--------------------|---------------------------------------------------|
| ratePeriod      | The period                                   | YearMonth | Mandatory          | YYYY-MM                                           |
| alcoholRegimes  | A set of regimes                             | Set(Enum) | Mandatory          | Beer, Cider, Wine, Spirits, OtherFermentedProduct |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/alcohol-duty-calculator/rates?ratePeriod="2024-07"&alcoholRegimes=Beer,Cider,Wine,Spirits,OtherFermentedProduct

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

The response body returns rate bands with the following fields

| Field Name                         | Description                                                      | Data Type | Mandatory/Optional | Notes                                                                   |
|------------------------------------|------------------------------------------------------------------|-----------|--------------------|-------------------------------------------------------------------------|
| taxTypeCode                        | The 3 digit tax code                                             | String    | Mandatory          |                                                                         |
| description                        | A description of the tax code                                    | String    | Mandatory          | Only those paid or part paid (amountPaid > 0)                           |
| rateType                           | The type of rate                                                 | Enum      | Mandatory          | Core, DraughtRelief, SmallProducerRelief, DraughtAndSmallProducerRelief |
| rangeDetails                       | Ranges of alcohol regimes and alcohol types covered by this rate | Enum      | Mandatory          | Return, LPI, RPI                                                        |
| rangeDetails.alcoholRegime         | The alcohol regime covered by this range                         | String    | Mandatory          | Beer, Cider, Wine, Spirits, OtherFermentedProduct                       |
| rangeDetails.abvRanges             | The ABV ranges and alcohol types covered                         | String    | Array(Items)       |                                                                         |
| rangeDetails.abvRanges.alcoholType | The alcohol type                                                 | Numeric   | Enum               | Beer, Cider, SparklingCider, Wine, Spirits, OtherFermentedProduct       |
| rangeDetails.abvRanges.minABV      | The min ABV                                                      | Numeric   | Mandatory          | Between 0 and 100                                                       |
| rangeDetails.abvRanges.maxABV      | The max ABV                                                      | Numeric   | Mandatory          | Between 0 and 100                                                       |
| rate                               | The rate                                                         | Numeric   | Optional           |                                                                         |

**Response Body Examples**

***A single element (real response will contain many elements like this):***

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
    "rate": 19.08
  }
]
```

### Responses

**Code**: `400 BAD_REQUEST`
This response can occur if the parameters cannot be parsed

**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any consumer without an authorized session that has an ADR enrolment.

**Code**: `500 INTERNAL_SERVER_ERROR`
This response can occur if the rates file couldn't be read