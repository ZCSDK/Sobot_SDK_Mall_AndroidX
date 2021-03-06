package com.sobot.chat.viewHolder;

import android.content.Context;

import androidx.annotation.IdRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sobot.chat.api.model.SobotEvaluateModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.viewHolder.base.MessageHolderBase;

/**
 * 客服主动邀请客户评价
 * Created by jinxl on 2017/3/17.
 */
public class CusEvaluateMessageHolder extends MessageHolderBase implements RadioGroup.OnCheckedChangeListener, RatingBar.OnRatingBarChangeListener {
    TextView sobot_center_title;
    RadioGroup sobot_readiogroup;
    RadioButton sobot_btn_ok_robot;
    RadioButton sobot_btn_no_robot;
    TextView sobot_tv_star_title;
    RatingBar sobot_ratingBar;
    TextView sobot_submit;//提交
    View sobot_ratingBar_split_view;//如果有已解决按钮和未解决按钮就显示，否则隐藏；
    SobotEvaluateModel sobotEvaluateModel;
    public ZhiChiMessageBase message;

    public CusEvaluateMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_center_title = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_center_title"));
        sobot_readiogroup = (RadioGroup) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_readiogroup"));
        sobot_btn_ok_robot = (RadioButton) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_btn_ok_robot"));
        sobot_btn_no_robot = (RadioButton) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_btn_no_robot"));
        sobot_tv_star_title = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_tv_star_title"));
        sobot_ratingBar = (RatingBar) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_ratingBar"));
        sobot_submit = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_submit"));
        sobot_ratingBar_split_view = convertView.findViewById(ResourceUtils.getIdByName(context, "id",
                "sobot_ratingBar_split_view"));
        sobot_btn_ok_robot.setSelected(true);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message = message;
        this.sobotEvaluateModel = message.getSobotEvaluateModel();


        sobot_center_title.setText(String.format(ChatUtils.getResString(context, "sobot_question"), message.getSenderName()));
        sobot_tv_star_title.setText(String.format(ChatUtils.getResString(context, "sobot_please_evaluate"), message.getSenderName()));

        checkQuestionFlag();
        refreshItem();

        sobot_readiogroup.setOnCheckedChangeListener(this);
        sobot_ratingBar.setOnRatingBarChangeListener(this);
        //默认是五星好评
        sobot_ratingBar.setRating(5);
        sobot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score = sobotEvaluateModel.getScore();
                // true 直接提交  false 打开评价窗口
                if (score == 5) {
                    doEvaluate(true);
                } else {
                    doEvaluate(false);
                }
            }
        });
    }

    /**
     * 检查是否开启   是否已解决配置
     */
    private void checkQuestionFlag() {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (ChatUtils.isQuestionFlag(sobotEvaluateModel)) {
            //是否已解决开启
            sobot_center_title.setVisibility(View.VISIBLE);
            sobot_readiogroup.setVisibility(View.VISIBLE);
            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);

        } else {
            //是否已解决关闭
            sobot_center_title.setVisibility(View.GONE);
            sobot_readiogroup.setVisibility(View.GONE);
            sobot_ratingBar_split_view.setVisibility(View.GONE);
        }
    }


    /**
     * 根据是否已经评价设置UI
     */
    public void refreshItem() {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (0 == sobotEvaluateModel.getEvaluateStatus()) {

            //未评价
            setNotEvaluatedLayout();
            sobot_submit.setVisibility(View.VISIBLE);
        } else if (1 == sobotEvaluateModel.getEvaluateStatus()) {
            //已评价
            setEvaluatedLayout();
            sobot_submit.setVisibility(View.GONE);
        }
    }

    private void setEvaluatedLayout() {
        if (sobot_readiogroup.getVisibility() == View.VISIBLE) {
            if (sobotEvaluateModel.getIsResolved() == -1) {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(false);
                sobot_btn_ok_robot.setVisibility(View.VISIBLE);
                sobot_btn_no_robot.setVisibility(View.VISIBLE);
            } else if (sobotEvaluateModel.getIsResolved() == 0) {
                sobot_btn_ok_robot.setChecked(true);
                sobot_btn_no_robot.setChecked(false);
                sobot_btn_ok_robot.setVisibility(View.VISIBLE);
                sobot_btn_no_robot.setVisibility(View.GONE);
            } else {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(true);
                sobot_btn_ok_robot.setVisibility(View.GONE);
                sobot_btn_no_robot.setVisibility(View.VISIBLE);
            }
        }
        sobot_ratingBar.setRating(sobotEvaluateModel.getScore());
        sobot_ratingBar.setEnabled(false);
    }

    private void setNotEvaluatedLayout() {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (sobot_readiogroup.getVisibility() == View.VISIBLE) {
            if (sobotEvaluateModel.getIsResolved() == -1) {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(false);
                sobot_btn_ok_robot.setVisibility(View.VISIBLE);
                sobot_btn_no_robot.setVisibility(View.VISIBLE);
            } else if (sobotEvaluateModel.getIsResolved() == 0) {
                sobot_btn_ok_robot.setChecked(true);
                sobot_btn_no_robot.setChecked(false);
                sobot_btn_ok_robot.setVisibility(View.VISIBLE);
                sobot_btn_no_robot.setVisibility(View.VISIBLE);
            } else {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(true);
                sobot_btn_ok_robot.setVisibility(View.VISIBLE);
                sobot_btn_no_robot.setVisibility(View.VISIBLE);
            }
        }

        sobot_ratingBar.setEnabled(true);
        sobot_ratingBar.setRating(sobotEvaluateModel.getScore());
    }

    /**
     * 评价 操作
     *
     * @param evaluateFlag true 直接提交  false 打开评价窗口
     */
    private void doEvaluate(boolean evaluateFlag) {
        if (mContext != null && message != null && message.getSobotEvaluateModel() != null) {
            int resolved = -1;
            if (ChatUtils.isQuestionFlag(message.getSobotEvaluateModel())) {
                if (sobot_btn_ok_robot.isChecked()) {
                    resolved = 0;
                } else if (sobot_btn_no_robot.isChecked()) {
                    resolved = 1;
                } else if (message.getSobotEvaluateModel().getScore() == 5) {
                    resolved = 0;
                }
            }
            message.getSobotEvaluateModel().setIsResolved(resolved);
            if (msgCallBack != null) {
                msgCallBack.doEvaluate(evaluateFlag, message);
            }
        }
        sobot_ratingBar.setRating(5);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (checkedId == sobot_btn_ok_robot.getId()) {
            sobotEvaluateModel.setIsResolved(0);
            sobot_btn_ok_robot.setChecked(true);
            sobot_btn_no_robot.setChecked(false);
            sobot_btn_ok_robot.setSelected(true);
            sobot_btn_no_robot.setSelected(false);
        }
        if (checkedId == sobot_btn_no_robot.getId()) {
            sobotEvaluateModel.setIsResolved(1);
            sobot_btn_ok_robot.setChecked(false);
            sobot_btn_no_robot.setChecked(true);
            sobot_btn_ok_robot.setSelected(false);
            sobot_btn_no_robot.setSelected(true);
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (sobotEvaluateModel != null && 0 == sobotEvaluateModel.getEvaluateStatus() && rating > 0) {
            //未评价时进行评价
            int score = (int) Math.ceil(sobot_ratingBar.getRating());
            sobotEvaluateModel.setScore(score);
            if (score == 5) {
              //  doEvaluate(true);
            } else {
                doEvaluate(false);
            }
        }
    }
}