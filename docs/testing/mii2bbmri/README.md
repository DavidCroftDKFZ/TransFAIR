# Test for MII 2 BBMRI.de

## Usage

### Preparation

```
docker-compose up
```

### Data

Upload data with 

```
curl -d @mii_test_person.json -H "Content-Type: application/json" http://localhost:8082/fhir 
```

### Transferring

```
docker run --env-file ./.env transfair
```