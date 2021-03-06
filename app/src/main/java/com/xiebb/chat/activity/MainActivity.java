package com.xiebb.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseConversationListFragment;
import com.hyphenate.util.NetUtils;
import com.xiebb.chat.R;
import com.xiebb.chat.huanxin.activity.ChatActivity;
import com.xiebb.chat.huanxin.fragment.ContactListFragment;
import com.xiebb.chat.utils.IntentUtil;
import com.xiebb.chat.utils.SpUtils;
import com.xiebb.chat.utils.ToastUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private DrawerLayout drawer;
    private ContactListFragment contactListFragment;
    private Toolbar mToolbar;
    private EaseConversationListFragment easeConversationListFragment;
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        connectionListener();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("消息");
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.show(getApplicationContext(), "直播功能正在开发中");
            }
        });

        //toolbar左边菜单按钮
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view = navigationView.getHeaderView(0);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        easeConversationListFragment = new EaseConversationListFragment();
        easeConversationListFragment.setConversationListItemClickListener(new EaseConversationListFragment.EaseConversationListItemClickListener() {

            @Override
            public void onListItemClicked(EMConversation conversation) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, conversation.getUserName()));
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.content, easeConversationListFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
//            getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.contact, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ToastUtils.show(getApplicationContext(), "设置功能正在开发");
            return true;
        } else if (id == R.id.add_frienid) {
            intent = new Intent(this, AddFriendActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.message) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, easeConversationListFragment).commit();
            mToolbar.setTitle("消息");
        } else if (id == R.id.contanct) {
            mToolbar.setTitle("联系人");
            contactListFragment = new ContactListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.content, contactListFragment).commit();
        } else if (id == R.id.dynamic) {
            ToastUtils.show(getApplicationContext(), "动态功能正在开发中");
//            mToolbar.setTitle("动态");
        } else if (id == R.id.setting) {
            ToastUtils.show(getApplicationContext(), "设置功能正在开发中");
//            mToolbar.setTitle("设置");
        }
        closeDrawer();
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        closeDrawer();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.imageView:
                if (TextUtils.isEmpty(SpUtils.getString(getApplicationContext(), "username"))) {
                    intent = IntentUtil.getInstance();
                    intent.setClass(getApplicationContext(), LoginActivity.class);
                } else {
                    intent = IntentUtil.getInstance();
                    intent.setClass(getApplicationContext(), MeActivity.class);
                }
                startActivity(intent);
                break;
        }
    }

    private void closeDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void connectionListener() {
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
    }

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  if (error == EMError.USER_REMOVED) {
                                      // 显示帐号已经被移除
                                  } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                                      // 显示帐号在其他设备登录
                                  } else {
                                      if (NetUtils.hasNetwork(MainActivity.this)) {

                                      }
                                      //连接不到聊天服务器
                                      else {
                                          //当前网络不可用，请检查网络设置
                                      }

                                  }
                              }
                          }
            );
        }
    }

    public void replaceChat(Fragment fragment) {

    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    isExit = false;
                }
            }, 5000);
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
