# Test for MII 2 bbmri.de

A small test setup for transfairing MII KDS data sets to [bbmri.de](https://simplifier.net/bbmri.de).

## Usage

### Preparation

Start the FHIR Servers with

```
docker-compose up
```

### Data

Upload data with 

```
curl -d @mii_test_person.json -H "Content-Type: application/json" http://localhost:8082/fhir 
```

### transfairing

Transfer the data with

```
docker run --env-file ./.env samply/transfair
```