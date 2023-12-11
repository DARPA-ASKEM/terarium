package software.uncharted.terarium.hmiserver.models.funman;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.funman.parts.FunmanWorkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanPostQueriesRequest implements Serializable{
   private Model model;
   private FunmanWorkRequest request;
}