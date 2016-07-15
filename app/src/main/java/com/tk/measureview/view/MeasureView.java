package com.tk.measureview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tk.measureview.R;

import java.text.DecimalFormat;

/**
 * Created by TK on 2016/7/2.
 * 围度测量
 * 默认生成的多边形顶点居上，可选根据offRadius 修正
 */
public class MeasureView extends View {
    private static final String TAG = "MeasureView";
    //默认5层网状
    private static final int DEFAULT_PATH = 5;
    //默认线框颜色
    private static final int DEFAULT_COLOR_LINE = Color.parseColor("#FF1E85d4");
    //默认填充颜色
    private static final int DEFAULT_COLOR_CONTENT = Color.parseColor("#501E85d4");
    //默认大小，定死圆形
    private static final int DEFAULT_SIZE = 320;
    //间距
    private static final int PADDING = 8;
    //线粗细
    private static final int LINE_WIDTH = 3;
    //宽=高
    private int mSize = DEFAULT_SIZE;
    //默认5围
    private int mMeasure = 5;
    //默认5层
    private int mPathSize = DEFAULT_PATH;
    //多边形修正角度
    private float offAngle = 0f;
    //是否画轴心线
    private boolean isLineOn = true;
    //线画笔，填充画笔
    private Paint pathPaint = new Paint();
    private Paint contentPaint = new Paint();
    //色值
    private int lineColor = DEFAULT_COLOR_LINE;
    private int contentColor = DEFAULT_COLOR_CONTENT;
    private Path contentPath = new Path();
    //架构坐标数组
    private float framework[][][][] = null;
    //填充坐标数组
    private float location[][] = null;
    //是否绘制填充
    private boolean isContent;
    private DecimalFormat df = new DecimalFormat("0.00");
    private boolean draw;
    //控件真实半径
    private int radius;

    public MeasureView(Context context) {
        super(context);
        init(null);
    }

    public MeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        Bundle bundle = (Bundle) state;
//        Parcelable superState = bundle.getParcelable("superState");
//        float[][] l = (float[][]) bundle.getSerializable("location");
//        if (l != null) {
//            location = l;
//        }
//        framework = (float[][][][]) bundle.getSerializable("framework");
//        super.onRestoreInstanceState(superState);
//    }
//
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Bundle bundle = new Bundle();
//        Parcelable superState = super.onSaveInstanceState();
//        bundle.putParcelable("superState", superState);
//        if (location != null) {
//            bundle.putSerializable("location", location);
//        }
//        bundle.putSerializable("framework", framework);
//        return bundle;
//    }

    private void init(AttributeSet attrs) {
        //硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MeasureView);
            mSize = array.getDimensionPixelOffset(R.styleable.MeasureView_size, mSize);
            offAngle = array.getFloat(R.styleable.MeasureView_offAngle, offAngle);
            int s = array.getInt(R.styleable.MeasureView_pathSize, mPathSize);
            if (s < 1 || s > 8) {
                Log.e(TAG, "pathSize not standard");
            } else {
                mPathSize = s;
            }
            int m = array.getInt(R.styleable.MeasureView_measureSize, mMeasure);
            if (m < 3 || m > 16) {
                Log.e(TAG, "measureSize not standard");
            } else {
                mMeasure = m;
            }
            isLineOn = array.getBoolean(R.styleable.MeasureView_isLineOn, true);
            lineColor = array.getColor(R.styleable.MeasureView_lineColor, DEFAULT_COLOR_LINE);
            contentColor = array.getColor(R.styleable.MeasureView_contentColor, DEFAULT_COLOR_CONTENT);
        }
        radius = (mSize - (PADDING >> 1)) >> 1;
        initPaint();
        initFramework();
    }

    /**
     * 初始化蛛网架构
     */
    private void initFramework() {
        // 蛛网层数*围度*首、位*X、Y
        framework = new float[mPathSize][mMeasure][2][2];
        location = new float[mMeasure][2];
        for (int a = 0; a < mPathSize; a++) {
            for (int b = 0; b < mMeasure; b++) {
                for (int c = 0; c < 2; c++) {
                    calculXY(a, b, c);
                }
            }
        }
    }

    /**
     * 计算坐标
     *
     * @param a
     * @param b
     * @param c
     */
    private void calculXY(int a, int b, int c) {
        float newR = radius * (mPathSize - a) / mPathSize;
        float angle = 360 / mMeasure;
        float newAngle;
        if (b + c != mMeasure) {
            newAngle = angle * (b + c) + offAngle;
        } else {
            //排除角度的误差
            //// TODO: 2016/7/3   mMeasure=13+时会出现角度误差
            newAngle = 360f + offAngle;
        }
        framework[a][b][c][0] = (float) ((mSize >> 1) + newR * Math.sin(newAngle * Math.PI / 180));
        framework[a][b][c][1] = (float) ((mSize >> 1) - newR * Math.cos(newAngle * Math.PI / 180));
    }

    private void initPaint() {
        contentPaint.setAntiAlias(true);
        contentPaint.setStyle(Paint.Style.FILL);
        contentPaint.setStrokeCap(Paint.Cap.ROUND);
        contentPaint.setStrokeJoin(Paint.Join.ROUND);
        contentPaint.setColor(contentColor);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(LINE_WIDTH);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setColor(lineColor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw = true;
        for (int a = 0; a < mPathSize; a++) {
            for (int b = 0; b < mMeasure; b++) {
                for (int c = 0; c < 2; c++) {
                    canvas.drawLine(framework[a][b][0][0],
                            framework[a][b][0][1],
                            framework[a][b][1][0],
                            framework[a][b][1][1], pathPaint);
                }
            }
        }
        if (isLineOn) {
            for (int b = 0; b < mMeasure; b++) {
                canvas.drawLine(framework[0][b][0][0],
                        framework[0][b][0][1],
                        (mSize >> 1),
                        (mSize >> 1), pathPaint);
            }
        }
        if (isContent) {
            contentPath.reset();
            contentPath.moveTo(location[0][0], location[0][1]);
            for (int i = 1; i < mMeasure; i++) {
                contentPath.lineTo(location[i][0], location[i][1]);
            }
            contentPath.close();
            canvas.drawPath(contentPath, contentPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSize = w;
        radius = (mSize - (PADDING >> 1)) >> 1;
        if (draw) {
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    /**
     * 设置值,百分比
     *
     * @param value
     */
    public void setValue(float[] value) {
        if (value.length != mMeasure) {
            Log.e(TAG, "setValue not standard");
            return;
        }

        for (int i = 0; i < mMeasure; i++) {
            float f = value[i];
            if (f > 1f || f < 0f) {
                Log.e(TAG, "setValue not standard");
                location = null;
                return;
            }
            float angle = 360 / mMeasure;
            float newAngle;
            if (i != mMeasure) {
                newAngle = angle * i + offAngle;
            } else {
                //排除角度的误差
                //// TODO: 2016/7/3   mMeasure=13+时会出现角度误差
                newAngle = 360f + offAngle;
            }
            location[i][0] = (float) ((mSize >> 1) + radius * f * Math.sin(newAngle * Math.PI / 180));
            location[i][1] = (float) ((mSize >> 1) - radius * f * Math.cos(newAngle * Math.PI / 180));
        }
        isContent = true;

        if (draw) {
            invalidate();
        }
    }

    /**
     * 得到填充面积
     *
     * @return
     */
    public float getValue() {
        float total = 0f;
        float sum = 0f;
        for (int i = 0; i < mMeasure; i++) {
            float x1 = framework[0][i % mMeasure][0][0];
            float y1 = framework[0][i % mMeasure][0][1];
            float x2 = framework[0][i % mMeasure][1][0];
            float y2 = framework[0][i % mMeasure][1][1];
            total += calculArea(x1, y1, x2, y2);
        }
        for (int i = 0; i < mMeasure; i++) {
            float x1 = location[i % mMeasure][0];
            float y1 = location[i % mMeasure][1];
            float x2 = location[(i + 1) % mMeasure][0];
            float y2 = location[(i + 1) % mMeasure][1];
            sum += calculArea(x1, y1, x2, y2);
        }
        return sum / total;
    }

    /**
     * 海伦公式计算3点面积
     *
     * @return
     */
    public float calculArea(float x1, float y1, float x2, float y2) {
        float a = (float) Math.sqrt(Math.pow((x1 - (mSize >> 1)), 2) + Math.pow((y1 - (mSize >> 1)), 2));
        float b = (float) Math.sqrt(Math.pow((x2 - (mSize >> 1)), 2) + Math.pow((y2 - (mSize >> 1)), 2));
        float c = (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
        float p = (a + b + c) / 2;
        return (float) Math.sqrt(p * (p - a) * (p - b) * (p - c));
    }

    /**
     * 设置围度3-12
     *
     * @param mMeasure
     */
    public void setmMeasure(int mMeasure) {
        if (mMeasure < 3 || mMeasure > 12) {
            Log.e(TAG, "pathSize not standard");
            return;
        }
        this.mMeasure = mMeasure;
        initFramework();
        if (draw) {
            invalidate();
        }
    }

    /**
     * 设置蛛网层数1-8
     *
     * @param mPathSize
     */
    public void setmPathSize(int mPathSize) {

        if (mPathSize < 1 || mPathSize > 8) {
            Log.e(TAG, "pathSize not standard");
            return;
        }
        this.mPathSize = mPathSize;
        initFramework();
        if (draw) {
            invalidate();
        }
    }

    /**
     * 清理填充
     */
    public void clearContent() {
        contentPath.reset();
        isContent = false;
        postInvalidate();
    }

    public int getmMeasure() {
        return mMeasure;
    }

    public void setLineOn(boolean lineOn) {
        isLineOn = lineOn;
        if (draw) {
            invalidate();
        }
    }
}
