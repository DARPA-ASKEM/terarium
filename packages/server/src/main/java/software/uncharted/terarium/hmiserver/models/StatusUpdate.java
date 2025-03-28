package software.uncharted.terarium.hmiserver.models;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import software.uncharted.terarium.hmiserver.ProgressState;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Builder
@TSModel
public class StatusUpdate<T> implements Serializable {

	private Double progress;
	private ProgressState state;
	private String message;
	private String error;
	private T data; // Additional data to be sent along with the status update
}
