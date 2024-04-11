package software.uncharted.terarium.esingest.models.output;

import lombok.Data;

@Data
public class Embedding {

  private String embeddingId;
  private double[] vector;
  private long[] spans;
}
