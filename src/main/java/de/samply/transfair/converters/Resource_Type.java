package de.samply.transfair.converters;

/**
 * Used in class {@link this.set_mapping} to define the FHIR resource type that an id belong to. See {@link IDMapper}.toBbmri and {@link IDMapper}.toMii methods
 * @author jdoerenberg
 */
public enum Resource_Type {
    PATIENT,
    SPECIMEN
}
