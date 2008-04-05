package org.fbreader.formats;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.description.BookDescription.WritableBookDescription;
import org.fbreader.formats.fb2.FB2Plugin;
import org.fbreader.formats.html.HtmlPlugin;
import org.fbreader.formats.oeb.OEBPlugin;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.language.ZLLanguageDetector;
import org.zlibrary.core.options.*;

public abstract class FormatPlugin {

	protected FormatPlugin() {}
		
	public abstract boolean providesMetaInfo();
	
	public abstract boolean acceptsFile(ZLFile file);
	
	public abstract String getIconName();
	

	public String tryOpen(String path) {
		final String EMPTY = "";
		return EMPTY;
	}
	
	public abstract	boolean readDescription(String path, BookDescription description);
	
	public abstract boolean readModel(BookDescription description, BookModel model);
	
	public FormatInfoPage createInfoPage(ZLOptionsDialog d, String str) { return null; }

	public static void detectEncodingAndLanguage(BookDescription description, InputStream stream) throws IOException {	
		String language = description.getLanguage();
		String encoding = description.getEncoding();
		if (encoding.length() == 0 || language.length() == 0) {
			PluginCollection collection = PluginCollection.instance();
			if (language.length() == 0) {
				language = collection.DefaultLanguageOption.getValue();
			}
			if (encoding.length() == 0) {
				encoding = collection.DefaultEncodingOption.getValue();
			}
			if (collection.LanguageAutoDetectOption.getValue() && stream != null) {
				int BUFSIZE = 65536;
				byte[] buffer = new byte[BUFSIZE];
				int size = stream.read(buffer, 0, BUFSIZE);
				stream.close();
				ZLLanguageDetector.LanguageInfo info =
					new ZLLanguageDetector().findInfo(buffer, 0, size);
				buffer = null;
				if (info != null) {
					language = info.Language;
					encoding = info.Encoding;
					if ((encoding == "US-ASCII") || (encoding == "ISO-8859-1")) {
						encoding = "windows-1252";
					}
				}
			}
			new WritableBookDescription(description).setEncoding(encoding);
			new WritableBookDescription(description).setLanguage(language);
		}
	}
	//Last working version
	/*public static void detectEncodingAndLanguage(BookDescription description, InputStream stream) {	
		String encoding = description.getEncoding();
		if (encoding.length() == 0) {
			encoding = EncodingDetector.detect(stream, PluginCollection.instance().DefaultLanguageOption.getValue());
			if (encoding == "unknown") {
				encoding = "windows-1252";
			}
			new WritableBookDescription(description).setEncoding(encoding);
		}

		if (description.getLanguage() == "") {
			if ((encoding.equals("US-ASCII")) ||
					(encoding.equals("ISO-8859-1"))) {
				new WritableBookDescription(description).setLanguage("en");
			} else if ((description.getEncoding().equals("KOI8-R")) ||
					(encoding.equals("windows-1251")) ||
					(encoding.equals("ISO-8859-5")) ||
					(encoding.equals("IBM866"))) {
				new WritableBookDescription(description).setLanguage("ru");
			} /*else if (
	                (PluginCollection.instance().DefaultLanguageOption.getValue() == EncodingDetector.Language.CZECH) &&
					((encoding == "windows-1250") ||
					 (encoding == "ISO-8859-2") ||
					 (encoding == "IBM852"))) {
				new WritableBookDescription(description).setLanguage("cs");
			}*/
		/*}

	}*/
	
	
	static class FormatInfoPage {
		protected FormatInfoPage() {}
	};

	public static class PluginCollection {

		private static PluginCollection ourInstance;
		private final ArrayList myPlugins = new ArrayList();
		public ZLStringOption DefaultLanguageOption;
		public ZLStringOption DefaultEncodingOption;
		public ZLBooleanOption LanguageAutoDetectOption;
		
		public static PluginCollection instance() {
			if (ourInstance == null) {
				ourInstance = new PluginCollection();
				ourInstance.myPlugins.add(new FB2Plugin());
				//ourInstance->myPlugins.push_back(new DocBookPlugin());
				ourInstance.myPlugins.add(new HtmlPlugin());
				/*ourInstance.myPlugins.add(new TxtPlugin());
				ourInstance.myPlugins.add(new PluckerPlugin());
				ourInstance.myPlugins.add(new PalmDocPlugin());
				ourInstance.myPlugins.add(new MobipocketPlugin());
				ourInstance.myPlugins.add(new ZTXTPlugin());
				ourInstance.myPlugins.add(new TcrPlugin());
				ourInstance.myPlugins.add(new CHMPlugin());
				*/
				ourInstance.myPlugins.add(new OEBPlugin());
				//ourInstance.myPlugins.add(new RtfPlugin());
				//ourInstance.myPlugins.add(new OpenReaderPlugin());
			}
			return ourInstance;
		}
		
		public static void deleteInstance() {
			if (ourInstance != null) {
				ourInstance = null;
			}
		}

		private PluginCollection() {
			LanguageAutoDetectOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "Format", "AutoDetect", true);
			DefaultLanguageOption = new ZLStringOption(ZLOption.CONFIG_CATEGORY, "Format", "DefaultLanguage", "en"); 
			DefaultEncodingOption = new ZLStringOption(ZLOption.CONFIG_CATEGORY, "Format", "DefaultEncoding", "windows-1252");
		}
			
		public FormatPlugin getPlugin(ZLFile file, boolean strong) {
			final ArrayList plugins = myPlugins;
			final int numberOfPlugins = plugins.size();
			for (int i = 0; i < numberOfPlugins; ++i) {
				FormatPlugin fp = (FormatPlugin)plugins.get(i);
				if ((!strong || fp.providesMetaInfo()) && fp.acceptsFile(file)) {
					return fp;
				}
			}
			return null;
		}
		
	};

}
