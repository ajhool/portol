package com.portol.cloudplayer.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Runnable is used to constantly attempt to take from the watch 
 * queue, and will receive all events that are registered with the 
 * fileWatcher it is associated. In this sample for simplicity we 
 * just output the kind of event and name of the file affected to 
 * standard out.
 */
class MyWatchQueueReader implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(MyWatchQueueReader.class);

	/** the watchService that is passed in from above */
	private WatchService myWatcher;
	private MPDUpdaterService parentService;
	private String repId; 

	public MyWatchQueueReader(WatchService myWatcher, MPDUpdaterService mpdUpdaterService, String repId) {
		super();
		this.myWatcher = myWatcher;
		this.parentService = mpdUpdaterService;
		this.repId = repId;
	}

	/**
	 * In order to implement a file watcher, we loop forever 
	 * ensuring requesting to take the next item from the file 
	 * watchers queue.
	 */
	@Override
	public void run() {
		try {
			// get the first event before looping
			WatchKey key = myWatcher.take();
			while(key != null) {
				// we have a polled event, now we traverse it and 
				// receive all the states from it
				for (WatchEvent event : key.pollEvents()) {
					logger.info("Received" + event.kind() +  "event for file:" +  event.context());
					final Path changed = (Path) event.context();
					System.out.println(changed);

					if (changed.endsWith("live.mpd")) {
						System.out.println("updating, live.mpd has changed");
						new Thread(new Runnable() {
							public void run()
							{
								logger.info("starting MPD file update method...");
								parentService.fileEvent(repId);
								logger.info("...MPD file updated");
							}
						}).start();


					} else if(event.kind() == ENTRY_CREATE){

					}


				}
				key.reset();
				key = myWatcher.take();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping thread");
	}



}
