package software.uncharted.terarium.hmiserver.models.dataservice;

public enum Role {
	AUTHOR("author"),
	CONTRIBUTOR("contributor"),
	MAINTAINER("maintainer"),
	OTHER("other");

	public final String type;

	Role(final String type) {
		this.type = type;
	}
}
