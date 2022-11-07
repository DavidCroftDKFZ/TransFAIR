package de.samply.transfair.resources;

import de.samply.transfair.converters.IDMapper;
import de.samply.transfair.converters.Resource_Type;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 Test classes responsible for the {@link de.samply.transfair.converters.IDMapper}
 */
@SpringBootTest
@TestPropertySource(properties = "FHIRTRANSFAIR_MAPPER_SETTING=csvmapping")
@TestPropertySource(properties = "FHIRTRANSFAIR_CSVMAPPING_PATH=./test_mapping.csv")
public class IDMapperTest {

    @Value("${FHIRTRANSFAIR_CSVMAPPING_PATH}")
    private String csv_path_config;

    @Value("${FHIRTRANSFAIR_MAPPER_SETTING}")
    private String mapper_setting;

    private static final Logger log = LoggerFactory.getLogger(IDMapperTest.class);

    public IDMapperTest(){}

    /**
     * Tests that instance of the converter {@link IDMapper} is set up correctly and that it maps ids between the correct domains
     */
    @Test
    void idMapper(){
        String bbmri_patient_id = "A";
        String mii_patient_id = "1";

        String bbmri_specimen_id = "B";
        String mii_specimen_id = "2";

        String mapping_string = "BBMRI.Patient,MII.Patient,BBMRI.Specimen,MII.Specimen\n"+bbmri_patient_id+","+mii_patient_id+"\n,,"+bbmri_specimen_id+","+mii_specimen_id;

        try {
            File file = new File(this.csv_path_config);
            FileWriter writer = new FileWriter(file);
            file.deleteOnExit();
            writer.write(mapping_string);
            writer.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }


        IDMapper idmapper = new IDMapper();
        idmapper.setup();

        // Check whether settings are imported correctly
        assertEquals(this.mapper_setting, idmapper.getMapper_setting());
        assertEquals(this.csv_path_config, idmapper.getCsv_mappings_path());

        // Test mapping MII->BBMRI
        assertEquals(bbmri_patient_id, idmapper.toBbmri(mii_patient_id, Resource_Type.PATIENT));
        assertEquals(bbmri_specimen_id, idmapper.toBbmri(mii_specimen_id, Resource_Type.SPECIMEN));

        //Test mapping BBMRI->MII
        assertEquals(mii_patient_id, idmapper.toMii(bbmri_patient_id, Resource_Type.PATIENT));
        assertEquals(mii_specimen_id, idmapper.toMii(bbmri_specimen_id, Resource_Type.SPECIMEN));
    }
}
