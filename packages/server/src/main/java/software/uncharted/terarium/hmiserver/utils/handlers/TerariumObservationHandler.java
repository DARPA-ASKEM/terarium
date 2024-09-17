package software.uncharted.terarium.hmiserver.utils.handlers;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TerariumObservationHandler implements ObservationHandler<Observation.Context> {

	@Override
	public boolean supportsContext(final Observation.Context context) {
		return true;
	}
}
