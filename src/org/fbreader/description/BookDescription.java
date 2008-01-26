package org.fbreader.description;

import java.util.*;
import org.zlibrary.core.util.*;

import org.fbreader.description.Author.MultiAuthor;
import org.fbreader.description.Author.SingleAuthor;
import org.fbreader.option.FBOptions;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLStringOption;

public class BookDescription {
	private Author myAuthor;
	private	String myTitle;
	private	String mySequenceName;
	private	int myNumberInSequence;
	private	final String myFileName;
	private	String myLanguage;
	private	String myEncoding;
	private final static HashMap ourDescriptions = new HashMap();
	
	private static final String EMPTY = "";
	private static final String UNKNOWN = "unknown";
	
//boolean checkFile = true
	public static BookDescription getDescription(String fileName, boolean checkFile) {
		String physicalFileName = new ZLFile(fileName).physicalFilePath();
		ZLFile file = new ZLFile(physicalFileName);
		if (checkFile && !file.exists()) {
			return null;
		}

		BookDescription description = (BookDescription)ourDescriptions.get(fileName);
		if (description == null) {
			description = new BookDescription(fileName);
			ourDescriptions.put(fileName, description);
		}

		if (!checkFile || BookDescriptionUtil.checkInfo(file)) {
			BookInfo info = new BookInfo(fileName);
			description.myAuthor = SingleAuthor.create(info.AuthorDisplayNameOption.getValue(), info.AuthorSortKeyOption.getValue());
			description.myTitle = info.TitleOption.getValue();
			description.mySequenceName = info.SequenceNameOption.getValue();
			description.myNumberInSequence = info.NumberInSequenceOption.getValue();
			description.myLanguage = info.LanguageOption.getValue();
			description.myEncoding = info.EncodingOption.getValue();
			if (info.isFull()) {
				return description;
			}
		} else {
			if (physicalFileName != fileName) {
				BookDescriptionUtil.resetZipInfo(file);
			}
			BookDescriptionUtil.saveInfo(file);
		}

		ZLFile bookFile = new ZLFile(fileName);
		//FormatPlugin plugin = PluginCollection.instance().plugin(bookFile, false);
		//if ((plugin == 0) || !plugin.readDescription(fileName, description)) {
		//	return null;
		//}

		if (description.myTitle.length() == 0) {
			description.myTitle = ZLFile.fileNameToUtf8(bookFile.name(true));
		}
		Author author = description.myAuthor;
		if (author == null || author.getDisplayName().length() == 0) {
			description.myAuthor = SingleAuthor.create();
		}
		if (description.myEncoding.length() == 0) {
			description.myEncoding = "auto";
		}
		{
			BookInfo info = new BookInfo(fileName);
			info.AuthorDisplayNameOption.setValue(description.myAuthor.getDisplayName());
			info.AuthorSortKeyOption.setValue(description.myAuthor.getSortKey());
			info.TitleOption.setValue(description.myTitle);
			info.SequenceNameOption.setValue(description.mySequenceName);
			info.NumberInSequenceOption.setValue(description.myNumberInSequence);
			info.LanguageOption.setValue(description.myLanguage);
			info.EncodingOption.setValue(description.myEncoding);
			info.IsSequenceDefinedOption.setValue(true);
		}
		return description;
	}


	private BookDescription(String fileName) {
		myFileName = fileName;
		myAuthor = null;
		myNumberInSequence = 0;
	}

	public Author getAuthor() {
		return myAuthor;
	}
	
	public String getTitle() {
		return myTitle;
	}
	
	public String getSequenceName() {
		return mySequenceName;
	}
	
	public int getNumberInSequence() {
		return myNumberInSequence; 
	}
	
	public String getFileName() {
		return myFileName; 
	}
	
	public String getLanguage() {
		return myLanguage;
	}
	
	public String getEncoding() {
		return myEncoding;
	}
	
	
	
	public static class BookInfo {
		// This option is used to fix problem with missing sequence-related options
		// in config in versions < 0.7.4k
		// It makes no sense if old fbreader was never used on your device.
		private final ZLBooleanOption IsSequenceDefinedOption;

		public BookInfo(String fileName) {
			AuthorDisplayNameOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "AuthorDisplayName", EMPTY);
			AuthorSortKeyOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "AuthorSortKey", EMPTY);
			TitleOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Title", EMPTY);
			SequenceNameOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Sequence", EMPTY);
			NumberInSequenceOption = new ZLIntegerRangeOption(FBOptions.BOOKS_CATEGORY, fileName, "Number in seq", 0, 100, 0);
			LanguageOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Language", UNKNOWN);
			EncodingOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Encoding", EMPTY);
			IsSequenceDefinedOption = new ZLBooleanOption(FBOptions.BOOKS_CATEGORY, fileName, "SequenceDefined", new ZLFile(fileName).extension().equals("fb2")); 
			
		}
	
		public boolean isFull() {
			return
			((AuthorDisplayNameOption.getValue().length() != 0) &&
			(AuthorSortKeyOption.getValue().length() != 0) &&
			(TitleOption.getValue().length() != 0) &&
			(EncodingOption.getValue().length() != 0) &&
			IsSequenceDefinedOption.getValue());
		}
		
		void reset() {
			AuthorDisplayNameOption.setValue(EMPTY);
			AuthorSortKeyOption.setValue(EMPTY);
			TitleOption.setValue(EMPTY);
			SequenceNameOption.setValue(EMPTY);
			NumberInSequenceOption.setValue(0);
			LanguageOption.setValue(UNKNOWN);
			EncodingOption.setValue(EMPTY);
		}

		private final ZLStringOption AuthorDisplayNameOption;
		private final ZLStringOption AuthorSortKeyOption;
		private final ZLStringOption TitleOption;
		private final ZLStringOption SequenceNameOption;
		private final ZLIntegerRangeOption NumberInSequenceOption;
		private final ZLStringOption LanguageOption;
		public final ZLStringOption EncodingOption;

	}
	
	static public class WritableBookDescription  {
		private final BookDescription myDescription;
		
		public WritableBookDescription(BookDescription description) {
			//super(description.getFileName());
			myDescription = description;
		}
		
		public void addAuthor(String name) {
			addAuthor(name, "");
		}
		
		public void addAuthor(String name, String sortKey) {
			String strippedName = name;
			strippedName.trim();
			if (strippedName.length() == 0) {
				return;
			}

			String strippedKey = sortKey;
			strippedKey.trim();
			if (strippedKey.length() == 0) {
				int index = strippedName.indexOf(' ');
				if (index == -1) {
					strippedKey = strippedName;
				} else {
					strippedKey = strippedName.substring(index + 1);
					while ((index >= 0) && (strippedName.charAt(index) == ' ')) {
						--index;
					}
					strippedName = strippedName.substring(0, index + 1) + ' ' + strippedKey;
				}
			}
			Author author = SingleAuthor.create(strippedName, strippedKey);
			
			if (myDescription.myAuthor == null) {
				myDescription.myAuthor = author;
			} else {
				if (myDescription.myAuthor.isSingle()) {
					myDescription.myAuthor = MultiAuthor.create(myDescription.myAuthor);
				}
				((MultiAuthor)myDescription.myAuthor).addAuthor(author);
			}
		}
		
		public void clearAuthor() {
			myDescription.myAuthor = null;
		}
		
		public Author getAuthor() {
			return myDescription.getAuthor();
		}
		
		public String getTitle() {
			return myDescription.myTitle;
		}
		
		public void setTitle(String title) {
			myDescription.myTitle = title;
		}
		
		public String getSequenceName() {
			return myDescription.mySequenceName;
		}
		
		public int getNumberInSequence() {
			return myDescription.myNumberInSequence;
		}
		
		public String getFileName() {
			return myDescription.myFileName; 
		}
		
		public String getLanguage() {
			return myDescription.myLanguage;
		}
		
		public void setLanguage(String language) {
			this.myDescription.myLanguage = language;
		}
		
		public String getEncoding() {
			return myDescription.myEncoding;
		}
		
		public void setEncoding(String encoding) {
			this.myDescription.myEncoding = encoding;
		}
	};
}
