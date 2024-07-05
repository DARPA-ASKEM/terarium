package software.uncharted.terarium.hmiserver.models.dataservice.project;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ProjectAndAssetAggregate {
	UUID getId();

	Timestamp getCreatedOn();

	Timestamp getUpdatedOn();

	Timestamp getDeletedOn();

	String getDescription();

	List<String> getFileNames();

	String getName();

	byte[] getOverviewContent();

	Boolean getPublicAsset();

	Boolean getTemporary();

	String getThumbnail();

	String getUserId();

	Integer getAssetCount();

	String getAssetType();
}
