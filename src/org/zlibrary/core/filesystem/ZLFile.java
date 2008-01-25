package org.zlibrary.core.filesystem;

import java.io.InputStream;
import java.util.Map;

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
	
	public static String fileNameToUtf8(String fileName) {
		return ZLFSManager.instance().convertFilenameToUtf8(fileName);
	}
	
	public ZLFile(String path) {
		myPath = path;
		myInfoIsFilled = false;
		ZLFSManager.instance().normalize(myPath);
		{
			int index = ZLFSManager.instance().findLastFileNameDelimiter(myPath);
			if (index < myPath.length() - 1) {
				myNameWithExtension = myPath.substring(index + 1);
			} else {
				myNameWithExtension = myPath;
			}
		}
		myNameWithoutExtension = myNameWithExtension;

		Map forcedFiles = ZLFSManager.instance().getForcedFiles();
		Integer value = (Integer)forcedFiles.get(myPath);
		if (value != null) {
			myArchiveType = value;
		} else {
			myArchiveType = ArchiveType.NONE;
			String lowerCaseName = "";// = ZLUnicodeUtil.toLower(myNameWithoutExtension);

			if (lowerCaseName.endsWith(".gz")) {
				myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 3);
				lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 3);
				myArchiveType = (int)(myArchiveType | ArchiveType.GZIP);
			}
			if (lowerCaseName.endsWith(".bz2")) {
				myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 4);
				lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 4);
				myArchiveType = (int)(myArchiveType | ArchiveType.BZIP2);
			}
			if (lowerCaseName.endsWith(".zip")) {
				myArchiveType = (int)(myArchiveType | ArchiveType.ZIP);
			} else if (lowerCaseName.endsWith(".tar")) {
				myArchiveType = (int)(myArchiveType | ArchiveType.TAR);
			} else if (lowerCaseName.endsWith(".tgz") || lowerCaseName.endsWith(".ipk")) {
				//nothing to-do myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 3) + "tar";
				myArchiveType = (int)(myArchiveType | ArchiveType.TAR | ArchiveType.GZIP);
			}
		}
//(arg0)rfind
		int index = myNameWithoutExtension.lastIndexOf('.');
		if (index > 0) {
			myExtension = myNameWithoutExtension.substring(index + 1);
			myNameWithoutExtension = myNameWithoutExtension.substring(0, index);
		}
	}
	
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
	
	public void forceArchiveType(int type) {
		if (myArchiveType != type) {
			myArchiveType = type;
			ZLFSManager.instance().putForcedFiles(myPath, myArchiveType);
		}
	}
	
	public boolean isCompressed() {
		 return (0 != (myArchiveType & ArchiveType.COMPRESSED)); 
	}
	
	public boolean isDirectory() {
		if (!myInfoIsFilled) fillInfo(); 
		return myInfo.IsDirectory;
	}
	
	public boolean isArchive() {
		return (0 != (myArchiveType & ArchiveType.ARCHIVE));
	}

	public boolean remove() {
		if (ZLFSManager.instance().removeFile(myPath)) {
			myInfoIsFilled = false;
			return true;
		} else {
			return false;
		}
	}

	public String path() {
		return myPath;
	}
	
	public String name(boolean hideExtension) {
		return hideExtension ? myNameWithoutExtension : myNameWithExtension;
	}
	
	public String extension() {
		return myExtension;
	}

	public String physicalFilePath() {
		String path = myPath;
		int index;
		while ((index = ZLFSManager.instance().findArchiveFileNameDelimiter(path)) != -1) {
			path = path.substring(0, index);
		}
		return path;
	}

	public InputStream inputStream() {
		if (isDirectory()) {
			return null;
		}

		InputStream stream = null;
		
		int index = ZLFSManager.instance().findArchiveFileNameDelimiter(myPath);
		if (index == -1) {
			stream = ZLFSManager.instance().createPlainInputStream(myPath);
		} else {
			ZLFile baseFile = new ZLFile(myPath.substring(0, index));
			InputStream base = baseFile.inputStream();
			if (base != null) {
				if ( 0 != (baseFile.myArchiveType & ArchiveType.ZIP)) {
					//stream = new ZLZipInputStream(base, myPath.substring(index + 1));
				} else if (0 != (baseFile.myArchiveType & ArchiveType.TAR)) {
					//stream = new ZLTarInputStream(base, myPath.substring(index + 1));
				}
			}
		}

		if (stream != null) {
			if (0 != (myArchiveType & ArchiveType.GZIP)) {
				//return new ZLGzipInputStream(stream);
			}
			if (0 != (myArchiveType & ArchiveType.BZIP2)) {
				//return new ZLBzip2InputStream(stream);
			}
		}
		return stream;
	}
	//public ZLOutputStream outputStream();*/
	
	//always createUnexisting = false;
	public ZLDir directory(boolean createUnexisting) {
		
		/*if (exists()) {
			if (isDirectory()) {
				return ZLFSManager.instance().createPlainDirectory(myPath);
			} else if (0 != (myArchiveType & ArchiveType.ZIP)) {
				return new ZLZipDir(myPath);
			} else if (0 != (myArchiveType & ArchiveType.TAR)) {
				return new ZLTarDir(myPath);
			}
		} else if (createUnexisting) {
			myInfoIsFilled = false;
			return ZLFSManager.instance().createNewDirectory(myPath);
		}*/
		return null;
	}

}


