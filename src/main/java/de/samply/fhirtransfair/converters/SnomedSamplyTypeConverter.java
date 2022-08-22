package de.samply.fhirtransfair.converters;

public class SnomedSamplyTypeConverter {

  public static String fromMiiToBbmri(String snomedType) {
    return switch (snomedType) {
      case "420135007" -> "whole-blood";
      case "14016003" -> "bone-marrow";
      case "258587000" -> "buffy-coat";
      case "119294007" -> "dried-whole-blood";
      case "404798000" -> "peripheral-blood-cells-vital"; // Right?
      case "419185008" -> "blood-plasma";
      case "708049000" -> "plasma-edta";
      case "708048008" -> "plasma-citrat";
      case "258958007" -> "plasma-heparin";
      case "50863008" -> "plasma-other";
      case "122591000" -> "blood-serum";
      case "389026000" -> "ascites"; //Check
      case "65216001" -> "csf-liquor";
      case "256897009" -> "saliva";
      case "39477002" -> "stool-faeces";
      case "78014005" -> "urine";
      case "257261003" -> "swab";
      case "441652008" -> "tissue-ffpe";
      case "16214131000119104" -> "tissue-frozen";
      case "85756007" -> "tissue-other"; //Check
      case "258566005" -> "dna";
      case "726740008" -> "cf-dna";
      case "18470003" -> "g-dna"; //Check
      case "27888000" -> "rna";
      case "33463005" -> "liquid-other";
      default -> "derivative-other";
    };
  }

  public static String fromBbmriToMii(String BbmriType) {
    return switch (BbmriType) {
      case "whole-blood" -> "420135007";
      case "bone-marrow" -> "14016003";
      case "buffy-coat" -> "258587000";
      case "dried-whole-blood" -> "119294007";
      case "peripheral-blood-cells-vital" -> "404798000";
      case "blood-plasma", "plasma-cell-free" -> "419185008";
      case "plasma-edta" -> "708049000";
      case "plasma-citrat" -> "708048008";
      case "plasma-heparin" -> "258958007";
      case "plasma-other" -> "50863008"; //Check
      case "blood-serum" -> "122591000";
      case "ascites" -> "389026000";
      case "csf-liquor" -> "65216001";
      case "saliva" -> "256897009";
      case "stool-faeces" -> "39477002";
      case "urine" -> "78014005";
      case "swab" -> "257261003";
      case "tissue-ffpe", "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe" -> "441652008";
      case "tissue-frozen", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen" -> "16214131000119104";
      case "tissue-other" -> "413675001";
      case "dna" -> "258566005";
      case "cf-dna" -> "726740008";
      case "g-dna" -> "18470003"; //Check
      case "rna" -> "27888000";
      case "liquid-other" -> "33463005";
      default -> "123038009";
    };
  }
}
