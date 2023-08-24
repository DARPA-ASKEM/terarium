package software.uncharted.terarium.hmiserver.utils.rebac;

public class Schema {
    public static String schema = """
            definition user {}

            definition group {
                relation owner: user | group
                relation admin: user
                relation writer: user
                relation reader: user
                relation invited: user

                permission administrate = admin + owner + owner->administrate
                permission read = reader + writer + admin + owner + owner->read
                permission write = writer + admin + owner + owner->write
                permission invite = reader + writer + admin + owner + owner->invite
                permission accept_invite = admin + owner + owner->accept_invite
                permission remove_member = admin + owner + owner->remove_member
                permission promote = admin + owner + owner->promote
            }
                            
            definition datum {
                relation owner: user | group
                relation admin: user
                relation reader: user
                relation writer: user
                            
                permission read = reader + writer + admin + owner + owner->read
                permission write = writer + admin + owner + owner->write
                permission administrate = admin + owner + owner->administrate
                permission change_ownership = owner + owner->owner
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
        INVITED("invited");

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
