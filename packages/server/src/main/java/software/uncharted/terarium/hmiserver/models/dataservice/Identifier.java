package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;


@TSModel
public record Identifier(String curie, String name) implements Serializable {
	private static final long serialVersionUID = 302308407252037615L;
}
