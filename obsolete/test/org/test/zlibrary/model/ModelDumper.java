package org.test.zlibrary.model;

import org.geometerplus.zlibrary.text.model.*;

public class ModelDumper {
	public static String dump(ZLTextModel model) {
		StringBuilder sb = new StringBuilder();
		final int len = model.getParagraphsNumber();
		for (int i = 0; i < len; ++i) {
			ZLTextParagraph paragraph = model.getParagraph(i);
			sb.append("[PARAGRAPH]\n");
			for (ZLTextParagraph.EntryIterator it = paragraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						sb.append("[TEXT]");
						sb.append(it.getTextData(), it.getTextOffset(), it.getTextLength());
						sb.append("[/TEXT]");
						break;
					case ZLTextParagraph.Entry.CONTROL:
						if (it.getControlIsStart())
							sb.append("[CONTROL "+it.getControlKind()+"]");
						else
							sb.append("[/CONTROL "+it.getControlKind()+"]");					
						break;
				}
			}
			sb.append("[/PARAGRAPH]\n");
		}
		return sb.toString();
	}
}
