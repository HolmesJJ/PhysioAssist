package sg.com.nyp.a164936j.physioAssist.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import sg.com.nyp.a164936j.physioAssist.R;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class CustomCircleLoading extends View {

    private static final int OUTER_LAYOUT_CIRCLE_COLOR = Color.parseColor("#FFFFFF");
    private static final int OUTER_LAYOUT_CIRCLE_STROKE_WIDTH = 2;
    private static final int TRIANGLE_COLOR = Color.parseColor("#FFFFFF");
    private static final int RADIUS = 30;

    private int outerCircleColor = OUTER_LAYOUT_CIRCLE_COLOR;
    private int outerCircleStrokeWidth = dp2px(OUTER_LAYOUT_CIRCLE_STROKE_WIDTH);
    private int mTriangleColor = TRIANGLE_COLOR;
    private int mRadius = dp2px(RADIUS);

    private Paint outerCirclePaint;
    private Paint mArcPaint;
    private Paint mTrianglePaint;
    private float mArcAngle;
    private float mDistance;
    private Path mPath;
    private float mTriangleLength;//三角形边长

    private Status mStatus = Status.End;

    public CustomCircleLoading(Context context) {
        this(context,null);
    }

    public CustomCircleLoading(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomCircleLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCircleLoading);
        int indexCount = array.getIndexCount();
        for(int i = 0;i < indexCount;i++){
            int attr = array.getIndex(i);
            switch (attr){
                case R.styleable.CustomCircleLoading_outer_layout_circle_color:
                    outerCircleColor = array.getColor(attr, OUTER_LAYOUT_CIRCLE_COLOR);
                    break;
                case R.styleable.CustomCircleLoading_outer_layout_circle_stroke_width:
                    outerCircleStrokeWidth = (int) array.getDimension(attr, outerCircleStrokeWidth);
                    break;
                case R.styleable.CustomCircleLoading_triangle_color:
                    mTriangleColor = array.getColor(attr, mTriangleColor);
                    break;
                case R.styleable.CustomCircleLoading_customCircleLoadingRadius:
                    mRadius = (int) array.getDimension(attr, mRadius);
                    break;
            }
        }
        //回收
        array.recycle();

        mDistance = (float) (mRadius * 0.06);
        mTriangleLength = mRadius;
        //设置画笔
        setPaint();
        //画三角形
        mPath = new Path();
        float mFirstPointX = (float) (mRadius - Math.sqrt(3.0) / 4 * mRadius);//勾股定理
        float mNiceFirstPointX = (float) (mFirstPointX + mFirstPointX * 0.2);
        float mFirstPointY = mRadius - mTriangleLength / 2;
        mPath.moveTo(mNiceFirstPointX,mFirstPointY);
        mPath.lineTo(mNiceFirstPointX, mRadius+mTriangleLength / 2);
        mPath.lineTo((float) (mNiceFirstPointX+Math.sqrt(3.0) / 2 * mRadius), mRadius);
        mPath.lineTo(mNiceFirstPointX, mFirstPointY);
    }

    private void setPaint() {
        outerCirclePaint = new Paint();
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setDither(true);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setColor(outerCircleColor);
        outerCirclePaint.setStrokeWidth(outerCircleStrokeWidth);
        outerCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setColor(outerCircleColor);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mTrianglePaint = new Paint();
        mTrianglePaint.setAntiAlias(true);
        mTrianglePaint.setDither(true);
        mTrianglePaint.setColor(outerCircleColor);
        mTrianglePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width;
        int height;
        if(widthMode != MeasureSpec.EXACTLY){
            width = getPaddingLeft() + mRadius*2 + outerCircleStrokeWidth*2 + getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        if(heightMode != MeasureSpec.EXACTLY){
            height = getPaddingTop() + mRadius*2 + outerCircleStrokeWidth*2 + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(),getPaddingTop());
        //画圆
        canvas.drawCircle(mRadius,mRadius,mRadius,outerCirclePaint);

        if(mStatus == Status.End){
            //画三角形
            canvas.drawPath(mPath, mTrianglePaint);
        }else{//正在进行状态
            //画扇形
            canvas.drawArc(new RectF(0 + mDistance,0 + mDistance,mRadius*2 - mDistance,mRadius*2 - mDistance), -90, 360*mArcAngle, true, mArcPaint);
        }

        canvas.restore();
    }

    public void animatorAngle(float value){
        mArcAngle = value;
        //刷新View
        invalidate();
    }

    public void init(){
        //刷新View
        invalidate();
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    public enum Status{
        End,
        Starting
    }
    public Status getStatus(){
        return mStatus;
    }

    public void setStatus(Status status){
        this.mStatus = status;
    }
}