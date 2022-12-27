# Test for bbmri.de 2 MII KDS

A small test setup for transfairing bbmri.de data sets to [MII KDS](https://simplifier.net/organization/koordinationsstellemii/~projects).

## Usage

### Preparation

Start the FHIR Servers with

```
docker-compose up
```

### Data

Upload data with 

```
curl -d @bbmri.json -H "Content-Type: application/json" http://localhost:8082/fhir 
```

### transfairing

Transfer the data with

```
docker run --env-file ./.env samply/transfair
```