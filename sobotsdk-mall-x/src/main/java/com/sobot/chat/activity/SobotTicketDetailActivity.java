package com.sobot.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.SobotApi;
import com.sobot.chat.activity.base.SobotBaseActivity;
import com.sobot.chat.adapter.SobotTicketDetailAdapter;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.StUserDealTicketInfo;
import com.sobot.chat.core.http.callback.StringResultCallBack;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.widget.dialog.SobotTicketEvaluateDialog;

import java.util.ArrayList;
import java.util.List;

public class SobotTicketDetailActivity extends SobotBaseActivity implements SobotTicketEvaluateDialog.SobotTicketEvaluateCallback {

    public static final int REQUEST_CODE_REPLY = 1001;

    public static final String INTENT_KEY_UID = "intent_key_uid";
    public static final String INTENT_KEY_COMPANYID = "intent_key_companyid";
    public static final String INTENT_KEY_TICKET_INFO = "intent_key_ticket_info";

    private String mUid = "";
    private String mCompanyId = "";
    private SobotUserTicketInfo mTicketInfo;
    private int infoFlag;

    private List<Object> mList = new ArrayList<>();
    private ListView mListView;
    private SobotTicketDetailAdapter mAdapter;

    /**
     * @param context 应用程序上下文
     * @return
     */
    public static Intent newIntent(Context context, String companyId, String uid, SobotUserTicketInfo ticketInfo) {
        Intent intent = new Intent(context, SobotTicketDetailActivity.class);
        intent.putExtra(INTENT_KEY_UID, uid);
        intent.putExtra(INTENT_KEY_COMPANYID, companyId);
        intent.putExtra(INTENT_KEY_TICKET_INFO, ticketInfo);
        return intent;
    }

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_activity_ticket_detail");
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(INTENT_KEY_UID);
            mCompanyId = getIntent().getStringExtra(INTENT_KEY_COMPANYID);
            mTicketInfo = (SobotUserTicketInfo) getIntent().getSerializableExtra(INTENT_KEY_TICKET_INFO);
            if (mTicketInfo != null) {
                infoFlag = mTicketInfo.getFlag();//保留原始状态
            }
        }
    }

    @Override
    protected void initView() {

        showLeftMenu(getResDrawableId("sobot_btn_back_selector"), getResString("sobot_back"), true);
        showRightMenu(0, getResString("sobot_reply"), false);
        setTitle(getResString("sobot_message_details"));
        mListView = (ListView) findViewById(getResId("sobot_listview"));

    }

    @Override
    protected void onRightMenuClick(View view) {
        //跳转回复
        Intent intent = new Intent(this, SobotReplyTicketActivity.class);
        intent.putExtra(INTENT_KEY_UID, mUid);
        intent.putExtra(INTENT_KEY_COMPANYID, mCompanyId);
        intent.putExtra(INTENT_KEY_TICKET_INFO, mTicketInfo);
        startActivityForResult(intent, REQUEST_CODE_REPLY);
    }

    @Override
    public void onBackPressed() {//返回
        if (mTicketInfo != null && infoFlag != mTicketInfo.getFlag()) {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();

    }

    @Override
    protected void initData() {
        if (mTicketInfo == null) {
            return;
        }
        zhiChiApi.getUserDealTicketInfoList(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketInfo.getTicketId(), new StringResultCallBack<List<StUserDealTicketInfo>>() {

            @Override
            public void onSuccess(List<StUserDealTicketInfo> datas) {
                if (datas != null && datas.size() > 0) {
                    mList.clear();
                    for (StUserDealTicketInfo info : datas) {
                        if (info.getFlag() == 1) {//创建
                            mTicketInfo.setFileList(info.getFileList());
                            break;
                        }
                    }
                    mList.add(mTicketInfo);
                    mList.addAll(datas);


                    for (StUserDealTicketInfo dealTicketInfo : datas) {
//                        StUserDealTicketInfo dealTicketInfo = datas.get(0);
                        if (dealTicketInfo.getFlag() == 3 && mTicketInfo.getFlag() != 3) {//有结束标志
                            mTicketInfo.setFlag(3);
                        }

                        if (mTicketInfo.getFlag() != 3 && mTicketInfo.getFlag() < dealTicketInfo.getFlag()) {//不是结束
                            mTicketInfo.setFlag(dealTicketInfo.getFlag());
                        }

                    }


                    if (mAdapter == null) {
                        mAdapter = new SobotTicketDetailAdapter(SobotTicketDetailActivity.this, mList);
                        mListView.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }

                    showRightMenu(0, getResString("sobot_reply"), SobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY) || mTicketInfo.getFlag() != 3);

                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(SobotTicketDetailActivity.this, des);
            }
        });
    }

    @Override
    public void submitEvaluate(final int score, final String remark) {
        zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketInfo.getTicketId(), score, remark, new StringResultCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                CustomToast.makeText(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_leavemsg_success_tip"), 1000, ResourceUtils.getDrawableId(SobotTicketDetailActivity.this, "sobot_iv_login_right")).show();
                for (int i = 0; i < mList.size(); i++) {
                    Object obj = mList.get(i);
                    if (obj instanceof StUserDealTicketInfo) {
                        StUserDealTicketInfo data = (StUserDealTicketInfo) mList.get(i);
                        if (data.getFlag() == 3 && data.getEvaluate() != null) {
                            SobotUserTicketEvaluate evaluate = data.getEvaluate();
                            evaluate.setScore(score);
                            evaluate.setRemark(remark);
                            evaluate.setEvalution(true);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_REPLY) {//回复
            if (resultCode == Activity.RESULT_OK) {
                CustomToast.makeText(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_leavemsg_success_tip"), Toast.LENGTH_LONG).show();
            }
            try {
                Thread.sleep(1000);//睡眠一秒  延迟拉取数据
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initData();
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}