/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser;

import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.io.IFileFilter;
import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
import group.pals.android.lib.ui.filechooser.services.FileProviderService;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.FilterMode;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortOrder;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortType;
import group.pals.android.lib.ui.filechooser.services.LocalFileProvider;
import group.pals.android.lib.ui.filechooser.utils.ActivityCompat;
import group.pals.android.lib.ui.filechooser.utils.E;
import group.pals.android.lib.ui.filechooser.utils.FileComparator;
import group.pals.android.lib.ui.filechooser.utils.FileUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.Utils;
import group.pals.android.lib.ui.filechooser.utils.history.History;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryFilter;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryListener;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryStore;
import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
import group.pals.android.lib.ui.filechooser.utils.ui.TaskListener;
import group.pals.android.lib.ui.filechooser.utils.ui.ViewFilesContextMenuUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Main activity for this library.<br>
 * <br>
 * <b>Notes:</b><br>
 * <br>
 * <b>I.</b> About keys {@link FileChooserActivity#_Rootpath},
 * {@link FileChooserActivity#_SelectFile} and preference
 * {@link DisplayPrefs#isRememberLastLocation(Context)}, the priorities of them
 * are:<br>
 * <li>1. {@link FileChooserActivity#_SelectFile}</li>
 * 
 * <li>2. {@link DisplayPrefs#isRememberLastLocation(Context)}</li>
 * 
 * <li>3. {@link FileChooserActivity#_Rootpath}</li>
 * 
 * @author Hai Bison
 * 
 */
public class FileChooserActivity extends Activity {

    /**
     * The full name of this class. Generally used for debugging.
     */
    public static final String _ClassName = FileChooserActivity.class.getName();

    /**
     * Types of view.
     * 
     * @author Hai Bison
     * @since v4.0 beta
     */
    public static enum ViewType {
        /**
         * Use {@link ListView} to display file list.
         */
        List,
        /**
         * Use {@link GridView} to display file list.
         */
        Grid
    }

    /*---------------------------------------------
     * KEYS
     */

    /**
     * Sets value of this key to a theme in {@code android.R.style.Theme_*}.<br>
     * Default is:<br>
     * 
     * <li>{@link android.R.style#Theme_DeviceDefault} for {@code SDK >= }
     * {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}</li>
     * 
     * <li>{@link android.R.style#Theme_Holo} for {@code SDK >= }
     * {@link Build.VERSION_CODES#HONEYCOMB}</li>
     * 
     * <li>{@link android.R.style#Theme} for older systems</li>
     * 
     * @since v4.3 beta
     */
    public static final String _Theme = _ClassName + ".theme";

    /**
     * Key to hold the root path.<br>
     * <br>
     * If {@link LocalFileProvider} is used, then default is sdcard, if sdcard
     * is not available, "/" will be used.<br>
     * <br>
     * <b>Note</b>: The value of this key is a {@link IFile}
     */
    public static final String _Rootpath = _ClassName + ".rootpath";

    /**
     * Key to hold the service class which implements {@link IFileProvider}.<br>
     * Default is {@link LocalFileProvider}
     */
    public static final String _FileProviderClass = _ClassName + ".file_provider_class";

    // ---------------------------------------------------------

    /**
     * Key to hold {@link IFileProvider.FilterMode}, default is
     * {@link IFileProvider.FilterMode#FilesOnly}.
     */
    public static final String _FilterMode = IFileProvider.FilterMode.class.getName();

    // flags

    // ---------------------------------------------------------

    /**
     * Key to hold max file count that's allowed to be listed, default =
     * {@code 1024}
     */
    public static final String _MaxFileCount = _ClassName + ".max_file_count";
    /**
     * Key to hold multi-selection mode, default = {@code false}
     */
    public static final String _MultiSelection = _ClassName + ".multi_selection";
    /**
     * Key to hold regex filename filter, default = {@code null}
     */
    public static final String _RegexFilenameFilter = _ClassName + ".regex_filename_filter";
    /**
     * Key to hold display-hidden-files, default = {@code false}
     */
    public static final String _DisplayHiddenFiles = _ClassName + ".display_hidden_files";
    /**
     * Sets this to {@code true} to enable double tapping to choose files/
     * directories. In older versions, double tapping is default. However, since
     * v4.7 beta, single tapping is default. So if you want to keep the old way,
     * please set this key to {@code true}.
     * 
     * @since v4.7 beta
     */
    public static final String _DoubleTapToChooseFiles = _ClassName + ".double_tap_to_choose_files";
    /**
     * Sets the file you want to select when starting this activity. The file is
     * an {@link IFile}.<br>
     * <b>Notes:</b><br>
     * <li>Currently this key is only used for single selection mode.</li>
     * 
     * <li>If you use save dialog mode, this key will override key
     * {@link #_DefaultFilename}.</li>
     * 
     * @since v4.7 beta
     */
    public static final String _SelectFile = _ClassName + ".select_file";

    public static final String _TextResources = _ClassName + ".text_resources";
    public static final String _ShowNewFolderButton = _ClassName + ".show_new_folder_button";
    public static final String _FilenameRegExp = _ClassName + ".file_regexp";
    // ---------------------------------------------------------

    /**
     * Key to hold property save-dialog, default = {@code false}
     */
    public static final String _SaveDialog = _ClassName + ".save_dialog";
    public static final String _ActionBar = _ClassName + ".action_bar";
    /**
     * Key to hold default filename, default = {@code null}
     */
    public static final String _DefaultFilename = _ClassName + ".default_filename";
    /**
     * Key to hold results (can be one or multiple files)
     */
    public static final String _Results = _ClassName + ".results";
    public static final String _FileSelectionMode = _ClassName + ".file_selection_mode";
    public static final String _FolderPath = _ClassName + ".folder_path";
    public static final String _SaveLastLocation = _ClassName + ".save_last_location";

    /**
     * This key holds current location (an {@link IFile}), to restore it after
     * screen orientation changed
     */
    static final String _CurrentLocation = _ClassName + ".current_location";
    /**
     * This key holds current history (a {@link History}&lt;{@link IFile}&gt;),
     * to restore it after screen orientation changed
     */
    static final String _History = _ClassName + ".history";

    /**
     * This key holds current full history (a {@link History}&lt; {@link IFile}
     * &gt;), to restore it after screen orientation changed.
     */
    static final String _FullHistory = History.class.getName() + "_full";

    // ====================
    // "CONSTANT" VARIABLES

    private Class<?> mFileProviderServiceClass;
    /**
     * The file provider service.
     */
    private IFileProvider mFileProvider;
    /**
     * The service connection.
     */
    private ServiceConnection mServiceConnection;

    private IFile mRoot;
    private boolean mIsMultiSelection;
    private boolean mIsSaveDialog;
    private boolean mIsActionBar;
    private boolean mIsSaveLastLocation;
    private boolean mDoubleTapToChooseFiles;
    private Toast mToast = null;

    /**
     * The history.
     */
    private History<IFile> mHistory;

    /**
     * The full history, to store and show the users whatever they have been
     * gone to.
     */
    private History<IFile> mFullHistory;

    /**
     * The adapter of list view.
     */
    private IFileAdapter mFileAdapter;

    /*
     * controls
     */
    private HorizontalScrollView mViewLocationsContainer;
    private ViewGroup mViewLocations;
    private ViewGroup mViewFilesContainer;
    private TextView mTxtFullDirName;
    private AbsListView mViewFiles;
    private TextView mFooterView;
    private Button mBtnSave;
    private Button mBtnOk;
    private Button mBtnCancel;
    private EditText mTxtSaveas;
    private ImageView mViewGoBack;
    private ImageView mViewGoForward;
    private ImageView mViewCreateFolder;
    private ImageView mViewFoldersView;
    private ImageView mViewSort;

    private HashMap<String, String> mTextResources; 
    private String mFilenameRegexp; 

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*
         * THEME
         */

        /*
        if (getIntent().hasExtra(_Theme)) {
            int theme;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_DeviceDefault);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_Holo);
            else
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme);
            setTheme(theme);
        }
        */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.afc_file_chooser);

        initGestureDetector();

        // load configurations

        mFileProviderServiceClass = (Class<?>) getIntent().getSerializableExtra(_FileProviderClass);
        if (mFileProviderServiceClass == null)
            mFileProviderServiceClass = LocalFileProvider.class;

        mIsMultiSelection = getIntent().getBooleanExtra(_MultiSelection, false);

        mIsActionBar = getIntent().getBooleanExtra(_ActionBar, false);
        mIsSaveDialog = getIntent().getBooleanExtra(_SaveDialog, false);
        if (mIsSaveDialog)
            mIsMultiSelection = false;

        mIsSaveLastLocation = getIntent().getBooleanExtra(_SaveLastLocation, true);
        if(!mIsSaveLastLocation)
            DisplayPrefs.setRememberLastLocation(this, false);
        mDoubleTapToChooseFiles = getIntent().getBooleanExtra(_DoubleTapToChooseFiles, false);

        mTextResources = (HashMap<String, String>)getIntent().getSerializableExtra(_TextResources);
        
        // load controls
        mViewSort = (ImageView) findViewById(R.id.afc_filechooser_activity_button_sort);
        mViewFoldersView = (ImageView) findViewById(R.id.afc_filechooser_activity_button_folders_view);
        mViewCreateFolder = (ImageView) findViewById(R.id.afc_filechooser_activity_button_create_folder);
        if (!getIntent().getBooleanExtra(_ShowNewFolderButton, true)) {
            mViewCreateFolder.setVisibility(View.GONE);
        }
        mFilenameRegexp = getIntent().getStringExtra(_FilenameRegExp);
        mViewGoBack = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_back);
        mViewGoForward = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_forward);
        mViewLocations = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_locations);
        mViewLocationsContainer = (HorizontalScrollView) findViewById(R.id.afc_filechooser_activity_view_locations_container);
        mTxtFullDirName = (TextView) findViewById(R.id.afc_filechooser_activity_textview_full_dir_name);
        mViewFilesContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_files_container);
        mFooterView = (TextView) findViewById(R.id.afc_filechooser_activity_view_files_footer_view);
        mTxtSaveas = (EditText) findViewById(R.id.afc_filechooser_activity_textview_saveas_filename);
        mBtnSave = (Button) findViewById(R.id.afc_filechooser_activity_button_save);
        mBtnOk = (Button) findViewById(R.id.afc_filechooser_activity_button_ok);
        mBtnCancel = (Button) findViewById(R.id.afc_filechooser_activity_button_cancel);

        // history
        if (savedInstanceState != null && savedInstanceState.get(_History) instanceof HistoryStore<?>)
            mHistory = savedInstanceState.getParcelable(_History);
        else
            mHistory = new HistoryStore<IFile>(DisplayPrefs._DefHistoryCapacity);
        mHistory.addListener(new HistoryListener<IFile>() {

            @Override
            public void onChanged(History<IFile> history) {
                int idx = history.indexOf(getLocation());
                mViewGoBack.setEnabled(idx > 0);
                mViewGoForward.setEnabled(idx >= 0 && idx < history.size() - 1);
            }
        });

        // full history
        if (savedInstanceState != null && savedInstanceState.get(_FullHistory) instanceof HistoryStore<?>)
            mFullHistory = savedInstanceState.getParcelable(_FullHistory);
        else
            mFullHistory = new HistoryStore<IFile>(DisplayPrefs._DefHistoryCapacity) {

                @Override
                public void push(IFile newItem) {
                    int i = indexOf(newItem);
                    if (i >= 0) {
                        if (i == size() - 1)
                            return;
                        else
                            remove(newItem);
                    }
                    super.push(newItem);
                }// push()
            };

        // make sure RESULT_CANCELED is default
        setResult(RESULT_CANCELED);

        bindService(savedInstanceState);
    }// onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.afc_file_chooser_activity, menu);
        MenuItem item = menu.findItem(R.id.afc_filechooser_activity_menuitem_home);
        if(item != null)
            item.setTitle(mTextResources.get("menuOrigin"));
        item = menu.findItem(R.id.afc_filechooser_activity_menuitem_reload);
        if(item != null)
            item.setTitle(mTextResources.get("menuReload"));
        return true;
    }// onCreateOptionsMenu()

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }// onPrepareOptionsMenu()

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_home) {
            doGoHome();
        } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_reload) {
            doReloadCurrentLocation();
        }

        return true;
    }// onOptionsItemSelected()

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }// onConfigurationChanged()

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(_CurrentLocation, getLocation());
        outState.putParcelable(_History, mHistory);
        outState.putParcelable(_FullHistory, mFullHistory);
    }// onSaveInstanceState()

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsMultiSelection && !mIsSaveDialog && mDoubleTapToChooseFiles)
            Dlg.toast(this, R.string.afc_hint_double_tap_to_select_file, Dlg._LengthShort);
    }// onStart()

    @Override
    public void onBackPressed() {
        IFile currentLoc = getLocation();
        if (currentLoc == null || mHistory == null) {
            super.onBackPressed();
            return;
        }

        IFile preLoc = null;
        while (currentLoc.equalsToPath(preLoc = mHistory.prevOf(currentLoc)))
            mHistory.remove(preLoc);
        if (preLoc != null) {
            goTo(preLoc);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        if (mFileProvider != null) {
            try {
                unbindService(mServiceConnection);
            } catch (Throwable t) {
                /*
                 * due to this error:
                 * https://groups.google.com/d/topic/android-developers
                 * /Gv-80mQnyhc/discussion
                 */
                Log.e(_ClassName, "onDestroy() - unbindService() - exception: " + t);
            }

            try {
                stopService(new Intent(this, mFileProviderServiceClass));
            } catch (SecurityException e) {
                /*
                 * we have permission to stop our own service, so this exception
                 * should never be thrown
                 */
            }
        }

        super.onDestroy();
    }

    /**
     * Connects to file provider service, then loads root directory. If can not,
     * then finishes this activity with result code =
     * {@link Activity#RESULT_CANCELED}
     * 
     * @param savedInstanceState
     */
    private void bindService(final Bundle savedInstanceState) {
        if (startService(new Intent(this, mFileProviderServiceClass)) == null) {
            doShowCannotConnectToServiceAndFinish();
            return;
        }

        mServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                try {
                    mFileProvider = ((FileProviderService.LocalBinder) service).getService();
                } catch (Throwable t) {
                    Log.e(_ClassName, "mServiceConnection.onServiceConnected() -> " + t);
                }
            }// onServiceConnected()

            public void onServiceDisconnected(ComponentName className) {
                mFileProvider = null;
            }// onServiceDisconnected()
        };

        bindService(new Intent(this, mFileProviderServiceClass), mServiceConnection, Context.BIND_AUTO_CREATE);

        new LoadingDialog(this, R.string.afc_msg_loading, false) {

            private static final int _WaitTime = 200;
            private static final int _MaxWaitTime = 3000; // 3 seconds

            @Override
            protected Object doInBackground(Void... params) {
                int totalWaitTime = 0;
                while (mFileProvider == null) {
                    try {
                        totalWaitTime += _WaitTime;
                        Thread.sleep(_WaitTime);
                        if (totalWaitTime >= _MaxWaitTime)
                            break;
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (mFileProvider == null) {
                    doShowCannotConnectToServiceAndFinish();
                } else {
                    setupService();
                    setupHeader();
                    setupViewFiles();
                    setupFooter();

                    /*
                     * Priorities for starting path:
                     * 
                     * 1. Current location (in case the activity has been killed
                     * after configurations changed).
                     * 
                     * 2. Selected file from key _SelectFile.
                     * 
                     * 3. Last location.
                     * 
                     * 4. Root path from key _Rootpath.
                     */

                    // current location
                    IFile path = savedInstanceState != null ? (IFile) savedInstanceState.get(_CurrentLocation) : null;
                    
                    // selected file
                    IFile selectedFile = null;
                    if (path == null) {
                        selectedFile = (IFile) getIntent().getParcelableExtra(_SelectFile);
                        if (selectedFile != null && selectedFile.exists())
                            path = selectedFile.parentFile();
                        if (path == null)
                            selectedFile = null;
                    }

                    // last location
                    if (path == null && DisplayPrefs.isRememberLastLocation(FileChooserActivity.this)) {
                        String lastLocation = DisplayPrefs.getLastLocation(FileChooserActivity.this);
                        if (lastLocation != null)
                            path = mFileProvider.fromPath(lastLocation);
                    }

                    final IFile _selectedFile = selectedFile;

                    // or root path
                    setLocation(path != null && path.isDirectory() ? path : mRoot, new TaskListener() {

                        @Override
                        public void onFinish(boolean ok, Object any) {
                            if (ok && _selectedFile != null && _selectedFile.isFile() && mIsSaveDialog)
                                mTxtSaveas.setText(_selectedFile.getName());

                            // don't push current location into history
                            boolean isCurrentLocation = savedInstanceState != null
                                    && any.equals(savedInstanceState.get(_CurrentLocation));
                            if (isCurrentLocation) {
                                mHistory.notifyHistoryChanged();
                            } else {
                                mHistory.push((IFile) any);
                                mFullHistory.push((IFile) any);
                            }
                        }// onFinish()
                    }, selectedFile);
                }
            }// onPostExecute()
        }.execute();// LoadingDialog
    }// bindService()

    /**
     * Setup the file provider:<br>
     * - filter mode;<br>
     * - display hidden files;<br>
     * - max file count;<br>
     * - ...
     */
    private void setupService() {
        /*
         * set root path, if not specified, try using
         * IFileProvider#defaultPath()
         */
        if (getIntent().getParcelableExtra(_Rootpath) != null)
            mRoot = (IFile) getIntent().getSerializableExtra(_Rootpath);
        if (mRoot == null || !mRoot.isDirectory())
            mRoot = mFileProvider.defaultPath();

        IFileProvider.FilterMode filterMode = (FilterMode) getIntent().getSerializableExtra(_FilterMode);
        if (filterMode == null) {
            filterMode = IFileProvider.FilterMode.DirectoriesOnly;
        }

        IFileProvider.SortType sortType = DisplayPrefs.getSortType(this);
        boolean sortAscending = DisplayPrefs.isSortAscending(this);

        mFileProvider.setDisplayHiddenFiles(getIntent().getBooleanExtra(_DisplayHiddenFiles, false));
        mFileProvider.setFilterMode(mIsSaveDialog ? IFileProvider.FilterMode.FilesOnly : filterMode);
        mFileProvider.setMaxFileCount(getIntent().getIntExtra(_MaxFileCount, 1024));
        mFileProvider.setRegexFilenameFilter(getIntent().getStringExtra(_RegexFilenameFilter));
        mFileProvider.setSortOrder(sortAscending ? IFileProvider.SortOrder.Ascending
                : IFileProvider.SortOrder.Descending);
        mFileProvider.setSortType(sortType);
    }// setupService()

    /**
     * Setup:<br>
     * - title of activity;<br>
     * - button go back;<br>
     * - button location;<br>
     * - button go forward;
     */
    private void setupHeader() {
        setTitle(mTextResources.get("title"));

        mViewSort.setOnClickListener(mBtnSortOnClickListener);
        if(DisplayPrefs.isSortAscending(this)){
            mViewSort.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_sort_as));
            mViewSort.setId(R.drawable.afc_selector_button_sort_as);
        }else{
            mViewSort.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_sort_de));
            mViewSort.setId(R.drawable.afc_selector_button_sort_de);
        }

        mViewFoldersView.setOnClickListener(mBtnFoldersViewOnClickListener);
        switch (DisplayPrefs.getViewType(this)) {
        case List:
            mViewFoldersView.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_folders_view_grid));
            mViewFoldersView.setId(R.drawable.afc_selector_button_folders_view_grid);
            break;
        case Grid:
            mViewFoldersView.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_folders_view_list));
            mViewFoldersView.setId(R.drawable.afc_selector_button_folders_view_list);
            break;
        }
        mViewCreateFolder.setOnClickListener(mBtnCreateFolderOnClickListener);
        
        mViewGoBack.setEnabled(false);
        mViewGoBack.setOnClickListener(mBtnGoBackOnClickListener);

        mViewGoForward.setEnabled(false);
        mViewGoForward.setOnClickListener(mBtnGoForwardOnClickListener);

        for (ImageView v : new ImageView[] { mViewGoBack, mViewGoForward })
            v.setOnLongClickListener(mBtnGoBackForwardOnLongClickListener);
    }// setupHeader()

    /**
     * Setup:<br>
     * - {@link #mViewFiles}<br>
     * - {@link #mViewFilesContainer}<br>
     * - {@link #mFileAdapter}
     */
    private void setupViewFiles() {
        switch (DisplayPrefs.getViewType(this)) {
        case Grid:
            mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_gridview_files, null);
            break;
        case List:
            mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_listview_files, null);
            break;
        }

        mViewFilesContainer.removeAllViews();
        mViewFilesContainer.addView(mViewFiles, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 1));

        mViewFiles.setOnItemClickListener(mViewFilesOnItemClickListener);
        mViewFiles.setOnItemLongClickListener(mViewFilesOnItemLongClickListener);
        mViewFiles.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mListviewFilesGestureDetector.onTouchEvent(event);
            }
        });

        createIFileAdapter();

        // no comments :-D
        mFooterView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                E.show(FileChooserActivity.this);
                return false;
            }
        });
    }// setupViewFiles()

    /**
     * Creates {@link IFileAdapter} and assign it to list view/ grid view of
     * files.
     */
    private void createIFileAdapter() {
        if (mFileAdapter != null)
            mFileAdapter.clear();

        mFileAdapter = new IFileAdapter(FileChooserActivity.this, new ArrayList<IFileDataModel>(),
                mFileProvider.getFilterMode(), mFilenameRegexp, mIsMultiSelection);
        /*
         * API 13+ does not recognize AbsListView.setAdapter(), so we cast it to
         * explicit class
         */
        if (mViewFiles instanceof ListView)
            ((ListView) mViewFiles).setAdapter(mFileAdapter);
        else
            ((GridView) mViewFiles).setAdapter(mFileAdapter);
    }// createIFileAdapter()

    /**
     * Setup:<br>
     * - button Cancel;<br>
     * - text field "save as" filename;<br>
     * - button Ok;
     */
    private void setupFooter() {
        // by default, view group footer and all its child views are hidden

        ViewGroup viewGroupFooterContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer_container);
        ViewGroup viewGroupFooter = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer2);
        ViewGroup viewGroupFooterBottom = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer_bottom);
        
        if (mIsSaveDialog) {
            viewGroupFooterContainer.setVisibility(View.VISIBLE);
            viewGroupFooter.setVisibility(View.VISIBLE);

            mTxtSaveas.setVisibility(View.VISIBLE);
            mTxtSaveas.setText(getIntent().getStringExtra(_DefaultFilename));
            mTxtSaveas.setOnEditorActionListener(mTxtFilenameOnEditorActionListener);

            mBtnSave.setVisibility(View.VISIBLE);
            mBtnSave.setOnClickListener(mBtnSave_SaveDialog_OnClickListener);
            mBtnSave.setBackgroundResource(R.drawable.afc_selector_button_ok_saveas);
            
            int size = getResources().getDimensionPixelSize(R.dimen.afc_button_ok_saveas_size);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mBtnSave.getLayoutParams();
            lp.width = size;
            lp.height = size;
            mBtnSave.setLayoutParams(lp);
        }// this is in save mode
        if(mIsActionBar){
            viewGroupFooterContainer.setVisibility(View.VISIBLE);
            viewGroupFooterBottom.setVisibility(View.VISIBLE);
            if (mFileProvider.getFilterMode() != IFileProvider.FilterMode.FilesOnly) {
                mBtnOk.setVisibility(View.VISIBLE);
                mBtnOk.setOnClickListener(mBtnOk_ActionBar_OnClickListener);
            } else {
                mBtnOk.setVisibility(View.GONE);
            }
            mBtnCancel.setVisibility(View.VISIBLE);
            mBtnCancel.setOnClickListener(mBtnCancel_ActionBar_OnClickListener);
            mBtnOk.setText(mTextResources.get("ok"));
            mBtnCancel.setText(mTextResources.get("cancel"));
        }    
        
        if (mIsMultiSelection) {
            viewGroupFooterContainer.setVisibility(View.VISIBLE);
            viewGroupFooter.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams lp = viewGroupFooter.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewGroupFooter.setLayoutParams(lp);

            mBtnSave.setMinWidth(getResources().getDimensionPixelSize(R.dimen.afc_single_button_min_width));
            mBtnSave.setText(android.R.string.ok);
            mBtnSave.setVisibility(View.VISIBLE);
            mBtnSave.setOnClickListener(mBtnSave_OpenDialog_OnClickListener);
        }
    }// setupFooter()

    private void doReloadCurrentLocation() {
        setLocation(getLocation(), null);
    }// doReloadCurrentLocation()

    private void doShowCannotConnectToServiceAndFinish() {
        Dlg.showError(FileChooserActivity.this, R.string.afc_msg_cannot_connect_to_file_provider_service,
                new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }// doShowCannotConnectToServiceAndFinish()

    private void doGoHome() {
        // TODO explain why?
        goTo(mRoot.clone());
    }// doGoHome()

    private static final int[] _BtnSortIds = { R.id.afc_settings_sort_view_button_sort_by_name_asc,
            R.id.afc_settings_sort_view_button_sort_by_name_desc, R.id.afc_settings_sort_view_button_sort_by_size_asc,
            R.id.afc_settings_sort_view_button_sort_by_size_desc, R.id.afc_settings_sort_view_button_sort_by_date_asc,
            R.id.afc_settings_sort_view_button_sort_by_date_desc };

    /**
     * Show a dialog for sorting options and resort file list after user
     * selected an option.
     */
    private void doResortViewFiles() {
        final AlertDialog _dialog = Dlg.newDlg(this);

        // get the index of button of current sort type
        int btnCurrentSortTypeIdx = 0;
        switch (DisplayPrefs.getSortType(this)) {
        case SortByName:
            btnCurrentSortTypeIdx = 0;
            break;
        case SortBySize:
            btnCurrentSortTypeIdx = 2;
            break;
        case SortByDate:
            btnCurrentSortTypeIdx = 4;
            break;
        }
        if (!DisplayPrefs.isSortAscending(this))
            btnCurrentSortTypeIdx++;

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                _dialog.dismiss();

                Context c = FileChooserActivity.this;

                if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortByName);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortByName);
                    DisplayPrefs.setSortAscending(c, false);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortBySize);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortBySize);
                    DisplayPrefs.setSortAscending(c, false);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortByDate);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortByDate);
                    DisplayPrefs.setSortAscending(c, false);
                }
                
                resortViewFiles();
                if(DisplayPrefs.isSortAscending(c)){
                    mViewSort.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_sort_as));
                    mViewSort.setId(R.drawable.afc_selector_button_sort_as);
                }else{
                    mViewSort.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_sort_de));
                    mViewSort.setId(R.drawable.afc_selector_button_sort_de);
                }
            }// onClick()
        };// listener

        View view = getLayoutInflater().inflate(R.layout.afc_settings_sort_view, null);
        TextView sortTitle = (TextView) view.findViewById(R.id.afc_settings_sort_view_textview_sort_by_name);
        sortTitle.setText(mTextResources.get("sortByName"));
        sortTitle = (TextView) view.findViewById(R.id.afc_settings_sort_view_textview_sort_by_size);
        sortTitle.setText(mTextResources.get("sortBySize"));
        sortTitle = (TextView) view.findViewById(R.id.afc_settings_sort_view_textview_sort_by_date);
        sortTitle.setText(mTextResources.get("sortByDate"));
        
        for (int i = 0; i < _BtnSortIds.length; i++) {
            Button btn = (Button) view.findViewById(_BtnSortIds[i]);
            btn.setOnClickListener(listener);
            if (i == btnCurrentSortTypeIdx) {
                btn.setEnabled(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    btn.setText(R.string.afc_ellipsize);
            }
        }

        //_dialog.setTitle(R.string.afc_title_sort_by);
        _dialog.setTitle(mTextResources.get("sortBy"));
        _dialog.setView(view);

        _dialog.show();
    }// doResortViewFiles()

    /**
     * Resort view files.
     */
    private void resortViewFiles() {
        if (mFileProvider.getSortType().equals(DisplayPrefs.getSortType(this))
                && mFileProvider.getSortOrder().isAsc() == (DisplayPrefs.isSortAscending(this)))
            return;

        /*
         * Re-sort the listview by re-loading current location; NOTE: re-sort
         * the adapter does not repaint the listview, even if we call
         * notifyDataSetChanged(), invalidateViews()...
         */
        mFileProvider.setSortType(DisplayPrefs.getSortType(this));
        mFileProvider.setSortOrder(DisplayPrefs.isSortAscending(this) ? SortOrder.Ascending : SortOrder.Descending);
        doReloadCurrentLocation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            ActivityCompat.invalidateOptionsMenu(this);
    }// resortViewFiles()

    /**
     * Switch view type between {@link ViewType#List} and {@link ViewType#Grid}
     */
    private void doSwitchViewType() {
        new LoadingDialog(this, R.string.afc_msg_loading, false) {

            @Override
            protected void onPreExecute() {
                // call this first, to let the parent prepare the dialog
                super.onPreExecute();

                switch (DisplayPrefs.getViewType(FileChooserActivity.this)) {
                case Grid:
                    DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.List);
                    break;
                case List:
                    DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.Grid);
                    break;
                }

                setupViewFiles();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    ActivityCompat.invalidateOptionsMenu(FileChooserActivity.this);

                doReloadCurrentLocation();
            }// onPreExecute()

            @Override
            protected Object doInBackground(Void... params) {
                // do nothing :-)
                return null;
            }// doInBackground()
        }.execute();
    }// doSwitchViewType()

    /**
     * Confirms user to create new directory.
     */
    private void doCreateNewDir() {
        if (mFileProvider instanceof LocalFileProvider
                && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_create_files, Dlg._LengthShort);
            return;
        }
        
        if ((getLocation() instanceof File)){
            if(!((File)getLocation()).canWrite()){
                Dlg.toast(this, R.string.afc_msg_app_cant_create_folder, Dlg._LengthShort);
                return;
            }
        }

        final AlertDialog _dlg = Dlg.newDlg(this);

        View view = getLayoutInflater().inflate(R.layout.afc_simple_text_input_view, null);
        final EditText _textFile = (EditText) view.findViewById(R.id.afc_simple_text_input_view_text1);
        _textFile.setHint(mTextResources.get("folderNameHint"));
        _textFile.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Ui.hideSoftKeyboard(FileChooserActivity.this, _textFile.getWindowToken());
                    _dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        _dlg.setView(view);
        _dlg.setTitle(mTextResources.get("newFolder"));
        _dlg.setIcon(android.R.drawable.ic_menu_add);
        _dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = _textFile.getText().toString().trim();
                        if (!FileUtils.isFilenameValid(name)) {
                            Dlg.toast(FileChooserActivity.this, getString(R.string.afc_pmsg_filename_is_invalid, name),
                                    Dlg._LengthShort);
                            return;
                        }

                        final IFileProvider fileProvider = mFileProvider;
                        final IFile location = getLocation();
                        if (fileProvider == null || location == null) {
                            return;
                        }

                        IFile dir = fileProvider.fromPath(String
                                .format("%s/%s", location.getAbsolutePath(), name));
                        if (dir.mkdir()) {
                            Dlg.toast(FileChooserActivity.this, getString(R.string.afc_msg_done), Dlg._LengthShort);
                            setLocation(getLocation(), null);
                        } else
                            Dlg.toast(FileChooserActivity.this,
                                    getString(R.string.afc_pmsg_cannot_create_folder, name), Dlg._LengthShort);
                    }// onClick()
                });
        _dlg.show();

        final Button _btnOk = _dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        _btnOk.setEnabled(false);

        _textFile.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                _btnOk.setEnabled(FileUtils.isFilenameValid(s.toString().trim()));
            }
        });
    }// doCreateNewDir()

    /**
     * Updates UI that {@code data} will not be deleted.
     * 
     * @param data
     *            {@link IFileDataModel}
     */
    private void notifyDataModelNotDeleted(IFileDataModel data) {
        data.setTobeDeleted(false);
        mFileAdapter.notifyDataSetChanged();
    }// notifyDataModelNotDeleted(()

    /**
     * Deletes a file.
     * 
     * @param file
     *            {@link IFile}
     */
    private void doDeleteFile(final IFileDataModel data) {
        if (mFileProvider instanceof LocalFileProvider
                && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            notifyDataModelNotDeleted(data);
            Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_delete_files, Dlg._LengthShort);
            return;
        }

        Dlg.confirmYesno(
                this,
                getString(R.string.afc_pmsg_confirm_delete_file, data.getFile().isFile() ? getString(R.string.afc_file)
                        : getString(R.string.afc_folder), data.getFile().getName()),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new LoadingDialog(FileChooserActivity.this, getString(R.string.afc_pmsg_deleting_file, data
                                .getFile().isFile() ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                data.getFile().getName()), true) {

                            private Thread mThread = FileUtils.createDeleteFileThread(data.getFile(), mFileProvider,
                                    true);
                            private final boolean _isFile = data.getFile().isFile();

                            private void notifyFileDeleted() {
                                mFileAdapter.remove(data);
                                mFileAdapter.notifyDataSetChanged();

                                refreshHistories();
                                // TODO remove all duplicate history items

                                Dlg.toast(
                                        FileChooserActivity.this,
                                        getString(
                                                R.string.afc_pmsg_file_has_been_deleted,
                                                _isFile ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                                data.getFile().getName()), Dlg._LengthShort);
                            }// notifyFileDeleted()

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mThread.start();
                            }// onPreExecute()

                            @Override
                            protected Object doInBackground(Void... arg0) {
                                while (mThread.isAlive()) {
                                    try {
                                        mThread.join(DisplayPrefs._DelayTimeWaitingThreads);
                                    } catch (InterruptedException e) {
                                        mThread.interrupt();
                                    }
                                }
                                return null;
                            }// doInBackground()

                            @Override
                            protected void onCancelled() {
                                mThread.interrupt();

                                if (data.getFile().exists()) {
                                    notifyDataModelNotDeleted(data);
                                    Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
                                } else
                                    notifyFileDeleted();

                                super.onCancelled();
                            }// onCancelled()

                            @Override
                            protected void onPostExecute(Object result) {
                                super.onPostExecute(result);

                                if (data.getFile().exists()) {
                                    notifyDataModelNotDeleted(data);
                                    Dlg.toast(
                                            FileChooserActivity.this,
                                            getString(R.string.afc_pmsg_cannot_delete_file,
                                                    data.getFile().isFile() ? getString(R.string.afc_file)
                                                            : getString(R.string.afc_folder), data.getFile().getName()),
                                            Dlg._LengthShort);
                                } else
                                    notifyFileDeleted();
                            }// onPostExecute()
                        }.execute();// LoadingDialog
                    }// onClick()
                }, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        notifyDataModelNotDeleted(data);
                    }// onCancel()
                });
    }// doDeleteFile()

    /**
     * As the name means.
     * 
     * @param filename
     * @since v1.91
     */
    private void doCheckSaveasFilenameAndFinish(String filename) {
        if (filename.length() == 0) {
            Dlg.toast(this, R.string.afc_msg_filename_is_empty, Dlg._LengthShort);
        } else {
            final IFile _file = mFileProvider.fromPath(getLocation().getAbsolutePath() + File.separator + filename);

            if (!FileUtils.isFilenameValid(filename)) {
                Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_invalid, filename), Dlg._LengthShort);
            } else if (_file.isFile()) {
                Dlg.confirmYesno(FileChooserActivity.this,
                        getString(R.string.afc_pmsg_confirm_replace_file, _file.getName()),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doFinish(_file);
                            }
                        });
            } else if (_file.isDirectory()) {
                Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_directory, _file.getName()), Dlg._LengthShort);
            } else
                doFinish(_file);
        }
    }// doCheckSaveasFilenameAndFinish()

    /**
     * Gets current location.
     * 
     * @return current location, can be {@code null}.
     */
    private IFile getLocation() {
        return (IFile) mViewLocations.getTag();
    }// getLocation()

    /**
     * Sets current location.
     * 
     * @param path
     *            the path
     * @param listener
     *            {@link TaskListener}: the second parameter {@code any} in
     *            {@link TaskListener#onFinish(boolean, Object)} will be
     *            {@code path}.
     */
    private void setLocation(final IFile path, final TaskListener listener) {
        setLocation(path, listener, null);
    }// setLocation()

    /**
     * Sets current location.
     * 
     * @param path
     *            the path
     * @param listener
     *            {@link TaskListener}: the second parameter {@code any} in
     *            {@link TaskListener#onFinish(boolean, Object)} will be
     *            {@code path}.
     * @param selectedFile
     *            the file should be selected after loading location done. Can
     *            be {@code null}.
     */
    private void setLocation(final IFile path, final TaskListener listener, final IFile selectedFile) {
        new LoadingDialog(this, R.string.afc_msg_loading, true) {

            // IFile[] files = new IFile[0];
            List<IFile> files;
            boolean hasMoreFiles = false;
            int shouldBeSelectedIdx = -1;
            /**
             * Used to focus last directory on list view.
             */
            String mLastPath = getLocation() != null ? getLocation().getAbsolutePath() : null;

            @Override
            protected Object doInBackground(Void... params) {
                try {
                    if (path.isDirectory() && path.canRead()) {
                        files = new ArrayList<IFile>();
                        mFileProvider.listAllFiles(path, new IFileFilter() {

                            @Override
                            public boolean accept(IFile pathname) {
                                if (mFileProvider.accept(pathname)) {
                                    if (files.size() < mFileProvider.getMaxFileCount())
                                        files.add(pathname);
                                    else
                                        hasMoreFiles = true;
                                }
                                return false;
                            }// accept()
                        });
                    } else
                        files = null;

                    if (files != null) {
                        Collections.sort(files,
                                new FileComparator(mFileProvider.getSortType(), mFileProvider.getSortOrder()));
                        if (selectedFile != null && selectedFile.exists()
                                && selectedFile.parentFile().equalsToPath(path)) {
                            for (int i = 0; i < files.size(); i++) {
                                if (files.get(i).equalsToPath(selectedFile)) {
                                    shouldBeSelectedIdx = i;
                                    break;
                                }
                            }
                        } else if (mLastPath != null && mLastPath.length() >= path.getAbsolutePath().length()) {
                            for (int i = 0; i < files.size(); i++) {
                                IFile f = files.get(i);
                                if (f.isDirectory() && mLastPath.startsWith(f.getAbsolutePath())) {
                                    shouldBeSelectedIdx = i;
                                    break;
                                }
                            }
                        }
                    }// if files != null
                } catch (Throwable t) {
                    setLastException(t);
                    cancel(false);
                }
                return null;
            }// doInBackground()

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
            }// onCancelled()

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (files == null) {
                    Dlg.toast(FileChooserActivity.this, mTextResources.get("permissionDenied"),
                            Dlg._LengthShort);
                    if (listener != null)
                        listener.onFinish(false, path);
                    return;
                }

                // update list view

                createIFileAdapter();
                for (IFile f : files)
                    mFileAdapter.add(new IFileDataModel(f));
                mFileAdapter.notifyDataSetChanged();

                // update footers

                mFooterView.setVisibility(hasMoreFiles || mFileAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                if (hasMoreFiles)
                    mFooterView.setText(getString(R.string.afc_pmsg_max_file_count_allowed,
                            mFileProvider.getMaxFileCount()));
                else if (mFileAdapter.isEmpty())
                    mFooterView.setText(R.string.afc_msg_empty);

                /*
                 * We use a Runnable to make sure this work. Because if the list
                 * view is handling data, this might not work.
                 */
                mViewFiles.post(new Runnable() {

                    @Override
                    public void run() {
                        if (shouldBeSelectedIdx >= 0 && shouldBeSelectedIdx < mFileAdapter.getCount()) {
                            mViewFiles.setSelection(shouldBeSelectedIdx);
                        } else if (!mFileAdapter.isEmpty())
                            mViewFiles.setSelection(0);
                    }// run()
                });

                /*
                 * navigation buttons
                 */
                createLocationButtons(path);

                /*
                 * update UI elements
                 */
                updateUI(path);

                if (listener != null)
                    listener.onFinish(true, path);
            }// onPostExecute()
        }.execute();// new LoadingDialog()
    }// setLocation()

    /**
     * Goes to a specified location.
     * 
     * @param dir
     *            a directory, of course.
     * @return {@code true} if {@code dir} <b><i>can</i></b> be browsed to.
     * @since v4.3 beta
     */
    private boolean goTo(final IFile dir) {
        if (dir.equalsToPath(getLocation()))
            return false;

        setLocation(dir, new TaskListener() {

            IFile mLastPath = getLocation();

            @Override
            public void onFinish(boolean ok, Object any) {
                if (ok) {
                    mHistory.truncateAfter(mLastPath);
                    mHistory.push(dir);
                    mFullHistory.push(dir);
                }
            }
        });
        return true;
    }// goTo()

    private void createLocationButtons(IFile path) {
        mViewLocations.setTag(path);
        mViewLocations.removeAllViews();

        LinearLayout.LayoutParams lpBtnLoc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpBtnLoc.gravity = Gravity.CENTER;
        LinearLayout.LayoutParams lpDivider = null;
        LayoutInflater inflater = getLayoutInflater();
        int count = 0;
        while (path != null) {
            TextView btnLoc = (TextView) inflater.inflate(R.layout.afc_button_location, null);
            btnLoc.setText(path.parentFile() != null ? "/"+path.getName() : mTextResources.get("root"));
            btnLoc.setTag(path);
            btnLoc.setOnClickListener(mBtnLocationOnClickListener);
            btnLoc.setOnLongClickListener(mBtnLocationOnLongClickListener);
            mViewLocations.addView(btnLoc, 0, lpBtnLoc);

            if (count++ == 0) {
                Rect r = new Rect();
                btnLoc.getPaint().getTextBounds(path.getName(), 0, path.getName().length(), r);
                if (r.width() >= getResources().getDimensionPixelSize(R.dimen.afc_button_location_max_width)
                        - btnLoc.getPaddingLeft() - btnLoc.getPaddingRight()) {
                    mTxtFullDirName.setText(path.getName());
                    mTxtFullDirName.setVisibility(View.VISIBLE);
                } else
                    mTxtFullDirName.setVisibility(View.GONE);
            }

            path = path.parentFile();
        }

        mViewLocationsContainer.postDelayed(new Runnable() {

            public void run() {
                mViewLocationsContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }// createLocationButtons()

    /**
     * Refreshes all the histories. This removes invalid items (which are not
     * existed anymore).
     */
    private void refreshHistories() {
        HistoryFilter<IFile> historyFilter = new HistoryFilter<IFile>() {

            @Override
            public boolean accept(IFile item) {
                return !item.isDirectory();
            }
        };

        mHistory.removeAll(historyFilter);
        mFullHistory.removeAll(historyFilter);
    }// refreshHistories()

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link IFile}
     */
    private void doFinish(IFile ... files) {
        final List<IFile> list = new ArrayList<IFile>();
        for (IFile f : files)
            list.add(f);
        doFinish((ArrayList<IFile>) list);
    }

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link IFile}
     */
    private void doFinish(ArrayList<IFile> files) {
        String returnPath = null;
        // set results
        switch(mFileProvider.getFilterMode()){
            case FilesOnly:
                if (files == null || files.isEmpty()) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
                break;
            case DirectoriesOnly:
            {
                final File file = (File)getLocation();
                if (file != null && file.canWrite()) {
                    returnPath = getLocation().getAbsolutePath();
                }
                break;
            }
            case FilesAndDirectories:
                if(files == null || files.isEmpty()){
                    returnPath = getLocation().getAbsolutePath();
                }
            break;
            default:
                returnPath = getLocation().getAbsolutePath();
                break;
        }
        
		boolean hasData = false;
        Intent intent = new Intent();
        if (returnPath != null) {
            intent.putExtra(_FolderPath, returnPath);
			hasData = true;
		}
        
        if (files != null) {
            intent.putExtra(_Results, files);
			hasData = true;
        } else {
            intent.putExtra(_Results, new ArrayList<IFile>());
        }

		if (!hasData) {
			return;
		}

        // return flags for further use (in case the caller needs)
        intent.putExtra(_FilterMode, mFileProvider.getFilterMode());
        intent.putExtra(_SaveDialog, mIsSaveDialog);

        setResult(RESULT_OK, intent);

        if (DisplayPrefs.isRememberLastLocation(this) && getLocation() != null) {
            DisplayPrefs.setLastLocation(this, getLocation().getAbsolutePath());
        } else
            DisplayPrefs.setLastLocation(this, null);

        finish();
    }// doFinish()

    /**********************************************************
     * BUTTON LISTENERS
     */
    private final View.OnClickListener mBtnSortOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            doResortViewFiles();
        }
    };// mBtnSortOnClickListener


    private final View.OnClickListener mBtnFoldersViewOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            doSwitchViewType();
            if(mViewFoldersView.getId() == R.drawable.afc_selector_button_folders_view_list){
                mViewFoldersView.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_folders_view_grid));
                mViewFoldersView.setId(R.drawable.afc_selector_button_folders_view_grid);
            }else{
                mViewFoldersView.setImageDrawable(getResources().getDrawable(R.drawable.afc_selector_button_folders_view_list));
                mViewFoldersView.setId(R.drawable.afc_selector_button_folders_view_list);
            }
        }
    };// mBtnFoldersViewOnClickListener


    private final View.OnClickListener mBtnCreateFolderOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            doCreateNewDir();   
        }
    };// mBtnCreateFolderOnClickListener


    private final View.OnClickListener mBtnGoBackOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /*
             * if user deleted a dir which was one in history, then maybe there
             * are duplicates, so we check and remove them here
             */
            IFile currentLoc = getLocation();
            IFile preLoc = null;
            while (currentLoc.equalsToPath(preLoc = mHistory.prevOf(currentLoc)))
                mHistory.remove(preLoc);

            if (preLoc != null) {
                setLocation(preLoc, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            mViewGoBack.setEnabled(mHistory.prevOf(getLocation()) != null);
                            mViewGoForward.setEnabled(true);
                            mFullHistory.push((IFile) any);
                        }
                    }
                });
            } else {
                mViewGoBack.setEnabled(false);
            }
        }
    };// mBtnGoBackOnClickListener

    private final View.OnClickListener mBtnLocationOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof IFile)
                goTo((IFile) v.getTag());
        }
    };// mBtnLocationOnClickListener

    private final View.OnLongClickListener mBtnLocationOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (IFileProvider.FilterMode.FilesOnly.equals(mFileProvider.getFilterMode()) || mIsSaveDialog)
                return false;

            doFinish((IFile) v.getTag());

            return false;
        }

    };// mBtnLocationOnLongClickListener

    private final View.OnClickListener mBtnGoForwardOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /*
             * if user deleted a dir which was one in history, then maybe there
             * are duplicates, so we check and remove them here
             */
            IFile currentLoc = getLocation();
            IFile nextLoc = null;
            while (currentLoc.equalsToPath(nextLoc = mHistory.nextOf(currentLoc)))
                mHistory.remove(nextLoc);

            if (nextLoc != null) {
                setLocation(nextLoc, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            mViewGoBack.setEnabled(true);
                            mViewGoForward.setEnabled(mHistory.nextOf(getLocation()) != null);
                            mFullHistory.push((IFile) any);
                        }
                    }
                });
            } else {
                mViewGoForward.setEnabled(false);
            }
        }
    };// mBtnGoForwardOnClickListener
    
    private void updateUI(IFile dir){
        final boolean isDirectoryWriteable = ((File)dir).canWrite();
        mViewCreateFolder.setEnabled(isDirectoryWriteable);
        mBtnOk.setEnabled(
            isDirectoryWriteable ||
            mFileProvider.getFilterMode() == IFileProvider.FilterMode.AnyDirectories
        );
    }
    
    private final View.OnLongClickListener mBtnGoBackForwardOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            ViewFilesContextMenuUtils.doShowHistoryContents(FileChooserActivity.this, mFileProvider, mFullHistory,
                    getLocation(), new TaskListener() {

                        @Override
                        public void onFinish(boolean ok, Object any) {
                            mHistory.removeAll(new HistoryFilter<IFile>() {

                                @Override
                                public boolean accept(IFile item) {
                                    return mFullHistory.indexOf(item) < 0;
                                }
                            });

                            if (any instanceof IFile) {
                                setLocation((IFile) any, new TaskListener() {

                                    @Override
                                    public void onFinish(boolean ok, Object any) {
                                        if (ok)
                                            mHistory.notifyHistoryChanged();
                                    }
                                });
                            } else if (mHistory.isEmpty()) {
                                mHistory.push(getLocation());
                                mFullHistory.push(getLocation());
                            }
                        }// onFinish()
                    });
            return false;
        }// onLongClick()
    };// mBtnGoBackForwardOnLongClickListener

    private final TextView.OnEditorActionListener mTxtFilenameOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Ui.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
                mBtnSave.performClick();
                return true;
            }
            return false;
        }
    };// mTxtFilenameOnEditorActionListener
    
    private final View.OnClickListener mBtnOk_ActionBar_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getLocation() instanceof File) {
                if (mFileProvider.getFilterMode() != IFileProvider.FilterMode.AnyDirectories) {
                    if (!((File)getLocation()).canWrite()) {
                        Dlg.toast(FileChooserActivity.this, R.string.afc_msg_app_cant_choose_folder, Dlg._LengthShort);
                        return;
                    }
                }
            }
            doFinish();
            FileChooserActivity.this.finish();
        }
    };// mBtnOk_ActionBar_OnClickListener

    private final View.OnClickListener mBtnCancel_ActionBar_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileChooserActivity.this.finish();
        }
    };// mBtnOk_ActionBar_OnClickListener

    private final View.OnClickListener mBtnSave_SaveDialog_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Ui.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
            String filename = mTxtSaveas.getText().toString().trim();
            doCheckSaveasFilenameAndFinish(filename);
        }
    };// mBtnSave_SaveDialog_OnClickListener

    private final View.OnClickListener mBtnSave_OpenDialog_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            List<IFile> list = new ArrayList<IFile>();
            for (int i = 0; i < mViewFiles.getAdapter().getCount(); i++) {
                // NOTE: header and footer don't have data
                Object obj = mViewFiles.getAdapter().getItem(i);
                if (obj instanceof IFileDataModel) {
                    IFileDataModel dm = (IFileDataModel) obj;
                    if (dm.isSelected())
                        list.add(dm.getFile());
                }
            }
            doFinish((ArrayList<IFile>) list);
        }
    };// mBtnSave_OpenDialog_OnClickListener

    /*
     * LIST VIEW HELPER
     */

    private GestureDetector mListviewFilesGestureDetector;

    private void initGestureDetector() {
        mListviewFilesGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private Object getData(float x, float y) {
                int i = getSubViewId(x, y);
                if (i >= 0)
                    return mViewFiles.getItemAtPosition(mViewFiles.getFirstVisiblePosition() + i);
                return null;
            }// getSubView()

            /**
             * Gets {@link IFileDataModel} from {@code e}.
             * 
             * @param e
             *            {@link MotionEvent}.
             * @return the data model, or {@code null} if not available.
             */
            private IFileDataModel getDataModel(MotionEvent e) {
                Object o = getData(e.getX(), e.getY());
                return o instanceof IFileDataModel ? (IFileDataModel) o : null;
            }// getDataModel()

            private int getSubViewId(float x, float y) {
                Rect r = new Rect();
                for (int i = 0; i < mViewFiles.getChildCount(); i++) {
                    mViewFiles.getChildAt(i).getHitRect(r);
                    if (r.contains((int) x, (int) y))
                        return i;
                }

                return -1;
            }// getSubViewId()

            @Override
            public void onLongPress(MotionEvent e) {
                // do nothing
            }// onLongPress()

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // do nothing
                return false;
            }// onSingleTapConfirmed()

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mDoubleTapToChooseFiles) {
                    if (mIsMultiSelection)
                        return false;

                    IFileDataModel data = getDataModel(e);
                    if (data == null)
                        return false;

                    if (data.getFile().isDirectory()
                            && IFileProvider.FilterMode.FilesOnly.equals(mFileProvider.getFilterMode()))
                        return false;

                    // if mFilterMode == DirectoriesOnly, files won't be
                    // shown

                    if (mIsSaveDialog) {
                        if (data.getFile().isFile()) {
                            mTxtSaveas.setText(data.getFile().getName());
                            doCheckSaveasFilenameAndFinish(data.getFile().getName());
                        } else
                            return false;
                    } else
                        doFinish(data.getFile());
                }// double tap to choose files
                else {
                    // do nothing
                    return false;
                }// single tap to choose files

                return true;
            }// onDoubleTap()

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int _max_y_distance = 19;// 10 is too short :-D
                final int _min_x_distance = 80;
                final int _min_x_velocity = 200;
                if (Math.abs(e1.getY() - e2.getY()) < _max_y_distance
                        && Math.abs(e1.getX() - e2.getX()) > _min_x_distance && Math.abs(velocityX) > _min_x_velocity) {
                    Object o = getData(e1.getX(), e1.getY());
                    if (o instanceof IFileDataModel) {
                        ((IFileDataModel) o).setTobeDeleted(true);
                        mFileAdapter.notifyDataSetChanged();
                        doDeleteFile((IFileDataModel) o);
                    }
                }

                /*
                 * always return false to let the default handler draw the item
                 * properly
                 */
                return false;
            }// onFling()
        });// mListviewFilesGestureDetector
    }// initGestureDetector()

    private final AdapterView.OnItemClickListener mViewFilesOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IFileDataModel data = mFileAdapter.getItem(position);

            if (data.getFile().isDirectory()) {
                goTo(data.getFile());
                return;
            }

            if (mIsSaveDialog)
                mTxtSaveas.setText(data.getFile().getName());

            if (mDoubleTapToChooseFiles) {
                // do nothing
                return;
            }// double tap to choose files
            else {
                if (mIsMultiSelection) {
                    return;
                }

                if (mIsSaveDialog) {
                    doCheckSaveasFilenameAndFinish(data.getFile().getName());
                } else {
                    final IFileAdapter.Bag bag = (IFileAdapter.Bag)view.getTag();
                    if (bag != null && bag.mIsAccessible) {
                        doFinish(data.getFile());
                    }
                }
            }// single tap to choose files
        }// onItemClick()
    };// mViewFilesOnItemClickListener

    private final AdapterView.OnItemLongClickListener mViewFilesOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            IFileDataModel data = mFileAdapter.getItem(position);

            if (mDoubleTapToChooseFiles) {
                // do nothing
            }// double tap to choose files
            else {
                if (!mIsSaveDialog
                        && !mIsMultiSelection
                        && data.getFile().isDirectory()
                        && (IFileProvider.FilterMode.DirectoriesOnly.equals(mFileProvider.getFilterMode()) || IFileProvider.FilterMode.FilesAndDirectories
                                .equals(mFileProvider.getFilterMode()))) {
                    doFinish(data.getFile());
                }
            }// single tap to choose files

            // notify that we already handled long click here
            return true;
        }// onItemLongClick()
    };// mViewFilesOnItemLongClickListener
}
