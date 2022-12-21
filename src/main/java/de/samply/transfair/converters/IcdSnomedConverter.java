package de.samply.transfair.converters;

/** Converter between SnomedCT and ICD10-WHO. */
public class IcdSnomedConverter {

  /** Converts a SnomedCT Diagnosis to ICD10. */
  public static String fromSnomed2Icd10Who(String snomed) {
    return switch (snomed) {
      case "195506001" -> "I95.0";
      case "9972008" -> "R52.9";
      case "99741000119100" -> "D09.9";
      case "99751000119103" -> "H18.7";
      case "9977002" -> " S90.8";
      case "399068003" -> "C61";
      default -> "";
    };
  }

  /** Converts a ICD10 Diagnosis to SnomedCT. */
  public static String fromIcd10Who2Snomed(String snomed) {
    return switch (snomed) {
      case "I95.0" -> "195506001";
      case "R52.9" -> "9972008";
      case "D09.9" -> "99741000119100";
      case "H18.7" -> "99751000119103";
      case "S90.8" -> "9977002";
      case "C61" -> "399068003";
      default -> "";
    };
  }
}
