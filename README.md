# TransFAIR

A tool for data integration for medical institutions
Instead of creating own ETL process, 
this tool provides certain data integration tasks like:
- Transferring Data
- Converting Data
- Pseudonymization

These Tasks are occurring currently in the interaction between German Biobanks and Data Integraton Centers(DIC).

Support for ETL process
Customizable ETL Process

ADT2FHIR 

## Usage

After setting up the environment just run
```sh
docker-compose up
```

## API

### FHIR

The tool supports multiple operation modes, converting or transferring.

#### BBMRI2BBMRI(Bridge)

```
/v1/fhir/bbmri2bbmri
```

This mode transfers all data related to the bbmri.de profiles


#### BBMIR2MII

```
/v1/fhir/bbmri2mii
```

This mode transfers data from bbmri.de to MII KDS profiles.

#### MII2BBRMI

```
/v1/fhir/mii2bbmri
```

This mode transfers data form MII KDS to bbmri.de

#### Overrides

There are some additional parameters you can set for the transfer

Overrides the source fhir server
```
?source=<adr>
```

Overrides the target fhir server
```
?target=<addr>
```

### Environment

| Variables        | Description                                                                                     |
|------------------|-------------------------------------------------------------------------------------------------|
| SOURCEFHIRSERVER | Address of the source fhir server                                                               |
| RESOURCEFILTER   | Exports only the specified resources. Empty exports all resources.                              |
| STARTRESOURCE    | (Patient/Specimen) Starts collection resources on the specified level. Empty starts at Patient. |
| TARGETFHIRSERVER | Address of the targeted fhir server                                                             |
| SAVETOFILESYSTEM | (true/false) Save the transformed data to filesystem                                            |
| PSEUDOCSVFILE    | Mapping-file for patient and sample identifiers                                                 |

## Pseudonymization

Not every store has the same ID's, therefore pseudonymization is needed. 
If needed, the tool supports different pseudonymization tools, like a plain csv file, Mianzelliste or GPAS.

## TODOs



