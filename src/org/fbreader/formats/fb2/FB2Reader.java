package org.fbreader.formats.fb2;

import java.io.*;

import javax.xml.parsers.*;

import org.fbreader.bookmodel.BookModel;
import org.xml.sax.SAXException;

public class FB2Reader {
	private String myFileName;
	private BookModel myBookModel = new BookModel();
	
	public FB2Reader(String fileName) {
		myFileName = fileName;
	}
	
	public BookModel read() {
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(myFileName);
			if (stream == null) {
				stream = new BufferedInputStream(new FileInputStream(myFileName));
			}
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, new FB2Handler(myBookModel));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myBookModel;
	}

}
