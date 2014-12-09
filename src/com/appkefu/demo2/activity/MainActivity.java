package com.appkefu.demo2.activity;

import java.util.ArrayList;

import org.jivesoftware.smack.util.StringUtils;

import com.appkefu.demo2.R;
import com.appkefu.demo2.adapter.ApiAdapter;
import com.appkefu.demo2.entity.ApiEntity;
import com.appkefu.lib.interfaces.KFInterfaces;
import com.appkefu.lib.service.KFMainService;
import com.appkefu.lib.service.KFXmppManager;
import com.appkefu.lib.utils.KFLog;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


/**
 * 微客服(AppKeFu.com)
 * 
 * 微客服，集成到您App里的在线客服
 * 国内首款App里的在线客服，支持文字、表情、图片、语音聊天。 立志为移动开发者提供最好的在线客服
 *   
 * 技术交流QQ群:48661516
 *  
 * 客服开发文档： http://appkefu.com/AppKeFu/tutorial-android2.html
 *          
 * @author jack ning, http://github.com/pengjinning 
 *                                                    
 */ 

public class MainActivity extends Activity {

	/**                       
	 * 提示：如果已经运行过旧版的Demo，请先在手机上删除原先的App再重新运行此工程
	 * 更多使用帮助参见：http://appkefu.com/AppKeFu/tutorial-android2.html
	 */  
	
	private TextView 			 mTitle; 
	
	private ListView 			 mApiListView;
	private ArrayList<ApiEntity> mApiArray;
	private ApiAdapter 			 mAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		
		//第一步：登录
		KFInterfaces.visitorLogin(this);
	}
	 
	@Override
	protected void onStart() {
		super.onStart();

		IntentFilter intentFilter = new IntentFilter();
		//监听网络连接变化情况
        intentFilter.addAction(KFMainService.ACTION_XMPP_CONNECTION_CHANGED);
        //监听消息
        intentFilter.addAction(KFMainService.ACTION_XMPP_MESSAGE_RECEIVED);
        //工作组在线状态
        intentFilter.addAction(KFMainService.ACTION_XMPP_WORKGROUP_ONLINESTATUS);
        
        registerReceiver(mXmppreceiver, intentFilter); 
	} 

	@Override
	protected void onStop() {
		super.onStop();

        unregisterReceiver(mXmppreceiver);
	}
	

	private void initView() {

		//界面标题
		mTitle = (TextView) findViewById(R.id.demo_title);
				
		mApiListView = (ListView)findViewById(R.id.api_list_view);
		mApiArray = new ArrayList<ApiEntity>();
		
		mAdapter = new ApiAdapter(this,  mApiArray);
		mApiListView.setAdapter(mAdapter);
		
		ApiEntity entity = new ApiEntity(1, getString(R.string.chat_with_kefu));
		mApiArray.add(entity);
			entity = new ApiEntity(2, getString(R.string.set_user_tags));
		mApiArray.add(entity);
		
		mAdapter.notifyDataSetChanged();
		mApiListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long id) {
				// TODO Auto-generated method stub
				
				ApiEntity entity = mApiArray.get(index);
				
				switch(entity.getId()) {
				case 1:
					startChat();
					break;
				case 2:
					showTagList();
					break;
				default:
					break;
				};
			}
		});
	}

	
	//1.咨询客服
	private void startChat()
	{
		KFInterfaces.startChat(this, 
				"wgdemo", //注意（重要）：在此处填写工作组名
				"客服小秘书",
				"正在联系客服，请稍后...");
		
	}
	
	//
	private void showTagList()
	{
		Intent intent = new Intent(this, TagListActivity.class);
		startActivity(intent);
	}
	
	//监听：连接状态、即时通讯消息、客服在线状态
	private BroadcastReceiver mXmppreceiver = new BroadcastReceiver() 
	{
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            //监听：连接状态
            if (action.equals(KFMainService.ACTION_XMPP_CONNECTION_CHANGED))//监听链接状态
            {
                updateStatus(intent.getIntExtra("new_state", 0));        
            }
            //监听：即时通讯消息
            else if(action.equals(KFMainService.ACTION_XMPP_MESSAGE_RECEIVED))//监听消息
            {
            	//消息内容
            	String body = intent.getStringExtra("body");
            	//消息来自于
            	String from = StringUtils.parseName(intent.getStringExtra("from"));
            }
            //客服工作组在线状态
            else if(action.equals(KFMainService.ACTION_XMPP_WORKGROUP_ONLINESTATUS))
            {
            	String onlineStatus = intent.getStringExtra("onlinestatus");
            	
            	KFLog.d("客服工作组:"+ onlineStatus);//online：在线；offline: 离线
            	
            	ApiEntity entity = mApiArray.get(0);
            	
            	if(onlineStatus.equals("online"))
            	{
            		
            		entity.setApiName(getString(R.string.chat_with_kefu) + "(在线)");
            		
            		KFLog.d("online:"+entity.getApiName());
            	}
            	else
            	{
            		
            		entity.setApiName(getString(R.string.chat_with_kefu) + "(离线)");
            		
            		KFLog.d("offline:"+entity.getApiName());
            	}
            	
            	mApiArray.set(0, entity);
            	mAdapter.notifyDataSetChanged();
            }            
        }
    };
    
   //根据监听到的连接变化情况更新界面显示
    private void updateStatus(int status) {

    	switch (status) {
            case KFXmppManager.CONNECTED:
            	mTitle.setText("微客服2(Demo)");
            	
            	//查询客服工作组在线状态，返回结果在BroadcastReceiver中返回
            	KFInterfaces.checkKeFuIsOnlineAsync("wgdemo", this);
            	
                break;
            case KFXmppManager.DISCONNECTED:
            	mTitle.setText("微客服2(Demo)(未连接)");
                break;
            case KFXmppManager.CONNECTING:
            	mTitle.setText("微客服2(Demo)(登录中...)");
            	break;                 
            case KFXmppManager.DISCONNECTING:
            	mTitle.setText("微客服2(Demo)(登出中...)");
                break;
            case KFXmppManager.WAITING_TO_CONNECT:
            case KFXmppManager.WAITING_FOR_NETWORK:
            	mTitle.setText("微客服2(Demo)(等待中)");
                break;
            default:  
                throw new IllegalStateException();
        }
    }

}
