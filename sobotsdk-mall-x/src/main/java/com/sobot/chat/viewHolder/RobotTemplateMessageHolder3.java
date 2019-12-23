package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.viewHolder.base.MessageHolderBase;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder3 extends MessageHolderBase implements View.OnClickListener {
    private TextView sobot_msg;
    private TextView tv_more;
    private LinearLayout sobot_ll_content;
    private LinearLayout sobot_template3_layout;

    private LinearLayout sobot_ll_transferBtn;//只包含转人工按钮
    private TextView sobot_tv_transferBtn;//机器人转人工按钮
    private ZhiChiMessageBase zhiChiMessageBase;

    private static final int PAGE_SIZE = 3;

    public RobotTemplateMessageHolder3(Context context, View convertView) {
        super(context, convertView);
        sobot_msg = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_msg"));
        sobot_template3_layout = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_layout"));
        tv_more = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_more"));
        sobot_ll_content = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_content"));
        tv_more.setOnClickListener(this);
        sobot_ll_transferBtn = (LinearLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_ll_transferBtn"));
        sobot_tv_transferBtn = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_tv_transferBtn"));

    }

    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        zhiChiMessageBase = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                sobot_msg.setText(msgStr);
                sobot_ll_content.setVisibility(View.VISIBLE);
                applyTextViewUIConfig(sobot_msg);
            } else {
                sobot_ll_content.setVisibility(View.INVISIBLE);
            }
            checkShowTransferBtn();
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                if (interfaceRetList != null && interfaceRetList.size() > 0) {
                    showMoreBtn(multiDiaRespInfo, interfaceRetList.size());
                    sobot_template3_layout.setVisibility(View.VISIBLE);
                    sobot_template3_layout.removeAllViews();
                    for (int i = 0; i < getDisplayNum(multiDiaRespInfo, interfaceRetList.size()); i++) {
                        final Map<String, String> interfaceRet = interfaceRetList.get(i);
                        if (interfaceRet != null && interfaceRet.size() > 0) {
                            View view = View.inflate(context, ResourceUtils.getIdByName(context, "layout", "sobot_chat_msg_item_template3_item_l"), null);
                            LinearLayout sobot_template3_anchor = (LinearLayout) view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_anchor"));
                            ImageView sobot_template3_thumbrail = (ImageView) view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_thumbrail"));
                            TextView sobot_template3_title = (TextView) view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_title"));
                            TextView sobot_template3_summary = (TextView) view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_summary"));
                            TextView sobot_template3_tag = (TextView) view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_tag"));
                            View sobot_template3_line = view.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_template3_line"));
                            if (i == getDisplayNum(multiDiaRespInfo, interfaceRetList.size()) - 1) {
                                sobot_template3_line.setVisibility(View.VISIBLE);
                            } else {
                                sobot_template3_line.setVisibility(View.GONE);
                            }

                            if (!TextUtils.isEmpty(interfaceRet.get("thumbnail"))) {
                                sobot_template3_thumbrail.setVisibility(View.VISIBLE);
                                SobotBitmapUtil.display(context, interfaceRet.get("thumbnail"), sobot_template3_thumbrail, ResourceUtils.getIdByName(context, "drawable", "sobot_logo_icon"), ResourceUtils.getIdByName(context, "drawable", "sobot_logo_icon"));
                            } else {
                                sobot_template3_thumbrail.setVisibility(View.GONE);
                            }

                            sobot_template3_title.setText(interfaceRet.get("title"));
                            String summary = interfaceRet.get("summary");
                            if (!TextUtils.isEmpty(summary)) {
                                sobot_template3_summary.setVisibility(View.VISIBLE);
                                sobot_template3_summary.setText(summary);
                            } else {
                                sobot_template3_summary.setVisibility(View.GONE);
                            }
                            sobot_template3_tag.setText(interfaceRet.get("tag"));

                            sobot_template3_anchor.setTag(interfaceRet);
                            sobot_template3_anchor.setEnabled(true);
                            sobot_template3_anchor.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(mContext == null || multiDiaRespInfo == null || interfaceRet == null){
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

                                    if (multiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(interfaceRet.get("anchor"))) {
                                        Intent intent = new Intent(mContext, WebViewActivity.class);
                                        intent.putExtra("url", interfaceRet.get("anchor"));
                                        mContext.startActivity(intent);
                                    } else {
                                        ChatUtils.sendMultiRoundQuestions(mContext, multiDiaRespInfo, interfaceRet,msgCallBack);
                                    }
                                }
                            });
                            sobot_template3_layout.addView(view);
                        }
                    }
                } else {
                    hideMoreBtn(multiDiaRespInfo);
                    sobot_template3_layout.setVisibility(View.GONE);
                }
            } else {
                hideMoreBtn(multiDiaRespInfo);
                sobot_template3_layout.setVisibility(View.GONE);
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