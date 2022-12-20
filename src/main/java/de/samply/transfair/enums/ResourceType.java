package de.samply.transfair.enums;

import de.samply.transfair.converters.IdMapper;

/**
 * Used in class {@link this.set_mapping} to define the FHIR resource type that an id belong to. See
 * {@link IdMapper}.toBbmri and {@link IdMapper}.toMii methods
 *
 * @author jdoerenberg
 */
public enum ResourceType {
  PATIENT,
  SPECIMEN
}
