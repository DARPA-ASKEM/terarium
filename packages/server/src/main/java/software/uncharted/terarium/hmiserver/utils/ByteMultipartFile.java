package software.uncharted.terarium.hmiserver.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public class ByteMultipartFile implements MultipartFile {

	private final byte[] content;
	private final String name;
	private final String contentType;

	public ByteMultipartFile(final byte[] content, final String name, final String contentType) {
		this.content = content;
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalFilename() {
		return name;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return content == null || content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return content;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(final File dest) throws IOException, IllegalStateException {
		new FileOutputStream(dest).write(content);
	}
}
