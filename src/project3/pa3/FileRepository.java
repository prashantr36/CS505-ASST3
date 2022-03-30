package project3.pa3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	public static String hostName;
	public static int portNumber;
	public final static int FORCE_FILE_STALE_VERSION_NUMBER = -1019;
	public FileRepository(String hostname, int portNumber) {
		FileRepository.hostName = hostname;
		FileRepository.portNumber = portNumber;
	}
	
	public static class FileRepositoryFile implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String filename = "INVALID";
		private long version = 0; // version number
		private String status = "ACCEPT";
		private boolean isMasterClient = false;
		private Instant last_mod_time;
		public FileRepositoryFile() {
			this.filename = "INVALID";
			this.version = 0; // version number
			this.status = "ACCEPT";
			this.isMasterClient = false;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    Instant instant = timestamp.toInstant();
		}
		
		public FileRepositoryFile(String filename) {
			this.filename = filename;
			this.status = "VALID";
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    Instant instant = timestamp.toInstant();
		    this.last_mod_time = instant;
		}
		
		public FileRepositoryFile(FileRepositoryFile fileRepositoryFile) {
			this.filename = fileRepositoryFile.filename;
			this.version = fileRepositoryFile.version;
			this.status = fileRepositoryFile.status;
			this.isMasterClient = fileRepositoryFile.isMasterClient;
			this.last_mod_time = fileRepositoryFile.last_mod_time;
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
			this.status = "FILE OUT OF DATE";
		}
		
		public void setValid() {
			this.status = "VALID";
		}
		
		public void setValidity(String val) {
			this.status = val;
		}
		
		public boolean isValid() {
			return !this.status.equalsIgnoreCase("FILE OUT OF DATE");
		}
		
		public void setIsMasterClient(boolean master_client) {
			this.isMasterClient = master_client;
		}
		
		public boolean isMasterClient() {
			return this.isMasterClient;
		}
		
		public void updateLastModTime() {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    Instant instant = timestamp.toInstant();
		    this.last_mod_time = instant;
		}
		

		@Override
		public String toString() {
			return "FileRepositoryFile [filename=" + filename + ", version=" + version + ", status=" + status + ", isMasterClient="
					+ isMasterClient + " Last-Mod-Time " + this.last_mod_time + 
					" Hostname: " + FileRepository.hostName + " Port: " + FileRepository.portNumber + "]";
		}
	}
	
	private static final ConcurrentHashMap<String, FileRepositoryFile> collection = new ConcurrentHashMap<>();
	private static final int FORCE_STALE = 0;
	
	
	public static void saveJsonToFile(final Object object, String filename) throws IOException {
		try (Writer writer = new FileWriter("file_metadata/" + filename + ".json")) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    ((Map<String, FileRepositoryFile>) object).getOrDefault(filename, new FileRepositoryFile()).updateLastModTime();
		    
		    /*
		     * Verify the master client status is correct
		     */
		    
		    Path path1 = Paths.get("files/" + filename);
		    Path path2 = Paths.get("downloads/" + filename);
		    if(Files.exists(path1)) {
		    	((Map<String, FileRepositoryFile>) object).getOrDefault(filename, new FileRepositoryFile()).setValid();
		    	((Map<String, FileRepositoryFile>) object).getOrDefault(filename, new FileRepositoryFile()).setIsMasterClient(true);
		    } else {
		    	if(Files.exists(path2)) {
		    		((Map<String, FileRepositoryFile>) object).getOrDefault(filename, new FileRepositoryFile()).setIsMasterClient(false);
		    	}
		    }
		    gson.toJson(((Map<String, FileRepositoryFile>) object).getOrDefault(filename, new FileRepositoryFile()), writer);
		    
		    
		}
	}
	
	
	
	/**
	 * Adds file to collection.
	 * Actually we are putting copy of file (saving a file by value, not by reference);
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	
	
	public  synchronized void add(FileRepositoryFile fileRepositoryFile) throws FileDuplicationException, IOException {
		if(collection.containsKey(fileRepositoryFile.getFileName())) {
			if(collection.get(fileRepositoryFile.getFileName()).version == fileRepositoryFile.getVersion())
				throw new FileDuplicationException("Duplicated file with id: " + fileRepositoryFile.getFileName());
		}
		collection.put(fileRepositoryFile.getFileName(), fileRepositoryFile);
		saveJsonToFile(collection, fileRepositoryFile.getFileName());
	}
	
	public  synchronized void update(FileRepositoryFile fileRepositoryFile) throws FileDuplicationException, VersionMismatchException, FileUnFoundException, IOException {
		if(!collection.containsKey(fileRepositoryFile.filename)) {
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
		collection.put(fileRepositoryFile.getFileName(), fileRepositoryFile);
		System.out.println(" UPDATING FILE REPOSITORY VERSION to " +  fileRepositoryFile.getVersion());
		saveJsonToFile(collection, fileRepositoryFile.getFileName());
	}
	
	/*
	 * Returns file representation to the client
	 */
	public synchronized FileRepositoryFile get(String filename) throws FileUnFoundException {
		if(!collection.containsKey(filename)) {
			throw new FileUnFoundException("GetOp: Not found file with filename: " + filename);
		}
		// return copy of the file
		return new FileRepositoryFile(collection.get(filename));
	}
	
	public synchronized void remove(String filename) throws FileUnFoundException, IOException {
		if(!collection.containsKey(filename)) {
			throw new FileUnFoundException("RemOp: Not found file with filename: " + filename);
		}
		// return copy of the file
		collection.remove(filename);
		saveJsonToFile(collection, filename);
	}
	
	
	public synchronized boolean containsFile(String filename)  {
		return collection.containsKey(filename);
	}
	
	public synchronized boolean isStaleThenInvalidate(String filename, int versionNumber) throws FileUnFoundException, IOException {
		if((versionNumber == FORCE_FILE_STALE_VERSION_NUMBER || get(filename).getVersion() < (int) versionNumber
				) && !get(filename).isMasterClient) {
			collection.get(filename).status = "FILE OUT OF DATE";
			saveJsonToFile(collection, filename);
			return true;
		}
		return false;
	}
	
	public synchronized boolean isValidFile(String filename, Long versionNumber) throws FileUnFoundException, IOException {
		if(get(filename).getVersion() >= versionNumber
				&& !get(filename).isMasterClient) {
			saveJsonToFile(collection, filename);
			return true;
		}
		return false;
	}
}
