package software.uncharted.terarium.hmiserver.models.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@NoArgsConstructor
@TSModel
@Accessors(chain = true)
public class GithubFile {

	/**
	 * File type, one of "file", "dir", "symlink", "submodule"
	 */
	public FileType type;

	/**
	 * File encoding
	 */
	public String encoding;

	/**
	 * File size
	 */
	public Number size;

	/**
	 * File name
	 */
	public String name;

	/**
	 * File path
	 */
	public String path;

	/**
	 * File content
	 */
	public String content;

	/**
	 * File sha
	 */
	public String sha;

	/**
	 * File url
	 */
	public String url;

	/**
	 * File git url
	 */
	@JsonAlias("git_url")
	public String gitUrl;

	/**
	 * File html url
	 */
	@JsonAlias("html_url")
	public String htmlUrl;

	/**
	 * File download url
	 */
	@JsonAlias("download_url")
	public String downloadUrl;

	/**
	 * File _links
	 */
	@JsonAlias("_links")
	public Links links;

	/**
	 * File submodule git url
	 */
	@JsonAlias("submodule_git_url")
	public String submoduleGitUrl;

	/**
	 * File target
	 */
	public String target;

	/**
	 * File type
	 */
	@Setter(AccessLevel.PACKAGE)
	public GithubRepo.FileCategory fileCategory;




	public enum FileType {
		@JsonAlias("file")
		FILE("file"),
		@JsonAlias("dir")
		DIR("dir"),
		@JsonAlias("symlink")
		SYMLINK("symlink"),
		@JsonAlias("submodule")
		SUBMODULE("submodule");


		public final String fileType;
		FileType(final String type) {
			this.fileType = type;
		}

		@Override
		@JsonValue
		public String toString() {
			return fileType;
		}
	}

}
