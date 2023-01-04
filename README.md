# TransFAIR

A ready-to-use tool (turnkey solution) for data integration for medical institutions. Instead of creating own ETL processes by hand, this tool facilitates certain data integration tasks like:

- **E**xtraction from source systems
- **T**ransformation into target schemata
- **L**oading into target systems
- Linkage of IDs / Pseudonymization
- Filtering of datasets

TransFAIR allows low-effort, fully automatic data transfer among software systems and data structures used in network medical research in Germany, in particular:

- Tumor Documentation Systems based on the [ADT/GEKID dataset](https://www.gekid.de/adt-gekid-basisdatensatz), as found in German Comprehensive Cancer Centers and connected via the [German Cancer Consortium (DKTK)](https://dktk.dkfz.de), e.g. CREDOS, GTDS, Onkostar
- The [CentraXX biobanking solution](https://www.kairos.de/produkte/centraxx-bio/) often found in German biobanks, which are networked under the umbrella of the [German Biobank Node](https://www.bbmri.de)
- Data Integration Centers, as established by the [Medical Informatics Initiative](https://www.medizininformatik-initiative.de) / [Netzwerk Universitätsmedizin](https://www.netzwerk-universitaetsmedizin.de), based on the [MII Core Dataset in FHIR](https://simplifier.net/organization/koordinationsstellemii)
- [Bridgeheads](https://github.com/samply/bridgehead) as used in the above networks as well as the European [Biobanking and BioMolecular Research Infrastructure (BBMRI-ERIC)](https://bbmri-eric.eu)
- ERNs participating in rare disease initiatives such as [EJP-RD](https://www.ejprarediseases.org/), which utilize the [Beacon 2.0 search API](http://docs.genomebeacons.org/). See the section [Beacon 2.0](#beacon-2) for details.

TransFAIR is designed to

- minimize effort for personnel at the sites (since they no longer have to do the data integration themselves)
- continuously update itself with new dataset/mapping definitions
- thus accelerate and facilitate rollout of new features and dataset extensions
- provide more consistent data quality (because as long as the source data is okay, errors within TransFAIR's mappings can be fixed centrally)

## Quickstart (for Bridgehead sites)

If you are part of a German University Hospital with a Bridgehead (e.g. via BBMRI-ERIC, GBN, DKTK, CCP/C4 or nNGM), you already have TransFAIR as part of your Bridgehead, usually preconfigured with sane default values and mappings by the respective network. The most straightforward way to use it is to just activate it.

To do so, specify the required configuration (see [Configuration](#configuration)) in a new environment file (e.g. `my.transfair`). Then, execute `bridgehead transfair mytransfair` and observe the output on the screen.

## Configuration

TransFAIR is configured using environment variables:

| Variable                                  | Description                                                                                                        | Default                          |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------|----------------------------------|
| `TF_FHIR_SERVER_SOURCE_ADDRESS`           | HTTP Address of the `SOURCE` datastore                                                                             | (required)                       |
| `TF_FHIR_SERVER_TARGET_ADDRESS`           | HTTP Address of the `TARGET` datastore                                                                             | (required)                       |
| `TF_FHIR_SERVER_(SOURCE/TARGET)_USERNAME` | Basic Auth User                                                                                                    |                                  |
| `TF_FHIR_SERVER_(SOURCE/TARGET)_PASSWORD` | Basic Auth Password                                                                                                |                                  |
| `TF_PROFILE`                              | Identifier of the TransFAIR profile to execute (see [Profiles](#profiles))                                         | (required)                       |
| `TF_RESOURCES_START`                      | (`Patient`/`Specimen`) Starts collection resources on the specified level.                                         | `Patient`                        |
| `TF_RESOURCES_FILTER`                     | Set to export only the specified resources.                                                                        | none, will export all ressources |
| `TF_RESOURCES_WHITELIST`                  | Transfers only resources according to the [Filters](#filters).                                                     |                                  |
| `TF_RESOURCES_BLACKLIST`                  | ignores resources according to the [Filters](#filters).                                                            |                                  |
| `TF_PSEUDONYMIZATION_ADDR`                | HTTP Address pointing to a service to map `SOURCE` IDs to `TARGET` IDs (see [Pseudonymization](#pseudonymization)) | none, IDs will be unchanged      |
| `TF_BEACON_PATH`                          | Path to directory for dumping Beacon-related files.                                                                                                   |

## Profiles

As of now, TransFAIR supports the following transformation profiles:



- `FHIR2FHIR` will transfer all ressources from `SOURCE` to `TARGET` unchanged. This can be used to perform filtering and/or pseudonymization across FHIR servers.
- `MII2BBMRI` will read the MII Core Dataset from `SOURCE` (usually a FHIR server/fassade providing the MII Core Dataset) and transfer all data required by BBMRI-ERIC into `TARGET` (= BBMRI-ERIC Bridgehead)
- `BBMRI2MII` will load biosample information from `SOURCE` (BBMRI-ERIC Bridgehead), transform into MII Core Dataset to `TARGET` (e.g. FHIR Store with MII Core Dataset)
- `BBMRI2BEACON` will load biosample information from `SOURCE` (BBMRI-ERIC Bridgehead), transform into "Beacon friendly format" (BFF) JSON and dump the result into files.

## Filters

TransFAIR supports many filters to customize the ETL process. Filters are coded with json.
For example here we provide a filter that either bans or only transfers the ids.

```json
{"patient": {
  "ids": ["1"]
  }
}
```

## Pseudonymization

TransFAIR supports various ways to map patient/sample IDs between source and target stores, e.g. pseudonymization solutions ([Mainzelliste](https://mainzelliste.de), [GPAS](https://www.ths-greifswald.de/forscher/gpas/)) or a plain mapping file in CSV format. Mapping works as follows:

Whenever TransFAIR encounters an ID from the `SOURCE` system, it will ask the service defined in `PSEUDONYMIZATION_ADDR` for the corresponding ID in the `TARGET` system (or vice-versa). We are currently defining a simple, implementation-independent API format in cooperation with pilot biobanks and will update this section once finished.

## Beacon 2

Setting the environment variable '''TF_PROFILE''' to "BBMRI2BEACON" causes TransFAIR to extract information from a FHIR store and generate files in "Beacon friendly format" (BFF). These files can be imported into a Beacon API reference implementation running on site, providing the Bridgehead with Beacon capabilites.

The generated files are deposited by default in the directory '''/srv/transfair'''. You can specify a different directory via the '''TF_BEACON_PATH''' environment variable. If you are using Docker for running TransFAIR, you can mount the directory as a volume, to make the generated files accessible externally.

Of all the possible BFF files, only two are generated by this code:

- individuals.json
- biosamples.json

These encapsulate information from patients and specimens respectively.

To run a Beacon 2.0 API, set up a Beacon reference implementation by cloning the [API](https://github.com/EGA-archive/beacon2-ri-api) and the [data import](https://github.com/EGA-archive/beacon2-ri-tools) repositories and building Docker images for them. Use the data import RI to import your generated BFF data into MongoDB and then start the Beacon API.

## Outlook

We have created TransFAIR with the specific use-case of bringing German biobanks and data integration centers closer together. Perspectively, we intend TransFAIR to become a toolbox with easily reusable components for use with [HL7 FHIR](https://hl7.org/fhir/), [OMOP](https://www.ohdsi.org/data-standardization/) and other well-known SQL, CSV and XML schemata.

## License

Copyright 2021 - 2022 The Samply Community
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
