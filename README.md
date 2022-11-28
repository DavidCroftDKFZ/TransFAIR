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

### Modes


#### FHIR

Name | Data source | Data target

The tool supports multiple operation modes, converting or transferring.

##### BBMRI2BBMRI(Bridge)

This mode transfers all data related to the bbmri.de profiles

##### BBMIR2MII

This mode transfers data from bbmri.de to MII KDS profiles.

##### MII2BBRMI

This mode transfers data form MII KDS to bbmri.de

### Environment

| Variables          | Description                                                             |
|--------------------|-------------------------------------------------------------------------|
| SOURCE_LOAD_FHIR   | (true/false) Loading data from FHIR server                              |
| SOURCE_FHIR_SERVER | Address of the source fhir server                                       |
| SOURCE_LOAD_FILES  | (true/false)    Loading data from filesystem                            |
| MODE               | (BBMRI2BBMRI, BBMRI2MII, MII2BBMRI, BBMRI2DKTK) Modes(#Modes)           |
| RESOURCE_FILTER    | The tool export only the specified resources. Empty means all resources |
| START_RESOURCE     | (Patient/Specimen) Starts collection resources on the specified level   |
| TARGET_POST_FHIR   | (true/false) Save the transferred data to a fhir server                 |
| TARGET_FHIR_SERVER | Address of the targeted fhir server                                     |
| TARGET_SAVE_FILES  | (true/false) Save the transferred data to filesystem                    |
|                    |                                                                         |

## Pseudonymization

Not every store has the same ID's, therefore pseudonymization is needed. 
If needed, the tool supports different pseudonymization tools, like a plain csv file, Mianzelliste or GPAS.

## TODOs



