
# alcohol-duty-calculator

This is the backend microservice that handles duty calculations for Alcohol Duty Service.

## API Endpoints

- [Calculate adjustment duty](api-docs/calculateAdjustmentDuty.md): `POST /alcohol-duty-calculator/calculate-adjustment-duty`
- [Calculate duty due by tax type](api-docs/calculateDutyDueByTaxType.md): `POST /alcohol-duty-calculator/calculate-duty-due-by-tax-type`
- [Calculate repackaged duty change](api-docs/calculateRepackagedDutyChange.md): `POST /alcohol-duty-calculator/calculate-repackaged-duty-change`
- [Calculate total adjustment](api-docs/calculateTotalAdjustment.md): `POST /alcohol-duty-calculator/calculate-total-adjustment`
- [Calculate total duty](api-docs/calculateTotalDuty.md): `POST /alcohol-duty-calculator/calculate-total-duty`
- [Get a rate band for a tax code in period](api-docs/getRateBand.md): `GET /alcohol-duty-calculator/rate-band`
<<<<<<< Updated upstream
- [Get several rate band for tax codes in periods](api-docs/getRateBands.md): `GET /alcohol-duty-calculator/rate-bands`
=======
- [Get several rate bands for tax codes in periods](api-docs/getRateBands.md): `GET /alcohol-duty-calculator/rate-bands`
>>>>>>> Stashed changes
- [Get rates for a period](api-docs/getRates.md): `GET /alcohol-duty-calculator/rates`

## Running the service

> `sbt run`
The service runs on port `16003` by default.

## Running tests

### Unit tests

> `sbt test`
### Integration tests

> `sbt it/test`
## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`

### All tests and checks
This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
