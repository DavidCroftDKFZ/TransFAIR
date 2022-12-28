package de.samply.transfair;

/**
 * Global parameter storage.
 */
// TODO: This is probably not the right way to do this under Spring, refactoring necessary!
public class TempParams {
  private static String saveToFilePath;

  public static String getSaveToFilePath() {
    return TempParams.saveToFilePath;
  }

  public static void setSaveToFilePath(String saveToFilePath) {
    TempParams.saveToFilePath = saveToFilePath;
  }
}
