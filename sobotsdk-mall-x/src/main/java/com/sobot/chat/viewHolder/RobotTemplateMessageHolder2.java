package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.viewHolder.base.MessageHolderBase;
import com.sobot.chat.widget.lablesview.SobotLabelsView;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder2 extends MessageHolderBase implements SobotLabelsView.OnLabelClickListener, View.OnClickListener {
    // 聊天的消息内容
    private TextView tv_msg;
    private TextView tv_more;
    private LinearLayout sobot_ll_content;

    private LinearLayout sobot_ll_transferBtn;//只包含转人工按钮
    private TextView sobot_tv_transferBtn;//机器人转人工按钮
    // 标签页控件
    private SobotLabelsView slv_labels;

    private ZhiChiMessageBase zhiChiMessageBase;

    private static final int PAGE_SIZE = 9;

    public RobotTemplateMessageHolder2(Context context, View convertView) {
        super(context, convertView);
        tv_msg = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template2_msg"));
        tv_more = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_more"));
        slv_labels = (SobotLabelsView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template2_labels"));
        sobot_ll_content = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_content"));
        tv_more.setOnClickListener(this);
        sobot_ll_transferBtn = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_transferBtn"));
        sobot_tv_transferBtn = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_transferBtn"));

    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        zhiChiMessageBase = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                applyTextViewUIConfig(tv_msg);
                HtmlTools.getInstance(context).setRichText(tv_msg, msgStr, getLinkTextColor());
                sobot_ll_content.setVisibility(View.VISIBLE);
            } else {
                sobot_ll_content.setVisibility(View.INVISIBLE);
            }
            checkShowTransferBtn();
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                String[] inputContent = multiDiaRespInfo.getInputContentList();
                ArrayList<SobotLablesViewModel> label = new ArrayList<>();
                if (interfaceRetList != null && interfaceRetList.size() > 0) {
                    for (int i = 0; i < getDisplayNum(multiDiaRespInfo, interfaceRetList.size()); i++) {
                        Map<String, String> interfaceRet = interfaceRetList.get(i);
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(interfaceRet.get("title"));
                        lablesViewModel.setAnchor(interfaceRet.get("anchor"));
                        label.add(lablesViewModel);
                    }
                    // 显示更多
                    showMoreBtn(multiDiaRespInfo, interfaceRetList.size());
                    slv_labels.setVisibility(View.VISIBLE);
                    slv_labels.setLabels(label);
                } else if (inputContent != null && inputContent.length > 0) {
                    for (int i = 0; i < getDisplayNum(multiDiaRespInfo, inputContent.length); i++) {
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(inputContent[i]);
                        label.add(lablesViewModel);
                    }
                    // 显示更多
                    showMoreBtn(multiDiaRespInfo, inputContent.length);
                    slv_labels.setVisibility(View.VISIBLE);
                    slv_labels.setLabels(label);
                } else {
                    hideMoreBtn(multiDiaRespInfo);
                    slv_labels.setVisibility(View.GONE);
                }

                slv_labels.setOnLabelClickListener(this);
                slv_labels.setTabEnable(true);
            } else {
                slv_labels.setVisibility(View.GONE);
                hideMoreBtn(multiDiaRespInfo);
            }
        }
    }

    //更多按钮的显示
    private void showMoreBtn(SobotMultiDiaRespInfo multiDiaRespInfo, int maxSize) {
        if (multiDiaRespInfo == null || mContext == null) {
            return;
        }
        tv_more.setVisibility(View.VISIBLE);
        if (multiDiaRespInfo.getPageNum() == 1 && multiDiaRespInfo.getPageNum() * PAGE_SIZE >= maxSize) {
            hideMoreBtn(multiDiaRespInfo);
        } else if (multiDiaRespInfo.getPageNum() * PAGE_SIZE >= maxSize) {
            //最后一页  收起全部
            tv_more.setText(ResourceUtils.getIdByName(mContext, "string", "sobot_collapse"));
            tv_more.setSelected(true);
        } else {
            //不是最后一页 展开更多
            tv_more.setText(ResourceUtils.getIdByName(mContext, "string", "sobot_more"));
            tv_more.setSelected(false);
        }
    }

    private void hideMoreBtn(SobotMultiDiaRespInfo multiDiaRespInfo) {
        if (multiDiaRespInfo != null) {
            multiDiaRespInfo.setPageNum(1);
        }
        tv_more.setVisibility(View.GONE);

    }

    private int getDisplayNum(SobotMultiDiaRespInfo multiDiaRespInfo, int maxSize) {
        if (multiDiaRespInfo == null) {
            return 0;
        }
        return Math.min(multiDiaRespInfo.getPageNum() * PAGE_SIZE, maxSize);
    }

    @Override
    public void onLabelClick(View label, SobotLablesViewModel data, int position) {
        if (zhiChiMessageBase == null || zhiChiMessageBase.getAnswer() == null) {
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
        SobotMultiDiaRespInfo multiDiaRespInfo = zhiChiMessageBase.getAnswer().getMultiDiaRespInfo();
        if (multiDiaRespInfo != null && multiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(data.getAnchor())) {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", data.getAnchor());
            mContext.startActivity(intent);
        } else {
            sendMultiRoundQuestions(data, multiDiaRespInfo);
        }
    }

    private void sendMultiRoundQuestions(SobotLablesViewModel data, SobotMultiDiaRespInfo multiDiaRespInfo) {
        if (multiDiaRespInfo == null) {
            return;
        }
        String labelText = data.getTitle();
        String[] outputParam = multiDiaRespInfo.getOutPutParamList();
        if (msgCallBack != null && zhiChiMessageBase != null) {
            ZhiChiMessageBase msgObj = new ZhiChiMessageBase();

            Map<String, String> map = new HashMap<>();
            map.put("level", multiDiaRespInfo.getLevel());
            map.put("conversationId", multiDiaRespInfo.getConversationId());
            if (outputParam != null && outputParam.length > 0) {

                for (String anOutputParam : outputParam) {
                    map.put(anOutputParam, labelText);
                }
            }
            msgObj.setContent(GsonUtil.map2Str(map));
            msgObj.setId(System.currentTimeMillis() + "");
            msgCallBack.sendMessageToRobot(msgObj, 4, 2, labelText, labelText);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == tv_more) {
            if (zhiChiMessageBase != null && zhiChiMessageBase.getAnswer() != null && zhiChiMessageBase.getSugguestionsFontColor() == 0) {
                SobotMultiDiaRespInfo info = zhiChiMessageBase.getAnswer().getMultiDiaRespInfo();
                if (info != null && "000000".equals(info.getRetCode())) {
                    if (tv_more.isSelected()) {
                        //最后一页
                        info.setPageNum(1);
                    } else {
                        info.setPageNum((info.getPageNum() + 1));
                    }
                    bindData(mContext, zhiChiMessageBase);
                }
            }
        }
    }

    private void checkShowTransferBtn() {
        if (zhiChiMessageBase.getTransferType() == 4) {
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
        if (zhiChiMessageBase != null) {
            zhiChiMessageBase.setShowTransferBtn(false);
        }
    }

    /**
     * 显示转人工按钮
     */
    public void showTransferBtn() {
        sobot_tv_transferBtn.setVisibility(View.VISIBLE);
        sobot_ll_transferBtn.setVisibility(View.VISIBLE);
        if (zhiChiMessageBase != null) {
            zhiChiMessageBase.setShowTransferBtn(true);
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