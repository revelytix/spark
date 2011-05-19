package spark.api.credentials;

import spark.api.Credentials;

public final class NoCredentials implements Credentials {

  public static NoCredentials INSTANCE = new NoCredentials();
  
  private NoCredentials() {}
  
  @Override
  public int hashCode() { 
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NoCredentials;
  }  
}
