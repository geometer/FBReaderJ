package org.geometerplus.fbreader.network.litres.readers;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.litres.LitresBookItem;
import org.geometerplus.fbreader.network.litres.LitresUtil;
import org.geometerplus.fbreader.network.urlInfo.BookBuyUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresBookEntry extends LitresEntry {
	public String SeriesTitle;
	public float SeriesIndex;
	private UrlInfoCollection<UrlInfo> myUrls = new UrlInfoCollection<UrlInfo>();
	public String myBookId;
	public String myGenre;
	public LinkedList<LitresBookItem.AuthorData> myAuthors = new LinkedList<LitresBookItem.AuthorData>();
	LitresBookEntry(ZLStringMap source){
		super(source);
		processAttr(source);
	}
	
	private void processAttr(ZLStringMap attributes){
		myBookId = attributes.getValue("hub_id");
		myUrls.addInfo(new UrlInfo(
			UrlInfo.Type.Image, attributes.getValue("cover_preview"), MimeType.IMAGE_AUTO
		));
		
		String price = attributes.getValue("price");
		myUrls.addInfo(new BookBuyUrlInfo(UrlInfo.Type.BookBuy, 
				BookUrlInfo.Format.FB2_ZIP, 
				LitresUtil.generatePurchaseUrl(myBookId),
				MimeType.APP_FB2_ZIP,
				new Money(price, "RUB")));

		myUrls.addInfo(new BookUrlInfo(
			UrlInfo.Type.BookConditional,
			BookUrlInfo.Format.FB2_ZIP,
			LitresUtil.generateDownloadUrl(myBookId),
			MimeType.APP_FB2_ZIP
		));
		
		int hasTrial = Integer.parseInt(attributes.getValue("has_trial"));
		if(hasTrial > 0){
			myUrls.addInfo(new BookUrlInfo(
				UrlInfo.Type.BookDemo,
				BookUrlInfo.Format.FB2_ZIP,
				LitresUtil.generateTrialUrl(myBookId),
				MimeType.APP_FB2_ZIP
			));
		}
	}
	
	public void addUrls(UrlInfoCollection<UrlInfo> urls){
		List<UrlInfo> newUrls = urls.getAllInfos();
		for(UrlInfo info : newUrls){
			myUrls.addInfo(info);
		}
	}
	
	public UrlInfoCollection<UrlInfo> getUrls(){
		return myUrls;
	}
}
