package viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 16-6-3.
 */
public class MyViewGroup extends ViewGroup {

    private List<Integer> lineMaxHeight;//每行的最大高度
    private List<List<View>> mAllviews;
    public MyViewGroup(Context context) {
        super(context);
        init();
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //初始化数据源的容器
    public void init(){
        lineMaxHeight = new ArrayList<Integer>();
        mAllviews = new ArrayList<List<View>>();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //清空容器
        mAllviews.clear();
        lineMaxHeight.clear();
        //解析widthMeasureSpec, heightMeasureSpec参数
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //处理自适应的情况
        //获取child的个数
        int cCount = getChildCount();
        //定义每一行的宽度和高度
        int lineWidth=0;
        int lineHeight=0;
        //每行自适应的宽度和高度
        int wrapWidth =0;
        int wrapHeight=0;
        //定义存放每行视图的容器
        List<View> mList = new ArrayList<View>();
        for (int i = 0;i < cCount;i++){
            View child = getChildAt(i);
            //测量child的自适应的情况
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            //获取child的包括外边距的实际宽度和高度
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int cWidth = child.getWidth()+mlp.leftMargin+mlp.rightMargin;
            int cHeight = child.getHeight()+mlp.topMargin+mlp.bottomMargin;
            //进行换行的逻辑处理
            if(lineWidth+cWidth<=widthSize){
                //每行宽度累加
                lineWidth+=cWidth;
                //取最大高度
                lineHeight = Math.max(lineHeight,cHeight);
                mList.add(child);
            }else {
                //换行之前把最大的行高添加到容器中
                lineMaxHeight.add(lineHeight);
                mAllviews.add(mList);
                //换行后重新创建容器
                mList = new ArrayList<View>();
                wrapWidth = Math.max(wrapWidth,lineWidth);
                wrapHeight += lineHeight;//每行的最大高度不断累加
                lineHeight = cHeight;
                lineWidth = cWidth;
                mList.add(child);
            }
            //处理最后的child
            if(i==cCount-1){
                //最后一次也要添加行高
                lineMaxHeight.add(lineHeight);
                mAllviews.add(mList);
                wrapWidth = Math.max(wrapWidth,lineWidth);
                wrapHeight += lineHeight;
            }
            //根据mode情况来确定最后的宽度和高度
            int lastWidth = widthMode==MeasureSpec.EXACTLY?widthSize:wrapWidth;
            int lastHeight = heightMode==MeasureSpec.EXACTLY?heightSize:wrapHeight;
            //重新设置
            setMeasuredDimension(lastWidth,lastHeight);
        }
    }

    //进行视图的摆放
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //根据容器mAllViews的大小进行摆放
        int count = mAllviews.size();
        int left = 0;
        int right = 0;
        int top = 0;
        int top2 = 0;
        int bottom = 0;
        for (int i = 0;i < count;i++){
            List<View> views = mAllviews.get(i);
            for(int j = 0;j < views.size();j++) {
                View child = views.get(j);
                MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
                left += mlp.leftMargin;
                top = mlp.topMargin;
                right = left + child.getMeasuredWidth();
                bottom = top+top2 + child.getMeasuredHeight();
                child.layout(left, top+top2, right, bottom);
                left += child.getMeasuredWidth() + mlp.rightMargin;
            }
            //换行的高度处理
            top2 += lineMaxHeight.get(i);
            //left要还原
            left = 0;
        }
    }
}
