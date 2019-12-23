package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.viewHolder.base.MessageHolderBase;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder1 extends MessageHolderBase {

    private TextView tv_title;
    private LinearLayout sobot_ll_content;
    private LinearLayout sobot_horizontal_scrollview_layout;
    private HorizontalScrollView sobot_horizontal_scrollview;
    private LinearLayout sobot_ll_transferBtn;//只包含转人工按钮
    private TextView sobot_tv_transferBtn;//机器人转人工按钮
    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder1(Context context, View convertView) {
        super(context, convertView);
        tv_title = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot__template1_msg"));
        sobot_horizontal_scrollview_layout = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_horizontal_scrollview_layout"));
        sobot_horizontal_scrollview = (HorizontalScrollView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_horizontal_scrollview"));
        sobot_ll_content = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_content"));
        sobot_ll_transferBtn = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_transferBtn"));
        sobot_tv_transferBtn = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_transferBtn"));

    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)){
                HtmlTools.getInstance(context).setRichText(tv_title, msgStr.replaceAll("\n", "<br/>"), getLinkTextColor());
                sobot_ll_content.setVisibility(View.VISIBLE);
            } else {
                sobot_ll_content.setVisibility(View.INVISIBLE);
            }
            checkShowTransferBtn();
            final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
            if ("000000".equals(multiDiaRespInfo.getRetCode()) && interfaceRetList != null && interfaceRetList.size() > 0) {
                sobot_horizontal_scrollview.setVisibility(View.VISIBLE);
                int childCount = sobot_horizontal_scrollview_layout.getChildCount();
                for (int i = interfaceRetList.size(); i < childCount; i++) {
                    sobot_horizontal_scrollview_layout.getChildAt(i).setVisibility(View.GONE);
                }
                for (int i = 0; i < interfaceRetList.size(); i++) {
                    final Map<String, String> interfaceRet = interfaceRetList.get(i);
                    Template1ViewHolder viewHolder = null;
                    if (i < childCount) {
                        View convertView = sobot_horizontal_scrollview_layout.getChildAt(i);
                        convertView.setVisibility(View.VISIBLE);
                        viewHolder = (Template1ViewHolder) convertView.getTag();
                    } else {
                        View view = View.inflate(context, ResourceUtils.getIdByName(context, "layout", "sobot_chat_msg_item_template1_item_l"), null);
                        viewHolder = new Template1ViewHolder(context, view,msgCallBack);
                        view.setTag(viewHolder);
                        sobot_horizontal_scrollview_layout.addView(view);
                    }
                    viewHolder.bindData(context, message, interfaceRet, multiDiaRespInfo);
                }
            } else {
                sobot_horizontal_scrollview.setVisibility(View.GONE);
            }
        }
        applyTextViewUIConfig(tv_title);
    }

    public static class Template1ViewHolder implements OnClickListener{
        LinearLayout sobotLayout;
        ImageView sobotThumbnail;
        TextView sobotTitle;
        TextView sobotSummary;
        TextView sobotLable;
        Context mContext;
        Map<String, String> mInterfaceRet;
        SobotMultiDiaRespInfo mMultiDiaRespInfo;
        SobotMsgAdapter.SobotMsgCallBack msgCallBack;
        ZhiChiMessageBase zhiChiMessageBase;

        public Template1ViewHolder(Context context, View convertView, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            sobotLayout = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_item_"));
            sobotThumbnail = (ImageView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_item_thumbnail"));
            sobotTitle = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_item_title"));
            sobotSummary = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_item_summary"));
            sobotLable = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template1_item_lable"));
            this.msgCallBack = msgCallBack;
        }

        public void bindData(final Context context, final ZhiChiMessageBase message, final Map<String, String> interfaceRet, final SobotMultiDiaRespInfo multiDiaRespInfo) {
            zhiChiMessageBase=message;
            mContext = context;
            mInterfaceRet = interfaceRet;
            mMultiDiaRespInfo = multiDiaRespInfo;
            if (interfaceRet != null && interfaceRet.size() > 0) {
                SobotBitmapUtil.display(context, interfaceRet.get("thumbnail"), sobotThumbnail, 0, 0);
                sobotTitle.setText(interfaceRet.get("title"));
                sobotSummary.setText(interfaceRet.get("summary"));
                sobotLable.setText(interfaceRet.get("label"));

                if (message.getSugguestionsFontColor() == 0) {
                    sobotLayout.setEnabled(true);
                    sobotLayout.setOnClickListener(this);
                } else {
                    String lastCid = SharedPreferencesUtil.getStringData(mContext, "lastCid", "");
                    //当前cid相同才能重复点;ClickFlag 是否允许多次点击 0:不允许 1:允许
                    if (mMultiDiaRespInfo.getClickFlag() == 1 && !TextUtils.isEmpty(message.getCid()) && lastCid.equals(message.getCid())) {
                        sobotLayout.setEnabled(true);
                        sobotLayout.setOnClickListener(this);
                    } else {
                        sobotLayout.setEnabled(false);
                    }
                }
            }

        }

        @Override
        public void onClick(View v) {
            if(mContext == null || mMultiDiaRespInfo == null || mInterfaceRet == null){
                return;
            }
            String lastCid = SharedPreferencesUtil.getStringData(mContext, "lastCid", "");
            //当前cid相同相同才能重复点;ClickFlag 是否允许多次点击 0:只点击一次 1:允许重复点击
            //ClickFlag=0 时  ClickCount=0可点击，大于0 不可点击
            if (zhiChiMessageBase.getSugguestionsFontColor() == 0) {
                if (!TextUtils.isEmpty(zhiChiMessageBase.getCid()) && lastCid.equals(zhiChiMessageBase.getCid())) {
                    if (zhiChiMessageBase.getAnswer().getMultiDiaRespInfo().getClickFlag() == 0 && zhiChiMessageBase.getClickCount() > 0) {
                        return;
                    }
                    zhiChiMessageBase.addClickCount();
                } else {
                    return;
                }
            } else {
                return;
            }

            if (mMultiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(mInterfaceRet.get("anchor"))) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("url", mInterfaceRet.get("anchor"));
                mContext.startActivity(intent);
            } else {
                ChatUtils.sendMultiRoundQuestions(mContext, mMultiDiaRespInfo, mInterfaceRet,msgCallBack);
            }
        }
    }

    private void checkShowTransferBtn() {
        if (message.getTransferType() == 4) {
            //4 多次命中 显示转人工
            showTransferBtn();
        } else {
            //隐藏转人工
            hideTransferBtn();
        }
    }

    /**
     * 隐藏转人工按钮
     */
    public void hideTransferBtn() {
        sobot_ll_transferBtn.setVisibility(View.GONE);
        sobot_tv_transferBtn.setVisibility(View.GONE);
        if (message != null) {
            message.setShowTransferBtn(false);
        }
    }

    /**
     * 显示转人工按钮
     */
    public void showTransferBtn() {
        sobot_tv_transferBtn.setVisibility(View.VISIBLE);
        sobot_ll_transferBtn.setVisibility(View.VISIBLE);
        if (message != null) {
            message.setShowTransferBtn(true);
        }
        sobot_ll_transferBtn.setOnClickListener(new NoDoubleClickListener() {

            @Override
            public void onNoDoubleClick(View v) {
                if (msgCallBack != null) {
                    msgCallBack.doClickTransferBtn();
                }
            }
        });
    }
}