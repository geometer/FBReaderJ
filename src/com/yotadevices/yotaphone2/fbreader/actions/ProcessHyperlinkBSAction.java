package com.yotadevices.yotaphone2.fbreader.actions;

import com.yotadevices.yotaphone2.fbreader.FBBSAction;
import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;

public class ProcessHyperlinkBSAction extends FBBSAction {
	public ProcessHyperlinkBSAction(FBReaderYotaService bsActivity, FBReaderApp fbreader) {
		super(bsActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		final ZLTextRegion region = Reader.getTextView().getSelectedRegion();
		if (region == null) {
			return;
		}

		final ZLTextRegion.Soul soul = region.getSoul();
		if (soul instanceof ZLTextHyperlinkRegionSoul) {
			Reader.getTextView().hideSelectedRegionBorder();
			Reader.getViewWidget().repaint();
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul) soul).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.INTERNAL:
					Reader.Collection.markHyperlinkAsVisited(Reader.getCurrentBook(), hyperlink.Id);
					Reader.tryOpenFootnote(hyperlink.Id);
					break;
			}
		}
	}
}
