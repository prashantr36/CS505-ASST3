package project3.pa3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileRepository {
	
	public static class FileDuplicationException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FileDuplicationException(String message) {
			super(message);
		}
	}
	
	public static class FileUnFoundException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FileUnFoundException(String message) {
			super(message);
		}
	}
	
	public static class VersionMismatchException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public VersionMismatchException(String message) {
			super(message);
		}
	}
	
	public FileRepository() {
		
	}
	
	public static class FileRepositoryFile {
		private String filename = "";
		
		private long version = 0; // version number
		private Boolean status = null;
		private Boolean isMasterClient = false;
		
		public FileRepositoryFile() {
		}
		
		public FileRepositoryFile(String filename) {
			this.filename = filename;
			this.status = null;
		}
		public FileRepositoryFile(FileRepositoryFile fileRepositoryFile) {
			this.filename = fileRepositoryFile.filename;
			this.version = fileRepositoryFile.version;
			this.status = false;
			this.isMasterClient = fileRepositoryFile.isMasterClient;
		}
		
		public String getFileName() {
			return this.filename;
		}
		
		public long getVersion() {
			return version;
		}
		
		public void setVersion(long version) {
			this.version = version;
		}
		
		public void setInvalid() {
			this.status = false;
		}
		
		public boolean isValid() {
			return this.status;
		}
		
		public void setIsMasterClient(boolean master_client) {
			this.isMasterClient = master_client;
		}
		
		public boolean isMasterClient() {
			return this.isMasterClient;
		}

		@Override
		public String toString() {
			return "FileRepositoryFile [filename=" + filename + ", version=" + version + ", status=" + status + ", isMasterClient="
					+ isMasterClient + "]";
		}
	}
	
	private static final Map<String, FileRepositoryFile> collection = new HashMap<>();
	
	
	public static void saveJsonToFile(final Object object) throws IOException {
		try (Writer writer = new FileWriter("file_metadata/info.json")) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    gson.toJson(object, writer);
		}
	}
	
	
	
	/**
	 * Adds file to collection.
	 * Actually we are putting copy of file (saving a file by value, not by reference);
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	
	
	public  void add(FileRepositoryFile fileRepositoryFile) throws FileDuplicationException, IOException {
		if(collection.containsKey(fileRepositoryFile.getFileName())) {
			throw new FileDuplicationException("Duplicated file with id: " + fileRepositoryFile.getFileName());
		}
		collection.put(fileRepositoryFile.getFileName(), fileRepositoryFile);
		saveJsonToFile(collection);
	}
	
	public  void update(FileRepositoryFile fileRepositoryFile) throws FileDuplicationException, VersionMismatchException, FileUnFoundException, IOException {
		if( collection.containsKey(fileRepositoryFile.filename)) {
			throw new FileUnFoundException("Not found file with id: " + fileRepositoryFile.getFileName());
		}
		FileRepositoryFile latestFile = collection.get(fileRepositoryFile.getFileName());
		if(fileRepositoryFile.getVersion() != latestFile.getVersion()) {
			throw new VersionMismatchException(
					"Tried to update stale version " + fileRepositoryFile.getVersion()
					+ " while actual version is " + latestFile.getVersion()
					);
		}
		
		//update the file version including the representation - modify the
		//reference here
		fileRepositoryFile.setVersion(fileRepositoryFile.getVersion() + 1);
		//save the file copy to repository
		collection.put(fileRepositoryFile.getFileName(), new FileRepositoryFile(fileRepositoryFile));
		saveJsonToFile(collection);
	}
	
	/*
	 * Returns file representation to the client
	 */
	public FileRepositoryFile get(String filename) throws FileUnFoundException {
		if(!collection.containsKey(filename)) {
			throw new FileUnFoundException("GetOp: Not found file with filename: " + filename);
		}
		// return copy of the file
		return new FileRepositoryFile(collection.get(filename));
	}
	
	public void remove(String filename) throws FileUnFoundException, IOException {
		if(!collection.containsKey(filename)) {
			throw new FileUnFoundException("RemOp: Not found file with filename: " + filename);
		}
		// return copy of the file
		collection.remove(filename);
		saveJsonToFile(collection);
	}
	
	
	public boolean containsFile(String filename)  {
		return collection.containsKey(filename);
	}
	
	public boolean isStaleThenInvalidate(String filename, int versionNumber) throws FileUnFoundException, IOException {
		if(get(filename).getVersion() != (int) versionNumber) {
			collection.get(filename).status = false;
			return true;
		}
		saveJsonToFile(collection);
		return false;
	}
	
}
