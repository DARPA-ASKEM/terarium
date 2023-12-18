package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
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
public class Assets implements Serializable {
	List<Dataset> datasets;
	List<Extraction> extractions;
	List<Model> models;
	List<ExternalPublication> publications;
	List<Workflow> workflows;
	List<Artifact> artifacts;
	List<Code> code;
	List<DocumentAsset> documents;

}
