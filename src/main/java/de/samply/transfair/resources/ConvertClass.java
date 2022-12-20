package de.samply.transfair.resources;

/** Template for Mapping classes. */
public abstract class ConvertClass<T1, T2> {

  public abstract void fromBbmri(T1 resource);

  public abstract void fromMii(T2 resource);

  public abstract T1 toBbmri();

  public abstract T2 toMii();
}
