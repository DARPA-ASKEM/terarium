package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.models.dataservice.permission.Group;
import software.uncharted.terarium.hmiserver.models.dataservice.permission.User;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

import java.util.ArrayList;
import java.util.List;

public class ProjectPermissions {
	private List<Group> groups = new ArrayList<>();
	private List<User> users = new ArrayList<>();

	public void addUser(String id, Schema.Relationship relationship) {
		users.add(new User(id, relationship.toString()));
	}

	public void addGroup(String id, Schema.Relationship relationship) {
		groups.add(new Group(id, relationship.toString()));
	}

	public List<Group> getGroups() { return groups; }
	public List<User> getUsers() { return users; }
}
