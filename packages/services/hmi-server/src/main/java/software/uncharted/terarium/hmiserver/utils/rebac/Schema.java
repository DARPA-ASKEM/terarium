package software.uncharted.terarium.hmiserver.utils.rebac;

public class Schema {
    public static String schema = """
            definition user {}

            definition group {
                relation creqtor: user
                relation admin: user
                relation member: user

                permission administrate = admin + creator
                permission membership = member + admin + creator
            }

            definition datum {
                relation creator: user
                relation admin: user | group
                relation reader: user | group
                relation writer: user | group

                permission read = reader + reader->membership + writer + writer->membership + admin + admin->membership + owner
                permission write = writer + writer->membership + admin + admin->membership + owner
                permission administrate = admin + admin->membership + owner
						}
            """;

    public enum Type {
        USER("user"),
        GROUP("group"),
        DATUM("datum");

        private final String text;
        Type(String text) { this.text = text; }

        @Override
        public String toString() { return text; }
    }

    public enum Relationship {
        OWNER("owner"),
        ADMIN("admin"),
        WRITER("writer"),
        READER("reader"),
        INVITED("member");

        private final String text;
        Relationship(String text) { this.text = text; }

        @Override
        public String toString() { return text; }
    }

    public enum Permission {
        READ("read"),
        WRITE("write"),
        ADMINISTRATE("administrate"),
        CHANGE_OWNERSHIP("change_ownership"),
        INVITE("invite"),
        ACCEPT_INVITE("accept_invite"),
        REMOVE_MEMBER("remove_member"),
        PROMOTE("promote");

        private final String text;
        Permission(String text) { this.text = text; }

        @Override
        public String toString() { return text; }
    }
}
