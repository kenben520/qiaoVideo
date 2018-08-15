package com.tencent.qcloud.qiaoqiao.videojoiner;

import com.tencent.qcloud.qiaoqiao.videochoose.TCVideoFileInfo;

/**
 * Created by liyuejiao on 2018/1/11.
 */

public class ItemView {
    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public interface OnAddListener {
        void onAdd(TCVideoFileInfo fileInfo);
    }
}
