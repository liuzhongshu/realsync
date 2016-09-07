package com.cloudtopo.tools;

import java.io.File;

public class SyncDir {
	public String srcDir;
	public String tgtDir;
	public boolean enable;
	public String excludePattern;
	public boolean syncDelete;
	public boolean syncCreate;
	public boolean syncModify;
	
	public SyncDir(SyncDir old, String child) {
		this.srcDir = new File(old.srcDir, child).toString();
		this.tgtDir = new File(old.tgtDir, child).toString();
		this.enable = old.enable;
		this.excludePattern = old.excludePattern;
		this.syncCreate = old.syncCreate;
		this.syncDelete = old.syncDelete;
		this.syncModify = old.syncModify;
	}
	
	public SyncDir() 
	{
		this.syncDelete = true;
		this.syncCreate = true;
		this.syncModify = true;
	}
}
