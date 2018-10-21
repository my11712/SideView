package com.my11712.sideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created  on 2018/10/20.
 */

public class SideView extends RelativeLayout {

    private Listener listener;
    /**
     * 上边距
     */
    private float marginTop = 0;

    private int textPadding = 10;

    private int textSize = 30;

    private float lineHeight;

    private int dialogWidth = 200;

    private int dialogHeight = 200;
    /**
     * 侧边栏触摸时，阴影的宽度,也是触摸的范围
     */
    private int sideBarWidth = 80;
    /**
     * 默认的文字颜色
     */
    private int textColor = 0xF0666666;
    /**
     * 触摸到的文字颜色
     */
    private int textTouchColor = 0xFF333333;
    /**
     * 中间提示框文字的颜色
     */
    private int dialogTextColor = 0xFF333333;
    /**
     * 弹出框背景颜色
     */
    private int dialogBackground;
    /**
     * sideview背景颜色
     */
    private int background;

    public static String[] A_Z = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};

    private String currentChar = null;

    public SideView(Context context) {
        super(context);
    }

    public SideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 根据xml布局获取相关属性
     *
     * @param attrs
     */

    private void init(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.sideView);
        textColor = a.getColor(R.styleable.sideView_text_color, getResources().getColor(R.color.side_view_text_color));
        dialogTextColor = a.getColor(R.styleable.sideView_text_color, getResources().getColor(R.color.side_view_dialog_text_color));
        dialogWidth = a.getDimensionPixelSize(R.styleable.sideView_dialog_width, 200);
        dialogHeight = a.getDimensionPixelSize(R.styleable.sideView_dialog_height, 200);
        background = a.getColor(R.styleable.sideView_backgroundColor, getResources().getColor(R.color.side_view_dialog_background));
        dialogBackground = a.getColor(R.styleable.sideView_dialog_background, getResources().getColor(R.color.dialog_background));
        a.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        float xPoint = event.getX();
        float barStart = getWidth() - sideBarWidth;
        if (event.getAction() == MotionEvent.ACTION_DOWN||event.getAction() == MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_UP) {
            if (xPoint > barStart) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float currentY = event.getY();
        float currentX = event.getX();
        //触摸点不是右边的字母栏目，不响应触摸事件
        if (currentX < (getWidth() - sideBarWidth) && event.getAction() != MotionEvent.ACTION_UP)
            return false;
        String tempChar = null;
        for (int i = 0; i < A_Z.length; i++) {
            float y0 = marginTop + lineHeight * (i);
            float y1 = marginTop + lineHeight * (i + 1);
            if (currentY >= y0 && currentY < y1) {
                tempChar = A_Z[i];
            } else if (currentY < marginTop) {
                //位置是在第一个字符上面
                tempChar = A_Z[0];
            } else if (currentY > marginTop + lineHeight * A_Z.length) {
                //位置是在最下面的的下面
                tempChar = A_Z[A_Z.length - 1];
            }
        }
        //已经选中了字母，也不进行拦截事件

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentChar = tempChar;
                break;
            case MotionEvent.ACTION_MOVE:
                //选中的没有改变时，不回调
                if (tempChar.equals(currentChar)) {
                    invalidate();
                    return true;
                } else {
                    currentChar = tempChar;
                    break;
                }
            case MotionEvent.ACTION_UP:
                currentChar = null;
                break;
        }
        if (listener != null && currentChar != null) listener.onSelect(currentChar);
        invalidate();
        return true;
    }

    /**
     * 计算右侧导航栏的上边距
     *
     * @param textHeight 行高
     * @return 上边距
     */
    private float getMarginTop(float textHeight) {
        return (getHeight() - textHeight * A_Z.length) / 2;
    }

    /**
     * 文字的行高
     *
     * @param textSize 文字大小
     * @param padding  内边距
     * @return 行高
     */
    private float getTextHeight(int textSize, int padding) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        Rect rect = new Rect();
        paint.getTextBounds(A_Z[0], 0, A_Z[A_Z.length - 1].length(), rect);
        //  int textHeight=rect.height();//这里是文字的实际高度
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        float textHeight = bottom - top + padding * 2;//文字的高度+内边距
        paint.reset();
        return textHeight;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (marginTop == 0) {
            lineHeight = getTextHeight(textSize, textPadding);
            marginTop = getMarginTop(lineHeight);
        }
        //绘制时，先绘制背景，再绘制文字，再绘制中间的字母弹窗口

        if (currentChar != null) {
            drawBackGround(canvas);
        }
        for (int i = 0; i < A_Z.length; i++) {

          /*  Paint paint0 = new Paint();
            paint0.setStyle(Paint.Style.STROKE);
            Rect rect1 = new Rect();
            rect1.left=getWidth()-sideBarWidth;
            rect1.top= (int) (marginTop+lineHeight*i);
            rect1.right= (int) getWidth();
            rect1.bottom=(int) (marginTop+lineHeight*(i+1));
            paint0.setColor(Color.parseColor("#f0ffff00"));
            canvas.drawRect(rect1,paint0);
            paint0.reset();
            if(i==0){
                Log.e("第一个个格子","x1："+ rect1.left+"，y1："+  rect1.top+",x2："+ rect1.right+",y2:"+ rect1.bottom);
            }*/

            Paint paint = new Paint();
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            Rect rect = new Rect();
            paint.getTextBounds(A_Z[i], 0, A_Z[i].length(), rect);
            paint.setTypeface(Typeface.DEFAULT_BOLD);  //设置字体
            paint.setAntiAlias(true);  //设置抗锯齿
            if (A_Z[i].equals(currentChar)) {
                //选中的文字变色
                paint.setColor(textTouchColor);
            } else {
                paint.setColor(textColor);
            }

            float textHeight = rect.height();//文字的高度
            float xPoint = getWidth() - sideBarWidth / 2;
            //drawText()的参数x，y不是左上角，二是英文的字母的基准线，所以文字的y应该是格子的1/2 再向下移动半个字的高度
            float yPoint = marginTop + lineHeight * (i) + (lineHeight + textHeight) / 2;
            canvas.drawText(A_Z[i], xPoint, yPoint, paint);
            paint.reset();// 重置画笔
        }


        if (currentChar != null) {

            //画弹出文字
            drawReact(canvas, currentChar);
        }

    }

    private void drawBackGround(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(background);
        RectF r2 = new RectF();
        r2.left = getWidth() - sideBarWidth;
        r2.right = getWidth();
        r2.top = 0;
        r2.bottom = getHeight();
        canvas.drawRect(r2, paint);
        paint.reset();
    }

    private void drawReact(Canvas canvas, String text) {
        Paint paint = new Paint();
        paint.setColor(dialogBackground);
        RectF r2 = new RectF();

        r2.left = (getWidth() - dialogWidth) / 2;
        r2.right = (getWidth() + dialogWidth) / 2;
        r2.top = (getHeight() - dialogHeight) / 2;
        r2.bottom = (getHeight() + dialogHeight) / 2;
        canvas.drawRoundRect(r2, 10, 10, paint);
        paint.setTextSize(40);
        paint.setColor(dialogTextColor);
        float textWidth = paint.measureText(text);
        float textHeight = paint.measureText(text);
        canvas.drawText(text, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2, paint);
        paint.reset();// 重置画笔
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSelect(String str);
    }
}
