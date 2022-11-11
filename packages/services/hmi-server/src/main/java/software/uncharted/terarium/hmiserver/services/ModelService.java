package software.uncharted.terarium.hmiserver.services;

import software.uncharted.terarium.hmiserver.models.Model;
import java.util.List;
import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ModelService {
	public List<Model> getModels() {
		List<Model> list=new ArrayList<Model>();
		return list;
	}
}
