package de.samply.transfair.converters.IdMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import de.samply.transfair.converters.IdMapper;
import de.samply.transfair.enums.ResourceType;
import lombok.extern.slf4j.Slf4j;

/**
 Test classes responsible for the {@link IdMapper}
 */
@SpringBootTest
@TestPropertySource(properties = "TRANSFAIR_MAPPER_SETTING=csvmapping")
@TestPropertySource(properties = "TRANSFAIR_CSVMAPPING_PATH=./test_mapping.csv")
@Slf4j
public class IdMapperTest {

    @Value("${TRANSFAIR_CSVMAPPING_PATH}")
    private String csv_path_config;

    @Value("${TRANSFAIR_MAPPER_SETTING}")
    private String mapper_setting;

    public IdMapperTest(){}

    /**
     * Tests that instance of the converter {@link IdMapper} is set up correctly and that it maps ids between the correct domains
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


        IdMapper idmapper = new IdMapper();
        idmapper.setup();

        // Check whether settings are imported correctly
        assertEquals(this.mapper_setting, idmapper.getMapperSetting());
        assertEquals(this.csv_path_config, idmapper.getCsvMappingsPath());

        // Test mapping MII->BBMRI
        try {
            assertEquals(bbmri_patient_id, idmapper.toBbmri(mii_patient_id, ResourceType.PATIENT));
            assertEquals(bbmri_specimen_id, idmapper.toBbmri(mii_specimen_id, ResourceType.SPECIMEN));

            //Test mapping BBMRI->MII
            assertEquals(mii_patient_id, idmapper.toMii(bbmri_patient_id, ResourceType.PATIENT));
            assertEquals(mii_specimen_id, idmapper.toMii(bbmri_specimen_id, ResourceType.SPECIMEN));
        }catch(Exception e){
            System.out.println("Unexpected exception thrown!");
            e.printStackTrace();
            fail();
        }
    }
}
