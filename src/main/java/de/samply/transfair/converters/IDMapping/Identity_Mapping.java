package de.samply.transfair.converters.IDMapping;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    public String map_id(@NotNull String id, @NotNull String src_domain, @NotNull String tar_domain){
        return id;
    }

    /**
     * Overrides parental class method to do nothing
     */
    public void set_mapping(@NotNull String dom_a, @NotNull String dom_b, @NotNull String id_a, @NotNull String id_b){}

    /**
     * Overrides parental class method to do nothing
     */
    @Override
    public void set_mappings(@NotNull String dom_a, @NotNull String dom_b, @NotNull String[] ids_a, @NotNull String[] ids_b){}

    /**
     * Overrides parental class method to do nothing
     */
    @SuppressWarnings("unused")
    public void set_mappings(@NotNull String dom_a, @NotNull String dom_b, @NotNull ArrayList<String> ids_a, @NotNull ArrayList<String> ids_b){}

}
