package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.viewHolder.base.MessageHolderBase;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder6 extends MessageHolderBase {

    private TextView sobot_template6_title;
    private TextView sobot_template6_msg;

    private LinearLayout sobot_ll_transferBtn;//只包含转人工按钮
    private TextView sobot_tv_transferBtn;//机器人转人工按钮
    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder6(Context context, View convertView) {
        super(context, convertView);
        sobot_template6_msg = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template6_msg"));
        sobot_template6_title = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template6_title"));
        sobot_ll_transferBtn = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_transferBtn"));
        sobot_tv_transferBtn = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_transferBtn"));

    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            sobot_template6_msg.setText(ChatUtils.getMultiMsgTitle(multiDiaRespInfo));
            applyTextViewUIConfig(sobot_template6_msg);
            checkShowTransferBtn();
            final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
            if ("000000".equals(multiDiaRespInfo.getRetCode()) && interfaceRetList != null && interfaceRetList.size() > 0) {
                final Map<String, String> interfaceRet = interfaceRetList.get(0);
                if (interfaceRet != null && interfaceRet.size() > 0) {
                    setSuccessView();
                    HtmlTools.getInstance(context).setRichText(sobot_template6_title, interfaceRet.get("tempStr").replace("<div>","").replace("</div>","").replace("<p>", "").replace("</p>", "<br/>"), getLinkTextColor());
                }
            } else {
                setFailureView();
            }
        }
    }


    private void setSuccessView() {
        sobot_template6_msg.setVisibility(View.VISIBLE);
        sobot_template6_title.setVisibility(View.VISIBLE);
    }

    private void setFailureView() {
        sobot_template6_msg.setVisibility(View.VISIBLE);
        sobot_template6_title.setVisibility(View.GONE);
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