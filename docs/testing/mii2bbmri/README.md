# Test for MII 2 BBMRI.de

## Usage

```
docker-compose up
```

## Data

Upload data with 

```
curl -d @mii_test_person.json -H "Content-Type: application/json" http://localhost:8082/fhir 
```

## Conversion

```
curl http://localhost:8070/v1/fhir/mii2bbmri
```