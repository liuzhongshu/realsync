package com.cloudtopo.tools;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
 
/**
 * Example to watch a directory (or tree) for changes to files.
 */
 
public class Watcher {
 
    private final WatchService watcherService;
    private final Map<WatchKey,SyncDir> key2Sync;
    private final JTextArea textArea;
 
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
 
    /**
     * Register the given directory with the WatchService
     */
    private void registerSingle(Path dir, SyncDir syncDir) {
		try {
	        key2Sync.put(dir.register(watcherService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), syncDir);
		} catch (IOException e) {
		}     
    }
 
    public void stop() {
    	for (WatchKey key :key2Sync.keySet())
    	{
    		key.cancel();
    	}
    	key2Sync.clear();
    }
    
    public void register(final SyncDir syncDir) {
    	File root = new File(syncDir.srcDir);
        if (!root.isDirectory())
        	return;
        
        registerSingle(root.toPath(), syncDir);
        File[] list = root.listFiles();
        if (list == null) return;
        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	register( new SyncDir(syncDir, f.getName()));
            }
        }
    }
 
    /**
     * Creates a WatchService and registers the given directory
     */
    Watcher(JTextArea textArea) throws IOException {
        this.watcherService = FileSystems.getDefault().newWatchService();
        this.key2Sync = new ConcurrentHashMap<WatchKey, SyncDir>();
        this.textArea = textArea;
        new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						processEvents();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
    }
 
    /**
     * Process all events for keys queued to the watcher
     * @throws InterruptedException 
     */
    void processEvents() throws Exception {
         	WatchKey key;
            key = watcherService.take();
 
            SyncDir sync = key2Sync.get(key);
            if (sync == null) {
                return;
            }
 
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
 
                if (kind == OVERFLOW) {
                    continue;
                }
 
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path srcDir = Paths.get(sync.srcDir);
                Path tgtDir = Paths.get(sync.tgtDir);
                File srcFile = new File(srcDir.resolve(name).toUri());
                File tgtFile = new File(tgtDir.resolve(name).toUri());
                //System.out.println(kind.toString() + ":" + srcFile.getAbsolutePath());
                
                if (kind == ENTRY_CREATE && sync.syncCreate) {
                	textArea.append("create " + tgtFile.getAbsolutePath() + "\r\n"); 
                    if (srcFile.isDirectory()) {
                    	tgtFile.mkdirs();
                        registerSingle(srcFile.toPath(), new SyncDir(sync, name.toString()));
                    }else {
                    	tgtFile.getParentFile().mkdirs();
                    	FileUtils.copyFile(srcFile, tgtFile);                    
                    }                    
                }
                else if (kind == ENTRY_MODIFY && sync.syncModify) {
                	if (srcFile.isFile()) {
                		textArea.append("modify " + tgtFile.getAbsolutePath() + "\r\n"); 
                		FileUtils.copyFile(srcFile,tgtFile);
                	}
                }
                else if (kind == ENTRY_DELETE && sync.syncDelete) {
                	textArea.append("delete " + tgtFile.getAbsolutePath() + "\r\n"); 
                	if (tgtFile.isFile())
                		tgtFile.delete();
                	else
                		FileUtils.deleteDirectory(tgtFile);
                }
            }
 
            boolean valid = key.reset();
            if (!valid) {
                key2Sync.remove(key);
            }
    }
}