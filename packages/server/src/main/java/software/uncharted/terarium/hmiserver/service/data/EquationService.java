package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.repository.data.EquationRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class EquationService extends TerariumAssetServiceWithoutSearch<Equation, EquationRepository> {

	public EquationService (
		final Config config,
		final ProjectAssetService projectAssetService,
		final EquationRepository repository,
		final S3ClientService s3ClientService) {
		super(config, projectAssetService, repository, s3ClientService, Equation.class);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Equations are not S3 Backed");
	}
}
