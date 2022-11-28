# Mode BBMRI2BBMRI

## Usage

This mode is for the use case transferring data from one FHIR server to another.
For example if you are changing the sever or want to clone your production data to a test system.

## Configuration

| Variables          | Description                         |
|--------------------|-------------------------------------|
| SOURCE_LOAD_FHIR   | true                                |
| SOURCE_FHIR_SERVER | Address of the source fhir server   |
| SOURCE_LOAD_FILES  | false                               |
| RESOURCE_FILTER    |                                     |
| START_RESOURCE     | Specimen                            |
| TARGET_POST_FHIR   | true                                |
| TARGET_FHIR_SERVER | Address of the targeted fhir server |
| TARGET_SAVE_FILES  | false                               |
|                    |                                     |
