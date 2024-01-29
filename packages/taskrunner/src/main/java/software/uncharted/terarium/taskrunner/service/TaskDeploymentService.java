package software.uncharted.terarium.taskrunner.service;

public class TaskDeploymentService {

	// @Value()

	// private void deployRepository() {
	// String url =
	// "https://github.com/username/repo/archive/refs/tags/v1.0.tar.gz";
	// String outputDir = "/path/to/output/directory";

	// // Download the tarball
	// InputStream in = new URL(url).openStream();
	// Files.copy(in, Paths.get("repo.tar.gz"),
	// StandardCopyOption.REPLACE_EXISTING);

	// // Extract the tarball
	// try (InputStream fi = Files.newInputStream(Paths.get("repo.tar.gz"));
	// BufferedInputStream bi = new BufferedInputStream(fi);
	// GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
	// TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

	// TarArchiveEntry entry;
	// while ((entry = (TarArchiveEntry) ti.getNextEntry()) != null) {
	// Path outputPath = Paths.get(outputDir, entry.getName());
	// if (entry.isDirectory()) {
	// Files.createDirectories(outputPath);
	// } else {
	// Files.createDirectories(outputPath.getParent());
	// try (OutputStream outputFileStream = Files.newOutputStream(outputPath)) {
	// IOUtils.copy(ti, outputFileStream);
	// }
	// }
	// }
	// }
	// }
}
