package de.samply.transfair.converters.id_mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

/**
  Test classes responsible for ID mapping. In particular {@link ID_Mapping}, {@link CSV_Mapping} and {@link Identity_Mapping}.
 */
@Slf4j
public class IDMappingTest {

  private final String csv_path_config;
  String dom_A;
  String dom_B;

  String[] IDs_A;
  String[] IDs_B;

  public IDMappingTest(){
    this.csv_path_config = "./test_mapping.csv";
    this.dom_A = "Domain_A";
    this.dom_B = "Domain_B";
    this.IDs_A = new String[]{"A", "B", "C", "D", "E"};
    this.IDs_B = new String[]{"1", "2", "3", "4", "5"};
  }

  /**
   * Test construction of {@link CSV_Mapping} object which inherits from ID_Mapping
   */
  @Test
  void generateObject() {
    new CSV_Mapping();
    new CSV_Mapping(csv_path_config);
    System.gc();
  }

  /**
   * Test set_mappings and set_mapping function of class {@link ID_Mapping} by using instance of inherited class {@link CSV_Mapping}
   */
  @Test
  void addMapping() {
    ID_Mapping mapping = new CSV_Mapping();

    // Test exceptions
    String[] too_short = {"A"};
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      mapping.set_mappings(dom_A, dom_B, too_short, IDs_B); //Should throw IllegalArgumentException, because ID lists do not have the same length
    });
    assertTrue(exception.getMessage().contains("ID lists must have same length! First list has length "+too_short.length+" and second list has length "+IDs_B.length));

    String theSame = "TheSame";
    exception = assertThrows(IllegalArgumentException.class, () -> {
      mapping.set_mapping(theSame, theSame, IDs_A[0], IDs_B[0]); // Should throw IllegalArgumentException, because domain names are equal
    });
    assertTrue(exception.getMessage().contains("Equal domain names not allowed! Both domain names are '"+theSame+"'"));

    exception = assertThrows(IllegalArgumentException.class, () -> {
      mapping.set_mapping("", dom_B, IDs_A[0], IDs_B[0]); // Should throw IllegalArgumentException, because one domain name is empty
    });
    assertTrue(exception.getMessage().contains("Empty domain name is not allowed!"));

    exception = assertThrows(IllegalArgumentException.class, () -> {
      mapping.set_mapping(dom_A, dom_B, "", IDs_B[0]); //Should throw IllegalArgumentException, because empty ID is not allowed
    });
    assertTrue(exception.getMessage().contains("Empty ID is not allowed!"));

    //Add single mapping and test whether it is stored correctly
    mapping.set_mapping(dom_A, dom_B, IDs_A[0], IDs_B[0]);
    assertTrue(mapping.getMappings_cache().containsKey(dom_A));
    assertTrue(mapping.getMappings_cache().get(dom_A).containsKey(dom_B));
    HashMap<String, String> A_to_B = mapping.getMappings_cache().get(dom_A).get(dom_B); //
    assertEquals(IDs_B[0], A_to_B.get(IDs_A[0]));

    assertTrue(mapping.getMappings_cache().containsKey(dom_B));
    assertTrue(mapping.getMappings_cache().get(dom_B).containsKey(dom_A));
    HashMap<String, String> B_to_A = mapping.getMappings_cache().get(dom_B).get(dom_A);
    assertEquals(IDs_A[0], B_to_A.get(IDs_B[0]));

    // Add multiple mappings batch-wise and test whether they are stored correctly
    mapping.set_mappings(dom_A,dom_B, IDs_A, IDs_B);
    for(int i=0; i<IDs_A .length; i++){
      assertEquals(IDs_B[i], A_to_B.get(IDs_A[i]));
    }
    for(int i=0; i<IDs_B.length; i++){
      assertEquals(IDs_A[i], B_to_A.get(IDs_B[i]));
    }

  }

  /**
   * Test map_id function of class {@link ID_Mapping} when reading from cache. Instance of inherited class {@link CSV_Mapping} is used.
   */
  @Test
  void mapID() {
    ID_Mapping mapping = new CSV_Mapping();
    mapping.set_mappings(dom_A, dom_B, IDs_A, IDs_B); // Add mappings to be tested

    int idx = 1; // Use mapping at index 1 for whole test function

    // Test exceptions
    String non_existing_domain = "non_existing_domain";
    Exception exception = assertThrows(Exception.class, () -> {
      mapping.map_id(IDs_A[idx], non_existing_domain, dom_B); // Should throw Exception, because mapping between these domains does not exist
    });

    String non_existing_id = "Non_existing_ID";
    exception = assertThrows(Exception.class, () -> {
      mapping.map_id(non_existing_id, dom_A, dom_B); // Should throw Exception, because ID does not exist in source domain
    });

    // Test whether correct mapping is returned in both directions
    try {
      assertEquals(IDs_B[idx], mapping.map_id(IDs_A[idx], dom_A, dom_B));
      assertEquals(IDs_A[idx], mapping.map_id(IDs_B[idx], dom_B, dom_A));
    }catch(Exception e){ //Should usually not happen
      log.error("Unexpected exception thrown!");
      fail();
    }
  }

  /**
   * Test import of mappings in {@link CSV_Mapping} from csv file
   */
  @Test
  void csvImport() {

    File file;
    FileWriter writer;
    String filepath_broken = "./broken_file.csv";

    String dom_C = "Domain_C";
    String[] IDs_A = {"A", "B", "", ""}; // The fact that row[3] is smaller than the others should not lead to an exception during the test!
    String[] IDs_B = {"1", "2", "3", "", ""};
    String[] IDs_C = {"alpha", "", "gamma", "", "epsilon"};

    //Create test mapping as csv file
    StringBuilder mapping_string = new StringBuilder(dom_A + "," + dom_B + "," + dom_C + "\n");
    for (int i = 0; i < IDs_A.length; i++) {
      mapping_string.append(IDs_A[i]).append(",").append(IDs_B[i]).append(",").append(IDs_C[i]).append("\n");
    }

    try {
      file = new File(csv_path_config);
      writer = new FileWriter(file);
      file.deleteOnExit();
      writer.write(mapping_string.toString());
      writer.close();

      file = new File(filepath_broken);
      writer = new FileWriter(file);
      file.deleteOnExit();
      writer.write("\",\n");
      writer.close();

    } catch (IOException ex) {
      log.error(ex.getMessage());
    }

    CSV_Mapping csv_mapping = new CSV_Mapping();

    // Test exceptions
    assertThrows(Exception.class, () -> { //TODO: Assert based on second exception in stack and based on message of second exception?
      csv_mapping.setFilepath("./Non_existing_file.csv");
      csv_mapping.map_id(IDs_A[0], dom_A, dom_B); // Should throw IOException, because file can not be read
    });

    assertThrows(Exception.class, () -> { //TODO: Assert based on second exception in stack and based on message of second exception?
      csv_mapping.setFilepath(filepath_broken);
      csv_mapping.map_id(IDs_A[0], dom_A, dom_B); // Should throw CsvMalformedLineException, because file is not a proper csv
    });

    // Test if mappings are read correctly
    // Use modified this.IDs_A and this.IDs_B (see top of method)
    csv_mapping.setFilepath(csv_path_config); //TODO: Is this really the correct filename?

    //Index 0: Mapping between all three
    try {
      assertEquals(IDs_B[0], csv_mapping.map_id(IDs_A[0], dom_A, dom_B)); //A->B
      assertEquals(IDs_A[0], csv_mapping.map_id(IDs_B[0], dom_B, dom_A)); //B->A
      assertEquals(IDs_C[0], csv_mapping.map_id(IDs_A[0], dom_A, dom_C)); //A->C
      assertEquals(IDs_A[0], csv_mapping.map_id(IDs_C[0], dom_C, dom_A)); //C->A
      assertEquals(IDs_C[0], csv_mapping.map_id(IDs_B[0], dom_B, dom_C)); //B->C
      assertEquals(IDs_B[0], csv_mapping.map_id(IDs_C[0], dom_C, dom_B)); //C->B
    }catch(Exception e){ //Should usually not happen
      log.error("Unexpected exception thrown!");
      log.error(e.getStackTrace().toString());
      fail();
    }

    //Index 1: Mapping between domain A and domain B
    try{
      assertEquals(IDs_B[1], csv_mapping.map_id(IDs_A[1], dom_A, dom_B)); //A->B
      assertEquals(IDs_A[1], csv_mapping.map_id(IDs_B[1], dom_B, dom_A)); //B->A
    }catch(Exception e){ //Should usually not happen
      log.error("Unexpected exception thrown!");
      log.error(e.getStackTrace().toString());
      fail();
    }
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_A[1], dom_A, dom_C)); //A->C //TODO: Specify exception type?
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[1], dom_C, dom_A)); //C->A
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[1], dom_B, dom_C)); //B->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[1], dom_C, dom_B)); //C->B

    //Index 2: Mapping between domain B and domain C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_A[2], dom_A, dom_B)); //A->B
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[2], dom_B, dom_A)); //B->A
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_A[2], dom_A, dom_C)); //A->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[2], dom_C, dom_A)); //C->A
    try{
      assertEquals(IDs_C[2], csv_mapping.map_id(IDs_B[2], dom_B, dom_C)); //B->C
      assertEquals(IDs_B[2], csv_mapping.map_id(IDs_C[2], dom_C, dom_B)); //C->B
    }catch(Exception e){ //Should usually not happen
      log.error("Unexpected exception thrown!");
      log.error(e.getStackTrace().toString());
      fail();
    }

    //Index 3: No mapping as all three empty
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_A[3], dom_A, dom_B)); //A->B
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[3], dom_B, dom_A)); //B->A
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_A[3], dom_A, dom_C)); //A->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[3], dom_C, dom_A)); //C->A
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[3], dom_B, dom_C)); //B->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[3], dom_C, dom_B)); //C->B

    //Index 4: No mapping, as there is just ID in domain C
    // Domain A does not have this index //A->B
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[4], dom_B, dom_A)); //B->A
    // Domain A does not have this index //A->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[4], dom_C, dom_A)); //C->A
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_B[4], dom_B, dom_C)); //B->C
    assertThrows(Exception.class, () -> csv_mapping.map_id(IDs_C[4], dom_C, dom_B)); //C->B
  }

  /**
   * Tests that the identity mapper {@link Identity_Mapping} returns the input id
   */
  @Test
  void identityMapper(){
    String id = "A";
    Identity_Mapping identity_mapper = new Identity_Mapping();
    assertEquals(id, identity_mapper.map_id(id, dom_A, dom_B)); // id itself should be returned in both directions
    assertEquals(id, identity_mapper.map_id(id, dom_B, dom_A)); // id itself should be returned in both directions
  }

}
