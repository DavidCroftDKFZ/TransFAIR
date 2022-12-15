package de.samply.transfair.converters.id_mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import de.samply.transfair.converters.IDMapper;
import de.samply.transfair.enums.Resource_Type;
import lombok.extern.slf4j.Slf4j;

/**
 Test classes responsible for the {@link de.samply.transfair.converters.IDMapper}
 */
@SpringBootTest
@TestPropertySource(properties = "TRANSFAIR_MAPPER_SETTING=csvmapping")
@TestPropertySource(properties = "TRANSFAIR_CSVMAPPING_PATH=./test_mapping.csv")
@Slf4j
public class IDMapperTest {

    @Value("${TRANSFAIR_CSVMAPPING_PATH}")
    private String csv_path_config;

    @Value("${TRANSFAIR_MAPPER_SETTING}")
    private String mapper_setting;

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
        try {
            assertEquals(bbmri_patient_id, idmapper.toBbmri(mii_patient_id, Resource_Type.PATIENT));
            assertEquals(bbmri_specimen_id, idmapper.toBbmri(mii_specimen_id, Resource_Type.SPECIMEN));

            //Test mapping BBMRI->MII
            assertEquals(mii_patient_id, idmapper.toMii(bbmri_patient_id, Resource_Type.PATIENT));
            assertEquals(mii_specimen_id, idmapper.toMii(bbmri_specimen_id, Resource_Type.SPECIMEN));
        }catch(Exception e){
            System.out.println("Unexpected exception thrown!");
            e.printStackTrace();
            fail();
        }
    }
}
