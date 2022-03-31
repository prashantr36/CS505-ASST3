package project3.pa3;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import project3.pa3.FileRepository.FileDuplicationException;
import project3.pa3.FileRepository.FileRepositoryFile;
import project3.pa3.FileRepository.FileUnFoundException;
import project3.pa3.FileRepository.VersionMismatchException;
public class PullWatcherDaemon {
   public static Integer TTR;
   private final ScheduledExecutorService scheduler;
   private FileRepository file_repository;
   private String key;
   private LeafNodeServerInterface lnsi;
   private String clientId;
   public static volatile HashSet<String> keys_being_monitored = new HashSet<String>();
   public PullWatcherDaemon(Integer TTR,
		   					FileRepository file_repository,
		   					String key, 
		   					LeafNodeServerInterface lnsi,
		   					String clientId) {
	   PullWatcherDaemon.TTR = TTR;
	   this.scheduler = Executors.newScheduledThreadPool(1);
	   this.file_repository = file_repository;
	   this.key = key;
	   this.lnsi = lnsi;
	   this.clientId = clientId;
   }
   /*--------- start change ----------*/
   public void beepForAnHour() {
	 PullWatcherDaemon.keys_being_monitored.add(key);
     final Runnable beeper = new Runnable() {
       public void run() {
    	   FileRepositoryFile fff;
		try {
			fff = file_repository.get(key);
			System.out.println(" POLLING RESULTS " + lnsi.POLL(clientId, key, Long.valueOf(file_repository.get(key).getVersion())));
			fff.setValidity(lnsi.POLL(clientId, key, Long.valueOf(file_repository.get(key).getVersion())));
			if(!fff.isValid()) {
				System.out.println(" UPDATING validity in cache " + clientId + " " + key);
				file_repository.isStaleThenInvalidate(fff.getFileName(), FileRepository.FORCE_FILE_STALE_VERSION_NUMBER);
			}
		} catch (FileUnFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
       }
     };
     final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 1, TTR, TimeUnit.MILLISECONDS);
     Timer timer = new Timer();
     timer.schedule(new TimerTask() {
         @Override
         public void run() {  
        		try {
        		if(!file_repository.get(key).isValid()) {
 					PullWatcherDaemon.keys_being_monitored.remove(key);
 					beeperHandle.cancel(true);
 				}
 			} catch (FileUnFoundException e) {
 				// TODO Auto-generated catch block
 				System.err.println(e.getMessage());
 			}
        }
     }, 0, 200);
   }
   /*--------- end change ----------*/
}