package software.uncharted.terarium.hmiserver.service;

import jakarta.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class HashService {
  /**
   * Hashes the given input using SHA-256 and returns the result as a hex string.
   *
   * @param input
   * @return
   */
  public static String sha256(String input) {
    return sha256(input.getBytes());
  }

  /**
   * Hashes the given input using SHA-256 and returns the result as a hex string.
   *
   * @param bytes The bytes to hash
   * @return The SHA-256 hash of the input as a hex string
   */
  public static String sha256(byte[] bytes) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(bytes);
      return DatatypeConverter.printHexBinary(digest).toLowerCase();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
