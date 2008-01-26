package org.fbreader.formats.fb2;

import org.fbreader.description.BookDescription;
import org.fbreader.description.BookDescription.WritableBookDescription;

public class FB2DescriptionReader {
	private WritableBookDescription myDescription;
	private	boolean myReturnCode;
	private	boolean myReadSomething;
	private	boolean myReadTitle;
	private	boolean myReadAuthor;
	private	boolean[] myReadAuthorName = new boolean[3];
	private	boolean myReadLanguage;
	private	String[] myAuthorNames = new String[3];

	public FB2DescriptionReader(BookDescription description) {
		myDescription = new WritableBookDescription(description);
		myDescription.clearAuthor();
		myDescription.setTitle("");
		myDescription.setLanguage("");
		
	}
	
	public boolean readDescription(String fileName) {
		myReadSomething = false;
		myReadTitle = false;
		myReadAuthor = false;
		myReadLanguage = false;
		for (int i = 0; i < 3; ++i) {
			myReadAuthorName[i] = false;
		}
		//return readDocument(fileName);
		//TODO!!
		return true;
	}

	public void startElementHandler(int tag, char attributes) {
		switch (tag) {
		case FB2Tag.BODY:
			myReturnCode = true;
			//interrupt();
			break;
		case FB2Tag.TITLE_INFO:
			myReadSomething = true;
			break;
		case FB2Tag.BOOK_TITLE:
			myReadTitle = true;
			break;
		case FB2Tag.AUTHOR:
			myReadAuthor = true;
			break;
		case FB2Tag.LANG:
			myReadLanguage = true;
			break;
		case FB2Tag.FIRST_NAME:
			if (myReadAuthor) {
				myReadAuthorName[0] = true;
			}
			break;
		case FB2Tag.MIDDLE_NAME:
			if (myReadAuthor) {
				myReadAuthorName[1] = true;
			}
			break;
		case FB2Tag.LAST_NAME:
			if (myReadAuthor) {
				myReadAuthorName[2] = true;
			}
			break;
		case FB2Tag.SEQUENCE:
			if (myReadSomething) {
				/*String name = attributeValue(attributes, "name");
				if (name != null) {
					String sequenceName = name;
					sequenceName.trim();
					myDescription.setSequenceName(sequenceName);
					String number = attributeValue(attributes, "number");
					myDescription.setNumberInSequence((number != null) ? Integer.parseInt(number) : 0);
				}*/
			}
			break;
	   default : 
			break;
	   }
	}
	
	public void endElementHandler(int tag) {
		switch (tag) {
		case FB2Tag.TITLE_INFO:
			myReadSomething = false;
			break;
		case FB2Tag.BOOK_TITLE:
			myReadTitle = false;
			break;
		case FB2Tag.AUTHOR:
			if (myReadSomething) {
				myAuthorNames[0].trim();
				myAuthorNames[1].trim();
				myAuthorNames[2].trim();
				String fullName = myAuthorNames[0];
				if (fullName.length() != 0 && myAuthorNames[1].length() != 0) {
					fullName += ' ';
				}
				fullName += myAuthorNames[1];
				if (fullName.length() != 0 && myAuthorNames[2].length() != 0) {
					fullName += ' ';
				}
				fullName += myAuthorNames[2];
				myDescription.addAuthor(fullName, myAuthorNames[2]);
				myAuthorNames[0] = "";
				myAuthorNames[1] = "";
				myAuthorNames[2] = "";
				myReadAuthor = false;
			}
			break;
		case FB2Tag.LANG:
			myReadLanguage = false;
			break;
		case FB2Tag.FIRST_NAME:
			myReadAuthorName[0] = false;
			break;
		case FB2Tag.MIDDLE_NAME:
			myReadAuthorName[1] = false;
			break;
		case FB2Tag.LAST_NAME:
			myReadAuthorName[2] = false;
			break;
		default:
			break;
	    }	
	}
	
	public void characterDataHandler(String text, int len) {
		if (myReadSomething) {
			if (myReadTitle) {
				//myDescription.getTitle().append(text, len);
			} else if (myReadLanguage) {
				//myDescription.getLanguage().append(text, len);
			} else {
				for (int i = 0; i < 3; ++i) {
					if (myReadAuthorName[i]) {
						//myAuthorNames[i].append(text, len);
						break;
					}
				}
			}
		}
	}

	//------------------------------------------------
	
	private boolean myInterrupted;
	//private	ZLXMLReaderInternal myInternalReader;
	private	String myParserBuffer;

	
	public static int bufferSize() { 
		return 2048; 
	}
	
	public boolean isInterrupted() {
		return myInterrupted;
	}

	public void interrupt() {
		myInterrupted = true;
	}

	/*public String attributeValue(String[] xmlattributes, String name) {
		while (xmlattributes != null) {
			boolean useNext = strcmp(*xmlattributes, name) == 0;
			++xmlattributes;
			if (*xmlattributes == 0) {
				return 0;
			}
			if (useNext) {
				return *xmlattributes;
			}
			++xmlattributes;
		}
		return null;
	}

	String attributeValue(const char **xmlattributes, const std::string &name) {
		while (*xmlattributes != 0) {
			bool useNext = name == *xmlattributes;
			++xmlattributes;
			if (*xmlattributes == 0) {
				return 0;
			}
			if (useNext) {
				return *xmlattributes;
			}
			++xmlattributes;
		}
		return 0;
	}
*/

}
