package de.samply.fhirtransfair.converters.IDMapping;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Mapper that maps every ID to itself and ignores any domain arguments. It is also impossible to add any mappings. Respective methods just do nothing.
 * @author jdoerenberg
 */
public class Identity_Mapping extends ID_Mapping{
    /**
     * Overrides parental class method to do nothing
     */
    public void read_mappings(){}

    /**
     * Returns the id itself
     * @param id id that is returned
     * @param src_domain has no effect
     * @param tar_domain has no effect
     * @return
     */
    @Override
    public String map_id(String id, String src_domain, String tar_domain){
        return id;
    }

    /**
     * Overrides parental class method to do nothing
     */
    public void set_mapping(String dom_a, String dom_b, String id_a, String id_b){}

    /**
     * Overrides parental class method to do nothing
     */
    @Override
    public void set_mappings(String dom_a, String dom_b, String[] ids_a, String[] ids_b){}

    /**
     * Overrides parental class method to do nothing
     */
    @SuppressWarnings("unused")
    public void set_mappings(String dom_a, String dom_b, ArrayList<String> ids_a, ArrayList<String> ids_b){}

}
