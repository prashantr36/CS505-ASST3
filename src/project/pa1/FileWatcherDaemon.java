package project.pa1;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;

public class FileWatcherDaemon implements Runnable {
	
	private RMIServerInterfaceImpl rsimpl;

	public FileWatcherDaemon (RMIServerInterfaceImpl rsimpl) {
		this.rsimpl = rsimpl;
	}
	@Override
	public void run() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(this.rsimpl.user_directory);
		    WatchKey key = dir.register(watcher,StandardWatchEventKinds.ENTRY_CREATE,
		    		StandardWatchEventKinds.ENTRY_DELETE, 
		    		StandardWatchEventKinds.ENTRY_MODIFY);
		    
		    Set<String> fileNamesModified = new HashSet<String>();
		    Set<String> fileNamesDeleted = new HashSet<String>();
		    Set<String> fileNamesCreated = new HashSet<String>();
		    while(true) {
		        try {
		        	key = watcher.take();
		        } catch (InterruptedException x) {
		            return;
		        }
		        for (WatchEvent<?> event: key.pollEvents()) {
		        	WatchEvent.Kind<?> kind = event.kind();

		            // This key is registered only
		            // for ENTRY_CREATE events,
		            // but an OVERFLOW event can
		            // occur regardless if events
		            // are lost or discarded.
		            if (kind == StandardWatchEventKinds.OVERFLOW) {
		                continue;
		            }

		            // The filename is the
		            // context of the event.
		            @SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
		            Path filename = ev.context();
		            
		            // Verify that the new
		            //  file is a text file.
		            try {
		                // Resolve the filename against the directory.
		                // If the filename is "test" and the directory is "foo",
		                // the resolved name is "test/foo".
		            	int i = filename.toString().lastIndexOf('.');
		                if (i < 0) {
		                	continue;
		                }
		                if(!filename.toString().substring(i).equalsIgnoreCase(".txt")) {
		                	continue;
		                }
		                Path child = dir.resolve(filename);
		                if (!Files.probeContentType(child).equals("text/plain")) {
		                    System.err.format("New file '%s'" +
		                        " is not a plain text file.%n", filename);
		                    continue;
		                }
		                
		            } catch (IOException x) {
		                System.err.println(x);
		                continue;
		            }
		            
		            if (kind == StandardWatchEventKinds.OVERFLOW) {
		                 continue;
		                }
		            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
		            	fileNamesDeleted.add(filename.toString());
		            }

					if((kind==StandardWatchEventKinds.ENTRY_MODIFY) ||  kind==StandardWatchEventKinds.ENTRY_CREATE){
		            	fileNamesCreated.add(filename.toString());
		            }
					
		            for(String fileNameCreate: fileNamesCreated) {
		            	this.rsimpl.PUT(this.rsimpl.local_hostname  + ":" + this.rsimpl.local_port, fileNameCreate, "");
		            }
		            
		            for(String fileNameDelete: fileNamesDeleted) {
		            	this.rsimpl.DELETE(this.rsimpl.local_hostname  + ":" + this.rsimpl.local_port, fileNameDelete);
		            }
		            fileNamesModified = new HashSet<String>();
				    fileNamesCreated = new HashSet<String>();
				    fileNamesDeleted = new HashSet<String>();
		        }

		        boolean valid = key.reset();
		        if (!valid) {
		            break;
		        }
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}
	};
	

}
