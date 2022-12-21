package de.samply.transfair.converters;

import java.util.Objects;

/** Static methods for converting the bbmri.de sample types to SnomedCT codes and back. */
public class SnomedSamplyTypeConverter {

  /** From SnomedCT to bbmri.de sample type. */
  public static String fromMiiToBbmri(String snomedType) {
    return switch (snomedType) {
      case "119297000" -> "whole-blood";
      case "119359002" -> "bone-marrow";
      case "258587000" -> "buffy-coat";
      case "119294007" -> "dried-whole-blood";
      case "404798000" -> "peripheral-blood-cells-vital";
      case "119361006" -> "blood-plasma";
      case "708049000" -> "plasma-edta";
      case "708048008" -> "plasma-citrat";
      case "258958007", "446272009" -> "plasma-heparin";
      case "119364003" -> "blood-serum";
      case "258441009" -> "ascites";
      case "258450006" -> "csf-liquor";
      case "119342007" -> "saliva";
      case "119339001" -> "stool-faeces";
      case "122575003" -> "urine";
      case "257261003" -> "swab";
      case "441652008" -> "tissue-ffpe";
      case "16214131000119104" -> "tissue-frozen";
      case "119376003" -> "tissue-other";
      case "258566005" -> "dna";
      case "726740008" -> "cf-dna";
      case "18470003" -> "g-dna"; // Check
      case "441673008" -> "rna";
      case "33463005" -> "liquid-other";
      default -> "derivative-other";
    };
  }

  /** From bbmri.de to SnomedCT sample type. */
  public static String fromBbmriToMii(String bbmriType) {
    String defaultSnomedcode = "123038009";
    if (Objects.equals(bbmriType, null)) {
      return defaultSnomedcode;
    }
    return switch (bbmriType) {
      case "whole-blood" -> "119297000";
      case "bone-marrow" -> "119359002";
      case "buffy-coat" -> "258587000";
      case "dried-whole-blood" -> "119294007";
      case "peripheral-blood-cells-vital" -> "404798000";
      case "blood-plasma", "plasma-cell-free", "plasma-other" -> "119361006";
      case "plasma-edta" -> "708049000";
      case "plasma-citrat" -> "708048008";
      case "plasma-heparin" -> "446272009";
      case "blood-serum" -> "119364003";
      case "ascites" -> "258441009";
      case "csf-liquor" -> "258450006";
      case "saliva" -> "119342007";
      case "stool-faeces" -> "119339001";
      case "urine" -> "122575003";
      case "swab" -> "257261003";
      case "tissue-ffpe",
          "tumor-tissue-ffpe",
          "normal-tissue-ffpe",
          "other-tissue-ffpe" -> "441652008";
      case "tissue-frozen",
          "tumor-tissue-frozen",
          "normal-tissue-frozen",
          "other-tissue-frozen" -> "16214131000119104";
      case "tissue-other" -> "119376003";
      case "dna" -> "258566005";
      case "cf-dna" -> "726740008";
      case "g-dna" -> "18470003"; // Check
      case "rna" -> "441673008";
      case "liquid-other" -> "33463005";
      default -> defaultSnomedcode;
    };
  }
}
