package software.uncharted.terarium.hmiserver.models.dataservice.project;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.externalpublication.ExternalPublication;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;

@Data
@Accessors(chain = true)
@Slf4j
@Deprecated
@TSModel
public class Assets implements Serializable {
  private List<Dataset> dataset;
  private List<Extraction> extraction;
  private List<Model> model;
  private List<ExternalPublication> publication;
  private List<Workflow> workflow;
  private List<Artifact> artifact;
  private List<Code> code;
  private List<DocumentAsset> document;
}
