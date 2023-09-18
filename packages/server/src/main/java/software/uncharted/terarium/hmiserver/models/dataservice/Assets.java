package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;


import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@Slf4j
public class Assets implements Serializable {
	List<Dataset> datasets;
	List<Extraction> extractions;
	List<Model> models;
	List<DocumentAsset> publications;
	List<Workflow> workflows;
	List<Artifact> artifacts;
	List<Code> code;


}
