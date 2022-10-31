package de.samply.fhirtransfair.converters.IDMapping;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores mappings between IDs from multiple domains in nested hashmaps. A mapping is always possible in both directions from domain A to domain B and vice versa.
 * @author jdoerenberg
 */
public abstract class ID_Mapping {
    //First key: src domain, second key tar domain, third key identifier
    private final HashMap<String, HashMap<String, HashMap<String, String>>> mappings;

    /**
     * Standard constructor. Prepares nested hashmaps to store mappings in.
     */
    public ID_Mapping(){
        this.mappings = new HashMap<>();
    }

    /**
     * Standard getter for field this.mappings
     * @return nested Hashmaps which store mappings
     */
    public HashMap<String, HashMap<String, HashMap<String, String>>> getMappings(){
        return this.mappings;
    }

    /**
     * Reads mappings from different sources depending on inherited class.
     * @throws Exception Exceptions depend on implementation in inherited class. E.g. when reading from a file it can be IOException.
     */
    public abstract void read_mappings() throws Exception;

    /**
     * Stores a single mapping between two IDs from two domains.
     * @param dom_a name of the first domain
     * @param dom_b name of the second domain
     * @param id_a id from the first domain
     * @param id_b id from the second domain
     * @throws IllegalArgumentException is thrown if
     */
    public void set_mapping(@NotNull String dom_a, @NotNull String dom_b, @NotNull String id_a, @NotNull String id_b) throws IllegalArgumentException{

        // check whether domain names are different
        if (dom_a.equals(dom_b)) {
            throw new IllegalArgumentException("Equal domain names not allowed! Both domain names are '" + dom_a + "'");
        }

        if (dom_a.equals("") || dom_b.equals("")) {
            throw new IllegalArgumentException("Empty domain name is not allowed!");
        }

        // Check whether both ids are not empty
        if (id_a.equals("") ||id_b.equals("")) {
            throw new IllegalArgumentException("Empty ID is not allowed!");
        }

        if(!this.mappings.containsKey(dom_a)) {
            this.mappings.put(dom_a, new HashMap<>());
        }
        if(!this.mappings.containsKey(dom_b)){
            this.mappings.put(dom_b, new HashMap<>());
        }

        if(!this.mappings.get(dom_a).containsKey(dom_b)) {
            this.mappings.get(dom_a).put(dom_b, new HashMap<>());
        }
        if(!this.mappings.get(dom_b).containsKey(dom_a)){
            this.mappings.get(dom_b).put(dom_a, new HashMap<>());
        }

        this.mappings.get(dom_a).get(dom_b).put(id_a, id_b);
        this.mappings.get(dom_b).get(dom_a).put(id_b, id_a);

    }

    /**
     * Stores multiple mappings between two input domains. While iterating over both id arrays at a time, pairs of IDs are fed to the {@link this.set_mapping} method.
     * @param dom_a name of first domain
     * @param dom_b name of second domain
     * @param ids_a array of IDs from domain a
     * @param ids_b array od IDs from domain b
     * @throws IllegalArgumentException If length of ids_a and ids_b is not equal, no mappings are stored. If an exception is thrown by {@link this.set_mapping} method while adding a single mapping, it is escalated and the respective mapping is skipped.
     */
    public void set_mappings(@NotNull String dom_a, @NotNull String dom_b, @NotNull String[] ids_a, @NotNull String[] ids_b) throws IllegalArgumentException {

        if (ids_a.length != ids_b.length) {
            throw new IllegalArgumentException("ID lists must have same length! First list has length " + ids_a.length + " and second list has length " + ids_b.length);
        }

        for(int i=0; i<ids_a.length; i++){
            this.set_mapping(dom_a, dom_b, ids_a[i], ids_b[i]);
        }

    }

    /**
     * Overloads {@link this.set_mappings} by adding the option to pass ArrayLists instead of arrays
     */
    @SuppressWarnings("unused")
    public void set_mappings(@NotNull String dom_a, @NotNull String dom_b, @NotNull ArrayList<String> ids_a, @NotNull ArrayList<String> ids_b) throws IllegalArgumentException{
        this.set_mappings(dom_a, dom_b, ids_a.toArray(new String[0]), ids_b.toArray(new String[0]));
    }

    /**
     * Maps a single id from its original source domain to a target domain and returns it.
     * @param id id that shall be mapped from source domain to target domain
     * @param src_domain domain that contains the id
     * @param tar_domain domain the id shall be mapped to
     * @throws IllegalArgumentException if mapping does not exist
     * @return id from tar_domain which was mapped from input id
     */
    public String map_id(@NotNull String id, @NotNull String src_domain, @NotNull String tar_domain) throws IllegalArgumentException{
        if(!mappings.containsKey(src_domain) || !mappings.get(src_domain).containsKey(tar_domain)) {
            throw new IllegalArgumentException("Mapping from source domain '" + src_domain + "' to target domain '" + tar_domain + "' does not exist!");
        }

        HashMap<String, String> src_to_tar = mappings.get(src_domain).get(tar_domain);
        if(!src_to_tar.containsKey(id)) {
            throw new IllegalArgumentException("ID '" + id + "' does not exist in mapping!");
        }

        return src_to_tar.get(id);
    }

}
