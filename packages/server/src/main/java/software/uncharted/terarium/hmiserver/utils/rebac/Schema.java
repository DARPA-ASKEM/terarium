package software.uncharted.terarium.hmiserver.utils.rebac;

public class Schema {

	public static String schema =
		"""
		definition user {}

		definition group {
			relation creator: user
			relation admin: user
			relation member: user

			permission administrate = admin + creator
			permission membership = member + admin + creator
		}

		definition project {
			relation creator: user
			relation admin: user | group
			relation reader: user | group
			relation writer: user | group

			permission read = reader + reader->membership + writer + writer->membership + admin + admin->membership + creator
			permission write = writer + writer->membership + admin + admin->membership + creator
			permission administrate = admin + admin->membership + creator
		}
		""";

	public enum Type {
		USER("user"),
		GROUP("group"),
		PROJECT("project");

		private final String text;

		Type(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Relationship {
		CREATOR("creator"),
		ADMIN("admin"),
		MEMBER("member"),
		WRITER("writer"),
		READER("reader");

		private final String text;

		Relationship(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Permission {
		NONE("none"),
		READ("read"),
		WRITE("write"),
		MEMBERSHIP("membership"),
		ADMINISTRATE("administrate");

		private final String text;

		Permission(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}
}
