package com.rdapps.flowlayouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowRadioGroup extends RadioGroup {
    private Context context;
    private int usefulWidth; // the space of a line we can use(line's width minus the sum of left and right padding
    private int rowSpacing = 0; // the spacing between lines in flowlayout
    private int numberOfColumns = 0; // number of columns in one line
    List<View> childList = new ArrayList<>();
    List<Integer> lineNumList = new ArrayList<>();

    public FlowRadioGroup(Context context) {
        super(context);
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowRadioGroup);
        rowSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowRadioGroup_rowSpacing, 0);
        numberOfColumns = typedArray.getInt(R.styleable.FlowRadioGroup_numberOfColumns, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int lineUsed = paddingLeft + paddingRight;
        int lineY = paddingTop;
        int lineHeight = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int spaceWidth = 0;
            int spaceHeight = 0;
            ViewGroup.LayoutParams childLp = child.getLayoutParams();
            if (childLp instanceof MarginLayoutParams) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineY);
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaceWidth = mlp.leftMargin + mlp.rightMargin;
                spaceHeight = mlp.topMargin + mlp.bottomMargin;
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }

            if (numberOfColumns > 0 && child.getMeasuredWidth() == 0) {
                child.getLayoutParams().width = widthSize / numberOfColumns - spaceWidth;
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            spaceWidth += childWidth;
            spaceHeight += childHeight;

            if (lineUsed + spaceWidth > widthSize) {
                //approach the limit of width and move to next line
                lineY += lineHeight + rowSpacing;
                lineUsed = paddingLeft + paddingRight;
                lineHeight = 0;
            }
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
        }
        setMeasuredDimension(
                widthSize,
                heightMode == MeasureSpec.EXACTLY ? heightSize : lineY + lineHeight + paddingBottom
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();

        int lineX = paddingLeft;
        int lineY = paddingTop;
        int lineWidth = r - l;
        usefulWidth = lineWidth - paddingLeft - paddingRight;
        int lineUsed = paddingLeft + paddingRight;
        int lineHeight = 0;
        int lineNum = 0;

        lineNumList.clear();
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int spaceWidth = 0;
            int spaceHeight = 0;
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = 0;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            ViewGroup.LayoutParams childLp = child.getLayoutParams();
            if (childLp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaceWidth = mlp.leftMargin + mlp.rightMargin;
                spaceHeight = mlp.topMargin + mlp.bottomMargin;
                left = lineX + mlp.leftMargin;
                top = lineY + mlp.topMargin;
                right = lineX + mlp.leftMargin + childWidth;
                bottom = lineY + mlp.topMargin + childHeight;
            } else {
                left = lineX;
                top = lineY;
                right = lineX + childWidth;
                bottom = lineY + childHeight;
            }
            spaceWidth += childWidth;
            spaceHeight += childHeight;

            if (lineUsed + spaceWidth > lineWidth) {
                //approach the limit of width and move to next line
                lineNumList.add(lineNum);
                lineY += lineHeight + rowSpacing;
                lineUsed = paddingLeft + paddingRight;
                lineX = paddingLeft;
                lineHeight = 0;
                lineNum = 0;
                if (childLp instanceof MarginLayoutParams) {
                    MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                    left = lineX + mlp.leftMargin;
                    top = lineY + mlp.topMargin;
                    right = lineX + mlp.leftMargin + childWidth;
                    bottom = lineY + mlp.topMargin + childHeight;
                } else {
                    left = lineX;
                    top = lineY;
                    right = lineX + childWidth;
                    bottom = lineY + childHeight;
                }
            }
            child.layout(left, top, right, bottom);
            lineNum++;
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
            lineX += spaceWidth;
        }
        // add the num of last line
        lineNumList.add(lineNum);
    }

    /**
     * resort child elements to use lines as few as possible
     */
    public void relayoutToCompress() {
        post(new Runnable() {
            @Override
            public void run() {
                compress();
            }
        });
    }

    private void compress() {
        int childCount = this.getChildCount();
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return;
        }
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof FlowLayout.BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            count++;
        }
        View[] childs = new View[count];
        int[] spaces = new int[count];
        int n = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof FlowLayout.BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            childs[n] = v;
            ViewGroup.LayoutParams childLp = v.getLayoutParams();
            int childWidth = v.getMeasuredWidth();
            if (childLp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaces[n] = mlp.leftMargin + childWidth + mlp.rightMargin;
            } else {
                spaces[n] = childWidth;
            }
            n++;
        }
        int[] compressSpaces = new int[count];
        for (int i = 0; i < count; i++) {
            compressSpaces[i] = spaces[i] > usefulWidth ? usefulWidth : spaces[i];
        }
        sortToCompress(childs, compressSpaces);
        this.removeAllViews();
        for (View v : childList) {
            this.addView(v);
        }
        childList.clear();
    }

    private void sortToCompress(View[] childs, int[] spaces) {
        int childCount = childs.length;
        int[][] table = new int[childCount + 1][usefulWidth + 1];
        for (int i = 0; i < childCount + 1; i++) {
            for (int j = 0; j < usefulWidth; j++) {
                table[i][j] = 0;
            }
        }
        boolean[] flag = new boolean[childCount];
        for (int i = 0; i < childCount; i++) {
            flag[i] = false;
        }
        for (int i = 1; i <= childCount; i++) {
            for (int j = spaces[i - 1]; j <= usefulWidth; j++) {
                table[i][j] = (table[i - 1][j] > table[i - 1][j - spaces[i - 1]] + spaces[i - 1]) ? table[i - 1][j] : table[i - 1][j - spaces[i - 1]] + spaces[i - 1];
            }
        }
        int v = usefulWidth;
        for (int i = childCount; i > 0 && v >= spaces[i - 1]; i--) {
            if (table[i][v] == table[i - 1][v - spaces[i - 1]] + spaces[i - 1]) {
                flag[i - 1] = true;
                v = v - spaces[i - 1];
            }
        }
        int rest = childCount;
        View[] restArray;
        int[] restSpaces;
        for (int i = 0; i < flag.length; i++) {
            if (flag[i]) {
                childList.add(childs[i]);
                rest--;
            }
        }

        if (0 == rest) {
            return;
        }
        restArray = new View[rest];
        restSpaces = new int[rest];
        int index = 0;
        for (int i = 0; i < flag.length; i++) {
            if (!flag[i]) {
                restArray[index] = childs[i];
                restSpaces[index] = spaces[i];
                index++;
            }
        }
        table = null;
        childs = null;
        flag = null;
        sortToCompress(restArray, restSpaces);
    }

    /**
     * add some blank view to make child elements look in alignment
     */
    public void relayoutToAlign() {
        post(new Runnable() {
            @Override
            public void run() {
                align();
            }
        });
    }

    private void align() {
        int childCount = this.getChildCount();
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return;
        }
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof FlowLayout.BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            count++;
        }
        View[] childs = new View[count];
        int[] spaces = new int[count];
        int n = 0;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof FlowLayout.BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue;
            }
            childs[n] = v;
            ViewGroup.LayoutParams childLp = v.getLayoutParams();
            int childWidth = v.getMeasuredWidth();
            if (childLp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) childLp;
                spaces[n] = mlp.leftMargin + childWidth + mlp.rightMargin;
            } else {
                spaces[n] = childWidth;
            }
            n++;
        }
        int lineTotal = 0;
        int start = 0;
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
            if (lineTotal + spaces[i] > usefulWidth) {
                int blankWidth = usefulWidth - lineTotal;
                int end = i - 1;
                int blankCount = end - start;
                if (blankCount >= 0) {
                    if (blankCount > 0) {
                        int eachBlankWidth = blankWidth / blankCount;
                        MarginLayoutParams lp = new MarginLayoutParams(eachBlankWidth, 0);
                        for (int j = start; j < end; j++) {
                            this.addView(childs[j]);
                            BlankView blank = new BlankView(context);
                            this.addView(blank, lp);
                        }
                    }
                    this.addView(childs[end]);
                    start = i;
                    i--;
                    lineTotal = 0;
                } else {
                    this.addView(childs[i]);
                    start = i + 1;
                    lineTotal = 0;
                }
            } else {
                lineTotal += spaces[i];
            }
        }
        for (int i = start; i < count; i++) {
            this.addView(childs[i]);
        }
    }

    /**
     * use both of relayout methods together
     */
    public void relayoutToCompressAndAlign() {
        post(new Runnable() {
            @Override
            public void run() {
                compress();
                align();
            }
        });
    }

    /**
     * cut the flowlayout to the specified num of lines
     *
     * @param line_num_now
     */
    public void specifyLines(final int line_num_now) {
        post(new Runnable() {
            @Override
            public void run() {
                int line_num = line_num_now;
                int childNum = 0;
                if (line_num > lineNumList.size()) {
                    line_num = lineNumList.size();
                }
                for (int i = 0; i < line_num; i++) {
                    childNum += lineNumList.get(i);
                }
                List<View> viewList = new ArrayList<>();
                for (int i = 0; i < childNum; i++) {
                    viewList.add(getChildAt(i));
                }
                removeAllViews();
                for (View v : viewList) {
                    addView(v);
                }
            }
        });
    }

    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return (LinearLayout.LayoutParams) new MarginLayoutParams(p);
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return (LinearLayout.LayoutParams) new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    class BlankView extends View {

        public BlankView(Context context) {
            super(context);
        }
    }
}
