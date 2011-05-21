package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class SelectionButtonPanel extends SeveralButtonsPanel {
    SelectionButtonPanel(FBReaderApp fbReader) {
        super(fbReader);
    }

    @Override
    protected void onAddButtons() {
        addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.selection_copy);
        addButton(ActionCode.SELECTION_SHARE, false, R.drawable.selection_share);
        addButton(ActionCode.SELECTION_OPEN_IN_DICTIONARY, false, R.drawable.selection_dictionary);
        addButton(ActionCode.SELECTION_ADD_BOOKMARK, true, R.drawable.selection_bookmark);
    }
    
    public void move(int selectionStartY, int selectionEndY) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        int verticalPosition; 
        int screenHeight = ((View)myControlPanel.getParent()).getHeight();
        if (screenHeight - selectionEndY > myControlPanel.getHeight() + 10)
            verticalPosition = RelativeLayout.ALIGN_PARENT_BOTTOM;
        else if (selectionStartY > myControlPanel.getHeight() + 10)
            verticalPosition = RelativeLayout.ALIGN_PARENT_TOP;
        else
            verticalPosition = RelativeLayout.CENTER_VERTICAL; 

        layoutParams.addRule(verticalPosition);
        myControlPanel.setLayoutParams(layoutParams);
    }
}
