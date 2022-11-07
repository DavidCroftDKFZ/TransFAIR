package de.samply.transfair.converters.IDMapping;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

/**
 * Reads ID-Mappings from csv file. See read_mappings method for details. Access to the mappings is provided by using the methods from the parent class.
 * @see this.read_mappings
 * @see ID_Mapping
 * @author jdoerenberg
 */
public class CSV_Mapping extends ID_Mapping {

    private String filepath;

    /**
     * Standard constructor
     */
    public CSV_Mapping(){
        super();
    }

    /**
     * Constructor which stores filepath
     * @param s filepath to csv file where mappings are loaded from
     */
    @SuppressWarnings("unused")
    public CSV_Mapping(@NotNull String s){
        super();
        this.filepath = s;
    }

    /**
     * Standard setter for field this.filepath
     * @param s  filepath to csv file where mappings are loaded from
     */
    public void setFilepath(@NotNull String s){
        this.filepath = s;
    }

    /**
     * Standard getter for field this.filepath
     * @return filepath to csv file where mappings are loaded from
     */
    @SuppressWarnings("unused")
    public String getFilepath(){
        return this.filepath;
    }

    /**
     * Reads ID mappings from a csv file and stores them in the data structure provided in parental class.
     * The column headlines (first row) contain the names of the domains mapped to each other. After reading these, mappings are processed row by row.
     * If two domains contain a non-empty value, these values are stored in the mapping. This is done for each pair of non-empty values in the row.
     * @throws IOException in case file at this.filepath cannot be read (e.g. opened by different program, does not exist,...)
     * @throws CsvException in case file at this.filepath is not a proper csv file
     */
    @Override
    public void read_mappings() throws IOException, CsvException { //TODO: Automatically call this after
        CSVReader reader = new CSVReader(new FileReader(filepath));
        String[] row;

        String[] domains = reader.readNext();

        while ((row = reader.readNext()) != null) {
            // Pair each domain with each domain
            // //use indices of row as a row could be shorter than the number of domains e.g. because there are no values in the last domain
            for(int a=0;a<row.length;a++){
                for(int b=a+1;b<row.length;b++){
                    if(!row[a].equals("") && !row[b].equals("")){ //Check whether there is a value und each of the two domains
                        this.set_mapping(domains[a], domains[b], row[a], row[b]);
                    }
                }
            }
        }
    }
}
