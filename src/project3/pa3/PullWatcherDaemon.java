package project3.pa3;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import project3.pa3.FileRepository.FileDuplicationException;
import project3.pa3.FileRepository.FileRepositoryFile;
import project3.pa3.FileRepository.FileUnFoundException;
import project3.pa3.FileRepository.VersionMismatchException;
class PullWatcherDaemon {
   private final Integer TTR;
   private final ScheduledExecutorService scheduler;
   private FileRepository file_repository;
   private String key;
   private LeafNodeServerInterface lnsi;
   private String clientId;
   
   public PullWatcherDaemon(Integer TTR,
		   					FileRepository file_repository,
		   					String key, 
		   					LeafNodeServerInterface lnsi,
		   					String clientId) {
	   this.TTR = TTR;
	   this.scheduler = Executors.newScheduledThreadPool(1);
	   this.file_repository = file_repository;
	   this.key = key;
	   this.lnsi = lnsi;
	   this.clientId = clientId;
   }
   
   public void beepForAnHour() {
     final Runnable beeper = new Runnable() {
       public void run() {
    	   FileRepositoryFile fff;
		try {
			fff = file_repository.get(key);
			fff.setValidity(lnsi.POLL(clientId, key, Long.valueOf(file_repository.get(key).getVersion())));
			if(!fff.isValid()) {
				file_repository.update(fff);
			}
		} catch (FileUnFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (FileDuplicationException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (VersionMismatchException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
       }
     };
     final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 1, TTR, TimeUnit.SECONDS);
       scheduler.schedule(new Runnable() {
       public void run() {  
       		try {
				if(!file_repository.get(key).isValid()) {
					beeperHandle.cancel(true);
				}
			} catch (FileUnFoundException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
			}
       }
     }, 300, TimeUnit.MILLISECONDS);
   }
}