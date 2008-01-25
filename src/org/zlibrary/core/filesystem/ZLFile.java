package org.zlibrary.core.filesystem;

public class ZLFile {
	public class ArchiveType {
		public static final int	NONE = 0;
		public static final int	GZIP = 0x0001;
		public static final int	BZIP2 = 0x0002;
		public static final int	COMPRESSED = 0x00ff;
		public static final int	ZIP = 0x0100;
		public static final int	TAR = 0x0200;
		public static final int	ARCHIVE = 0xff00;
	};
	
	private String myPath;
	private String myNameWithExtension;
	private String myNameWithoutExtension;
	private String myExtension;
	private	int myArchiveType;
	private	ZLFileInfo myInfo;
	private	boolean myInfoIsFilled;

	private void fillInfo() {
		int index = ZLFSManager.instance().findArchiveFileNameDelimiter(myPath);
		if (index == -1) {
			myInfo = ZLFSManager.instance().fileInfo(myPath);
		} else {
			myInfo = ZLFSManager.instance().fileInfo(myPath.substring(0, index));
			myInfo.IsDirectory = false;
		}
		myInfoIsFilled = true;
	}
	
	//public static String fileNameToUtf8(String fileName) {}
	
	public ZLFile(String path) {}
	
	public boolean exists() {
		if (!myInfoIsFilled) 
			fillInfo(); 
		return myInfo.Exists;
	}
	
	public long mTime() {
		if (!myInfoIsFilled) 
			fillInfo(); 
		return myInfo.MTime; 
	}
	
	public long size() {
		if (!myInfoIsFilled) 
			fillInfo(); 
		return myInfo.Size;
	}	
	
	//public void forceArchiveType(ArchiveType type);
	
	public boolean isCompressed() {
		 return !(0 == (myArchiveType & ArchiveType.COMPRESSED)); 
	}
	
	public boolean isDirectory() {
		if (!myInfoIsFilled) fillInfo(); 
		return myInfo.IsDirectory;
	}
	
	public boolean isArchive() {
		return !(0 == (myArchiveType & ArchiveType.ARCHIVE));
	}

	//public boolean remove();

	public String path() {
		return myPath;
	}
	
	public String name(boolean hideExtension) {
		return hideExtension ? myNameWithoutExtension : myNameWithExtension;
	}
	
	public String extension() {
		return myExtension;
	}

	/*public String physicalFilePath();

	public ZLInputStream inputStream();
	public ZLOutputStream outputStream();*/
	public ZLDir directory(boolean createUnexisting) {
		createUnexisting = false;
		/*if (exists()) {
			if (isDirectory()) {
				return ZLFSManager.instance().createPlainDirectory(myPath);
			} else if (!(0 == (myArchiveType & ArchiveType.ZIP))) {
				return new ZLZipDir(myPath);
			} else if (!(0 == (myArchiveType & ArchiveType.TAR))) {
				return new ZLTarDir(myPath);
			}
		} else if (createUnexisting) {
			myInfoIsFilled = false;
			return ZLFSManager.instance().createNewDirectory(myPath);
		}*/
		return null;
	}

}


