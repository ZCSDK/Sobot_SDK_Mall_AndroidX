package com.sobot.chat.widget.gif;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sobot.chat.R;
import com.sobot.chat.utils.ScreenUtils;

import java.io.FileInputStream;
import java.io.InputStream;

public class GifView2 extends View {

    private final int DEFAULT_MOVIE_VIEW_DURATION = 1000;
    private int movieMovieResourceId = 0;
    private Movie movie;
    private long movieStart = 0;
    private int currentAnimationTime;

    private float movieLeft;
    private float movieTop;
    private float movieScale;

    private int movieMeasuredMovieWidth;
    private int movieMeasuredMovieHeight;

    volatile boolean isPaused;

    private boolean isVisible = true;

    int gifResource;
    boolean isPlaying;


    public GifView2(Context context) {
        super(context);
    }

    public GifView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GifView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GifView2, defStyleAttr, android.R.style.Widget);

        movieMovieResourceId = array.getResourceId(R.styleable.GifView2_gif, -1);
        array.recycle();

        if (movieMovieResourceId != -1) {
            movie = movie.decodeStream(getResources().openRawResource(movieMovieResourceId));
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        if (movie != null) {
            movieMeasuredMovieWidth = ScreenUtils.formatDipToPx(getContext(),movie.width());
            movieMeasuredMovieHeight = ScreenUtils.formatDipToPx(getContext(), movie.height());

            if (movieMeasuredMovieWidth==0||movieMeasuredMovieHeight==0){
                setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
                return;
            }

            /*
             * Calculate horizontal scaling
             */
            float scaleW = 1f;


            int[] screen = ScreenUtils
                    .getScreenWH(getContext());
            Log.e( "onMeasure: ","\n"+movieMeasuredMovieWidth+"\t"+movieMeasuredMovieHeight+"\n"+screen[0]+"\t"+screen[1] );

            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

            if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
                int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (movieMeasuredMovieWidth > screen[0]) {
                    scaleW = movieMeasuredMovieWidth * 1.0f / screen[0];
                    movieMeasuredMovieHeight = (int) (movieMeasuredMovieHeight / scaleW);
                    movieMeasuredMovieWidth=screen[0];
                }
            }
            /*
             * calculate vertical scaling
             */
            float scaleH = 1f;
            int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

            if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
                int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (movieMeasuredMovieHeight > screen[1]) {
                    scaleH = movieMeasuredMovieHeight * 1.0f / screen[1];
                    movieMeasuredMovieWidth = (int) (movieMeasuredMovieWidth/ scaleH);
                    movieMeasuredMovieHeight=screen[1];
                }
            }
            /*
             * calculate overall scale
             */
            float scale = getResources().getDisplayMetrics().density;
            movieScale = scale/(scaleW*scaleH);
//            movieMeasuredMovieWidth = (int) (movieMeasuredMovieWidth * movieScale);
//            movieMeasuredMovieHeight = (int) (movieMeasuredMovieHeight * movieScale);
            Log.e( "onMeasure: ","\n"+movieMeasuredMovieWidth+"\t"+movieMeasuredMovieHeight+"\n"+movieScale);
            setMeasuredDimension(movieMeasuredMovieWidth, movieMeasuredMovieHeight);
        } else {
            /*
             * No movie set, just set minimum available size.
             */
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*
         * Calculate movieLeft / movieTop for drawing in center
         */
        movieLeft = (getWidth() - movieMeasuredMovieWidth) / 2.0f;
        movieTop = (getHeight() - movieMeasuredMovieHeight) / 2.0f;
        isVisible = getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (movie != null) {
            if (!isPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }
        }
    }

    private void invalidateView() {
        if (isVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }


    /**
     * Calculate current animation time
     */
    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();

        if (movieStart == 0L) {
            movieStart = now;
        }

        long duration = movie.duration();

        if (duration == 0) {
            duration = DEFAULT_MOVIE_VIEW_DURATION;
        }

        currentAnimationTime = (int) ((now - movieStart) % duration);
    }

    /**
     * Draw current GIF frame
     */
    private void drawMovieFrame(Canvas canvas) {
        movie.setTime(currentAnimationTime);
        canvas.save();
        canvas.scale(movieScale, movieScale);
        movie.draw(canvas, movieLeft / movieScale, movieTop / movieScale);
        canvas.restore();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        isVisible = screenState == View.SCREEN_STATE_ON;
        invalidateView();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        isVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        isVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    public void play() {
        if (this.isPaused) {
            this.isPaused = false;
            /**
             * Calculate new movie start time, so that it resumes from the same
             * frame.
             */
            movieStart = android.os.SystemClock.uptimeMillis() - currentAnimationTime;
            invalidate();
        }
    }

    public void pause() {
        if (!this.isPaused) {
            this.isPaused = true;
            invalidate();
        }
    }

    public int getMovieMovieResourceId() {
        return movieMovieResourceId;
    }

    /**
     * 设置gif 资源 流
     *
     * @param movieMovieResourceId 没有 填 -1
     * @param inputStream
     */
    public void setMovieMovieResourceId(int movieMovieResourceId, InputStream inputStream) {
        this.movieMovieResourceId = movieMovieResourceId;
        if (movieMovieResourceId != -1) {
            movie = Movie.decodeStream(getResources().openRawResource(movieMovieResourceId));
        } else if (inputStream != null) {
            movie = Movie.decodeStream(inputStream);
        }
        requestLayout();
    }

    public void setGifImage(FileInputStream inputStream) {
        if (inputStream != null) {
            movie = Movie.decodeStream(inputStream);
        }
        requestLayout();
    }
}
