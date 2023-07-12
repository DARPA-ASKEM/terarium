package software.uncharted.terarium.hmiserver.models.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Github repository
 * This is a convenience class for the front end to use to display a github repository.  It
 * contains a list of files, and a count of the total number of files in the repo.
 */
@TSModel
public class GithubRepo {

	@TSIgnore
	private static final List<String> CODE_TYPES = new ArrayList<>(List.of(".py", ".m", ".js" ));

	@TSIgnore
	private static final List<String> DATA_TYPES = new ArrayList<>(List.of(".csv", ".json", ".tsv", ".xml", ".yaml", ".yml"));

	@TSIgnore
	private static final List<String> DOCUMENT_TYPES = new ArrayList<>(List.of(".pdf", ".txt", ".md"));

	/**
	 * Categories of files
	 */
	public Map<FileCategory, List<GithubFile>> files;

	public Number totalFiles;

	/**
	 * Constructor for GithubRepo. Requires a non-null list of files.
	 * @param files
	 */
	public GithubRepo(List<GithubFile> files){

		//Initialize our map by adding an empty list for each category
		this.files = Map.of(
				FileCategory.DIRECTORY, new ArrayList<>(),
				FileCategory.CODE, new ArrayList<>(),
				FileCategory.DATA, new ArrayList<>(),
				FileCategory.DOCUMENTS, new ArrayList<>(),
				FileCategory.OTHER, new ArrayList<>()
		);

		//Add data to our map by iterating over our given files and adding them to the appropriate category
		for(GithubFile file : files){
			if(file.type == GithubFile.FileType.FILE){
				if(CODE_TYPES.stream().anyMatch(file.name.toLowerCase()::endsWith)){
					file.setFileCategory(FileCategory.CODE);
					this.files.get(FileCategory.CODE).add(file);
				}
				else if(DATA_TYPES.stream().anyMatch(file.name.toLowerCase()::endsWith)){
					file.setFileCategory(FileCategory.DATA);
					this.files.get(FileCategory.DATA).add(file);
				}
				else if(DOCUMENT_TYPES.stream().anyMatch(file.name.toLowerCase()::endsWith)){
					file.setFileCategory(FileCategory.DOCUMENTS);
					this.files.get(FileCategory.DOCUMENTS).add(file);
				}
				else{
					file.setFileCategory(FileCategory.OTHER);
					this.files.get(FileCategory.OTHER).add(file);
				}
			}
			else if(file.type == GithubFile.FileType.DIR){
				file.setFileCategory(FileCategory.DIRECTORY);
				this.files.get(FileCategory.DIRECTORY).add(file);
			}
		}

		//Alphabetically sort the sublists inside our map
		for(List<GithubFile> fileList : this.files.values()){
			fileList.sort(Comparator.comparing(a -> a.name));
		}

		//Sums the total number of files in our map, excluding directories
		this.totalFiles = this.files.values().stream().mapToInt(List::size).sum() - this.files.get(FileCategory.DIRECTORY).size();

	}

	public enum FileCategory {
		@JsonAlias("directory")
		DIRECTORY("Directory"),
		@JsonAlias("code")
		CODE("Code"),
		@JsonAlias("data")
		DATA("Data"),
		@JsonAlias("documents")
		DOCUMENTS("Documents"),

		@JsonAlias("other")
		OTHER("Other");


		public final String fileCategory;
		FileCategory(final String type) {
			this.fileCategory = type;
		}

		@Override
		@JsonValue
		public String toString() {
			return fileCategory;
		}
	}

}
