package com.example.liangxiao.coordinatorlayouttest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.ViewCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.SystemClock;
import master.flame.danmaku.ui.widget.DanmakuView;

/**
 * Created by liangxiao on 2018/5/23.
 */

public class DanmuBuilder {
    private DanmakuContext mContext;

    public DanmakuContext build(final Context context,final DanmakuView danmakuView){

        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mContext = DanmakuContext.create();

        final int mIconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, context.getResources().getDisplayMetrics());
        mContext.setDanmakuBold(true);
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
                .setCacheStuffer(new ViewCacheStuffer<MyViewHolder>() {

                    @Override
                    public MyViewHolder onCreateViewHolder(int viewType) {
                        Log.e("DFM", "onCreateViewHolder:" + viewType);
                        return new MyViewHolder(View.inflate(context.getApplicationContext(), R.layout.layout_view_cache, null));
                    }

                    @Override
                    public void onBindViewHolder(int viewType, MyViewHolder viewHolder, BaseDanmaku danmaku, AndroidDisplayer.DisplayerConfig displayerConfig, TextPaint paint) {
                        if (paint != null)
                            viewHolder.mText.getPaint().set(paint);
                        viewHolder.mText.setText(danmaku.text);
                        viewHolder.mText.setTextColor(danmaku.textColor);
                        viewHolder.mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, danmaku.textSize);
                        Bitmap bitmap = null;
                        MyImageWare imageWare = (MyImageWare) danmaku.tag;
                        if (imageWare != null) {
                            bitmap = imageWare.bitmap;
                            if (danmaku.text.toString().contains("textview")) {
                                Log.e("DFM", "onBindViewHolder======> bitmap:" + (bitmap == null) + "  " + danmaku.tag + "url:" + imageWare.getImageUri());
                            }
                        }
                        if (bitmap != null) {
                            viewHolder.mIcon.setImageBitmap(bitmap);
                            if (danmaku.text.toString().contains("textview")) {
                                Log.e("DFM", "onBindViewHolder======>" + danmaku.tag + "url:" + imageWare.getImageUri());
                            }
                        } else {
                            //viewHolder.mIcon.setImageResource(R.mipmap.ic_launcher);
                        }
                    }

                    @Override
                    public void releaseResource(BaseDanmaku danmaku) {
                        MyImageWare imageWare = (MyImageWare) danmaku.tag;
                        if (imageWare != null) {
                            ImageLoader.getInstance().cancelDisplayTask(imageWare);
                        }
                        danmaku.setTag(null);
                        Log.e("DFM", "releaseResource url:" + danmaku.text);
                    }


                    String[] avatars = { "http://i0.hdslb.com/bfs/face/e13fcb94342c325debb2d3a1d9e503ac4f083514.jpg@45w_45h.webp",
                            "http://i0.hdslb.com/bfs/bangumi/2558e1341d2e934a7e06bb7d92551fef5c82c172.jpg@72w_72h.webp", "http://i0.hdslb.com/bfs/face/128edefeef7ce9cfc443a2489d8a1c7d44d88b80.jpg@72w_72h.webp"};
                    @Override
                    public void prepare(BaseDanmaku danmaku, boolean fromWorkerThread) {
                        if (danmaku.isTimeOut()) {
                            return;
                        }
                        MyImageWare imageWare = (MyImageWare) danmaku.tag;
                        if (imageWare == null) {
                            String avatar = avatars[danmaku.index % avatars.length];
                            imageWare = new MyImageWare(avatar, danmaku, mIconWidth, mIconWidth, danmakuView);
                            danmaku.setTag(imageWare);
                        }
                        if (danmaku.text.toString().contains("textview")) {
                            Log.e("DFM", "onAsyncLoadResource======>" + danmaku.tag + "url:" + imageWare.getImageUri());
                        }
                        ImageLoader.getInstance().displayImage(imageWare.getImageUri(), imageWare);
                    }

                }, null) // 图文混排使用SpannedCacheStuffer
//        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40);
        if (danmakuView != null) {
            BaseDanmakuParser mParser = createParser(context.getResources().openRawResource(R.raw.comments));
            danmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                @Override
                public void prepared() {
                    danmakuView.start();
                }
            });
            danmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {

                @Override
                public boolean onDanmakuClick(IDanmakus danmakus) {
                    Log.d("DFM", "onDanmakuClick: danmakus size:" + danmakus.size());
                    BaseDanmaku latest = danmakus.last();
                    if (null != latest) {
                        Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onDanmakuLongClick(IDanmakus danmakus) {
                    return false;
                }

                @Override
                public boolean onViewClick(IDanmakuView view) {
//                    mMediaController.setVisibility(View.VISIBLE);
                    return false;
                }
            });
            danmakuView.prepare(mParser, mContext);
            danmakuView.showFPS(true);
            danmakuView.enableDanmakuDrawingCache(true);
        }
            return mContext;
    }
    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }

    public class MyViewHolder extends ViewCacheStuffer.ViewHolder {

        private final ImageView mIcon;
        private final TextView mText;

        public MyViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mText = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public static class MyImageWare extends NonViewAware {

        private long start;
        private int id;
        private WeakReference<IDanmakuView> danmakuViewRef;
        private BaseDanmaku danmaku;
        private Bitmap bitmap;

        public MyImageWare(String imageUri, BaseDanmaku danmaku, int width, int height, IDanmakuView danmakuView) {
            this(imageUri, new ImageSize(width, height), ViewScaleType.FIT_INSIDE);
            if (danmaku == null) {
                throw new IllegalArgumentException("danmaku may not be null");
            }
            this.danmaku = danmaku;
            this.id = danmaku.hashCode();
            this.danmakuViewRef = new WeakReference<>(danmakuView);
            this.start = SystemClock.uptimeMillis();
        }

        @Override
        public int getId() {
            return this.id;
        }

        public String getImageUri() {
            return this.imageUri;
        }

        private MyImageWare(ImageSize imageSize, ViewScaleType scaleType) {
            super(imageSize, scaleType);
        }

        private MyImageWare(String imageUri, ImageSize imageSize, ViewScaleType scaleType) {
            super(imageUri, imageSize, scaleType);
        }

        @Override
        public boolean setImageDrawable(Drawable drawable) {
            return super.setImageDrawable(drawable);
        }

        @Override
        public boolean setImageBitmap(Bitmap bitmap) {
//            if (this.danmaku.isTimeOut() || this.danmaku.isFiltered()) {
//                return true;
//            }
            if (this.danmaku.text.toString().contains("textview")) {
                Log.e("DFM", (SystemClock.uptimeMillis() - this.start) + "ms=====> inside" + danmaku.tag + ":" + danmaku.getActualTime() + ",url: bitmap" + (bitmap == null));
            }
            this.bitmap = bitmap;
            IDanmakuView danmakuView = danmakuViewRef.get();
            if (danmakuView != null) {
                danmakuView.invalidateDanmaku(danmaku, true);
            }
            return true;
        }
    }


}
