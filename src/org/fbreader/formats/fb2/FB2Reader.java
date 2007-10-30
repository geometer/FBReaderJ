package org.fbreader.formats.fb2;

import java.io.IOException;

import javax.xml.parsers.*;

import org.xml.sax.SAXException;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

public class FB2Reader {
	private String myFileName;
	private ZLTextModel myModel = (new ZLModelFactory()).createModel();
	
	public FB2Reader(String fileName) {
		myFileName = fileName;
	}
	
	public ZLTextModel read() {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(myFileName, new FB2Handler(myModel));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myModel;
	}

}
