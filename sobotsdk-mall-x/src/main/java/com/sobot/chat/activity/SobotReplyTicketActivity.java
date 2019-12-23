package com.sobot.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sobot.chat.activity.base.SobotBaseActivity;
import com.sobot.chat.adapter.SobotPicListAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiUploadAppFileModelResult;
import com.sobot.chat.core.http.callback.StringResultCallBack;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 留言回复界面
 */
public class SobotReplyTicketActivity extends SobotBaseActivity implements View.OnClickListener {

    private String mUid = "";
    private String mCompanyId = "";
    private SobotUserTicketInfo mTicketInfo;

    private TextView sobotTvTitle;
    private LinearLayout sobotNegativeButton;
    private EditText sobotReplyEdit;
    private GridView sobotReplyMsgPic;
    private Button sobotBtnSubmit;

    private ArrayList<ZhiChiUploadAppFileModelResult> pic_list = new ArrayList<>();
    private SobotPicListAdapter adapter;
    private SobotSelectPicDialog menuWindow;

    @Override
    public void onClick(View v) {

        if (v == sobotBtnSubmit) {//提交
            submitPost(sobotReplyEdit.getText().toString(), getFileStr());

        }
    }

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_layout_dialog_reply");
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(SobotTicketDetailActivity.INTENT_KEY_UID);
            mCompanyId = getIntent().getStringExtra(SobotTicketDetailActivity.INTENT_KEY_COMPANYID);
            mTicketInfo = (SobotUserTicketInfo) getIntent().getSerializableExtra(SobotTicketDetailActivity.INTENT_KEY_TICKET_INFO);
        }
    }

    @Override
    protected void initView() {


        showLeftMenu(getResDrawableId("sobot_btn_back_selector"), getResString("sobot_back"), true);
        setTitle(getResString("sobot_reply"));

        sobotReplyEdit = (EditText) findViewById(getResId("sobot_reply_edit"));
        sobotReplyMsgPic = (GridView) findViewById(getResId("sobot_reply_msg_pic"));
        sobotBtnSubmit = (Button) findViewById(getResId("sobot_btn_submit"));

        sobotBtnSubmit.setOnClickListener(this);
        adapter = new SobotPicListAdapter(this, pic_list);
        sobotReplyMsgPic.setAdapter(adapter);
        initPicListView();
    }

    /**
     * 初始化图片选择的控件
     */
    private void initPicListView() {
        sobotReplyMsgPic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardUtil.hideKeyboard(view);
                if (pic_list.get(position).getViewState() == 0) {
                    menuWindow = new SobotSelectPicDialog(SobotReplyTicketActivity.this, itemsOnClick);
                    menuWindow.show();
                } else {
                    LogUtils.i("当前选择图片位置：" + position);
                    Intent intent = new Intent(SobotReplyTicketActivity.this, SobotPhotoListActivity.class);
                    intent.putExtra(ZhiChiConstant.SOBOT_KEYTYPE_PIC_LIST, adapter.getPicList());
                    intent.putExtra(ZhiChiConstant.SOBOT_KEYTYPE_PIC_LIST_CURRENT_ITEM, position);
                    startActivityForResult(intent, ZhiChiConstant.SOBOT_KEYTYPE_DELETE_FILE_SUCCESS);
                }
            }
        });

        adapter.restDataView();
    }

    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == getResId("btn_take_photo")) {
                LogUtils.i("拍照");
                selectPicFromCameraBySys();
            }
            if (v.getId() == getResId("btn_pick_photo")) {
                LogUtils.i("选择照片");
                selectPicFromLocal();
            }
            if (v.getId() == getResId("btn_pick_vedio")) {
                LogUtils.i("选择视频");
                selectVedioFromLocal();
            }
        }
    };

    /**
     * 调用系统相机拍照
     */
    public void selectPicFromCameraBySys() {
        if (!CommonUtils.isExitsSdcard()) {
            Toast.makeText(this, getResString("sobot_sdcard_does_not_exist"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                if (checkStorageAndCameraPermission() && isCameraCanUse()) {
                    cameraFile = ChatUtils.openCamera(SobotReplyTicketActivity.this, null);
                }
            }
        };
        if (!checkStorageAndCameraPermission()) {
            return;
        }
        cameraFile = ChatUtils.openCamera(SobotReplyTicketActivity.this, null);
    }


    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                if (checkStoragePermission()) {
                    ChatUtils.openSelectPic(SobotReplyTicketActivity.this, null);
                }
            }
        };
        if (!checkStoragePermission()) {
            return;
        }
        ChatUtils.openSelectPic(SobotReplyTicketActivity.this, null);
    }

    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                if (checkStoragePermission()) {
                    ChatUtils.openSelectVedio(SobotReplyTicketActivity.this, null);
                }
            }
        };
        if (!checkStoragePermission()) {
            return;
        }
        ChatUtils.openSelectVedio(SobotReplyTicketActivity.this, null);
    }

    @Override
    protected void initData() {

    }


    public String getFileStr() {
        String tmpStr = "";
        ArrayList<ZhiChiUploadAppFileModelResult> tmpList = adapter.getPicList();
        for (int i = 0; i < tmpList.size(); i++) {
            tmpStr += tmpList.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }



    public void submitPost(String content, String fileStr) {
        if (TextUtils.isEmpty(content)) {
            CustomToast.makeText(SobotReplyTicketActivity.this, ResourceUtils.getResString(SobotReplyTicketActivity.this, "sobot_please_reply_input"), Toast.LENGTH_LONG).show();
            return;
        }
        zhiChiApi.replyTicketContent(this, mUid, mTicketInfo.getTicketId(), content, fileStr, mCompanyId, new StringResultCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                LogUtils.e(s);
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(SobotReplyTicketActivity.this, des);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    String path = ImageUtils.getPath(this, selectedImage);
                    if (MediaFileUtils.isVideoFileType(path)) {
                        MediaPlayer mp = new MediaPlayer();
                        try {
                            mp.setDataSource(this, selectedImage);
                            mp.prepare();
                            int videoTime = mp.getDuration();
                            if (videoTime / 1000 > 15) {
                                ToastUtil.showToast(this, getResString("sobot_upload_vodie_length"));
                                return;
                            }
                            SobotDialogUtils.startProgressDialog(this);
                            sendFileListener.onSuccess(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        SobotDialogUtils.startProgressDialog(this);
                        ChatUtils.sendPicByUriPost(this, selectedImage, sendFileListener);
                    }
                } else {
                    showHint(getResString("sobot_did_not_get_picture_path"));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(this);
                    ChatUtils.sendPicByFilePath(this, cameraFile.getAbsolutePath(), sendFileListener);
                } else {
                    showHint(getResString("sobot_pic_select_again"));
                }
            }
        }
    }

    public void showHint(String content) {
        CustomToast.makeText(this, content, 1000).show();
    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotReplyTicketActivity.this, mCompanyId, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {

                    SobotDialogUtils.stopProgressDialog(SobotReplyTicketActivity.this);
                    if (zhiChiMessage.getData() != null) {
                        ZhiChiUploadAppFileModelResult item = new ZhiChiUploadAppFileModelResult();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        item.setViewState(1);
                        adapter.addData(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(SobotReplyTicketActivity.this);
                    showHint(des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(SobotReplyTicketActivity.this);
        }
    };
}
