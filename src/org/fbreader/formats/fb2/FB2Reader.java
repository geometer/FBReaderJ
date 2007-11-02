package org.fbreader.formats.fb2;

import java.io.IOException;

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
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(myFileName, new FB2Handler(myBookModel));
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
