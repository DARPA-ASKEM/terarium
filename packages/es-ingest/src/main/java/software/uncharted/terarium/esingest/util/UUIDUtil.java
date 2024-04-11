package software.uncharted.terarium.esingest.util;

import java.util.Random;
import java.util.UUID;

public class UUIDUtil {

  public static UUID generateSeededUUID(String seed) {
    Random random = new Random(seed.hashCode());

    long mostSigBits = random.nextLong();
    long leastSigBits = random.nextLong();

    return new UUID(mostSigBits, leastSigBits);
  }
}
