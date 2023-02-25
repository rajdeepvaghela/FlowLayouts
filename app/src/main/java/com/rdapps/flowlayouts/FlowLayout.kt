package com.rdapps.flowlayouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class FlowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var usefulWidth = 0
    // the space of a line we can use(line's width minus the sum of left and right padding

    private var rowSpacing = 0 // the spacing between lines in flowlayout
    private var numberOfColumns = 0 // number of columns in one line

    private var childList: MutableList<View?> = ArrayList()
    private var lineNumList: MutableList<Int> = ArrayList()

    init {
        fetchAttrs(context, attrs)
    }

    private fun fetchAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
        rowSpacing = typedArray.getDimensionPixelSize(R.styleable.FlowLayout_rowSpacing, 0)
        numberOfColumns = typedArray.getInt(R.styleable.FlowLayout_numberOfColumns, 0)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var lineUsed = paddingLeft + paddingRight
        var lineY = paddingTop
        var lineHeight = 0
        for (i in 0 until this.childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            var spaceWidth = 0
            var spaceHeight = 0
            val childLp = child.layoutParams
            if (childLp is MarginLayoutParams) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineY)
                spaceWidth = childLp.leftMargin + childLp.rightMargin
                spaceHeight = childLp.topMargin + childLp.bottomMargin
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
            if (numberOfColumns > 0 && child.measuredWidth == 0) {
                child.layoutParams.width = widthSize / numberOfColumns - spaceWidth
            }
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            spaceWidth += childWidth
            spaceHeight += childHeight
            if (lineUsed + spaceWidth > widthSize) {
                //approach the limit of width and move to next line
                lineY += lineHeight + rowSpacing
                lineUsed = paddingLeft + paddingRight
                lineHeight = 0
            }
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight
            }
            lineUsed += spaceWidth
        }
        setMeasuredDimension(
            widthSize,
            if (heightMode == MeasureSpec.EXACTLY) heightSize else lineY + lineHeight + paddingBottom
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        var lineX = paddingLeft
        var lineY = paddingTop
        val lineWidth = r - l
        usefulWidth = lineWidth - paddingLeft - paddingRight
        var lineUsed = paddingLeft + paddingRight
        var lineHeight = 0
        var lineNum = 0
        lineNumList.clear()
        for (i in 0 until this.childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            var spaceWidth = 0
            var spaceHeight = 0
            var left = 0
            var top = 0
            var right = 0
            var bottom = 0
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val childLp = child.layoutParams
            if (childLp is MarginLayoutParams) {
                spaceWidth = childLp.leftMargin + childLp.rightMargin
                spaceHeight = childLp.topMargin + childLp.bottomMargin
                left = lineX + childLp.leftMargin
                top = lineY + childLp.topMargin
                right = lineX + childLp.leftMargin + childWidth
                bottom = lineY + childLp.topMargin + childHeight
            } else {
                left = lineX
                top = lineY
                right = lineX + childWidth
                bottom = lineY + childHeight
            }
            spaceWidth += childWidth
            spaceHeight += childHeight
            if (lineUsed + spaceWidth > lineWidth) {
                //approach the limit of width and move to next line
                lineNumList.add(lineNum)
                lineY += lineHeight + rowSpacing
                lineUsed = paddingLeft + paddingRight
                lineX = paddingLeft
                lineHeight = 0
                lineNum = 0
                if (childLp is MarginLayoutParams) {
                    left = lineX + childLp.leftMargin
                    top = lineY + childLp.topMargin
                    right = lineX + childLp.leftMargin + childWidth
                    bottom = lineY + childLp.topMargin + childHeight
                } else {
                    left = lineX
                    top = lineY
                    right = lineX + childWidth
                    bottom = lineY + childHeight
                }
            }
            child.layout(left, top, right, bottom)
            lineNum++
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight
            }
            lineUsed += spaceWidth
            lineX += spaceWidth
        }
        // add the num of last line
        lineNumList.add(lineNum)
    }

    /**
     * resort child elements to use lines as few as possible
     */
    fun relayoutToCompress() {
        post { compress() }
    }

    private fun compress() {
        val childCount = this.childCount
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return
        }
        var count = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue
            }
            count++
        }
        val children = arrayOfNulls<View>(count)
        val spaces = IntArray(count)
        var n = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue
            }
            children[n] = v
            val childLp = v.layoutParams
            val childWidth = v.measuredWidth
            if (childLp is MarginLayoutParams) {
                val mlp = childLp
                spaces[n] = mlp.leftMargin + childWidth + mlp.rightMargin
            } else {
                spaces[n] = childWidth
            }
            n++
        }
        val compressSpaces = IntArray(count)
        for (i in 0 until count) {
            compressSpaces[i] = if (spaces[i] > usefulWidth) usefulWidth else spaces[i]
        }
        sortToCompress(children, compressSpaces)
        removeAllViews()
        for (v in childList) {
            this.addView(v)
        }
        childList.clear()
    }

    private fun sortToCompress(children: Array<View?>, spaces: IntArray) {
        val childCount = children.size
        var table: Array<IntArray>? = Array(childCount + 1) { IntArray(usefulWidth + 1) }
        for (i in 0 until childCount + 1) {
            for (j in 0 until usefulWidth) {
                table!![i][j] = 0
            }
        }
        var flag: BooleanArray? = BooleanArray(childCount)
        for (i in 0 until childCount) {
            flag!![i] = false
        }
        for (i in 1..childCount) {
            for (j in spaces[i - 1]..usefulWidth) {
                table!![i][j] =
                    if (table[i - 1][j] > table[i - 1][j - spaces[i - 1]] + spaces[i - 1]) table[i - 1][j] else table[i - 1][j - spaces[i - 1]] + spaces[i - 1]
            }
        }
        var v = usefulWidth
        var i = childCount
        while (i > 0 && v >= spaces[i - 1]) {
            if (table!![i][v] == table[i - 1][v - spaces[i - 1]] + spaces[i - 1]) {
                flag!![i - 1] = true
                v -= spaces[i - 1]
            }
            i--
        }

        var rest = childCount
        for (i in flag!!.indices) {
            if (flag[i]) {
                childList.add(children[i])
                rest--
            }
        }
        if (0 == rest) {
            return
        }
        val restArray: Array<View?> = arrayOfNulls(rest)
        val restSpaces: IntArray = IntArray(rest)
        var index = 0
        for (i in flag.indices) {
            if (!flag[i]) {
                restArray[index] = children[i]
                restSpaces[index] = spaces[i]
                index++
            }
        }
        table = null
        flag = null
        sortToCompress(restArray, restSpaces)
    }

    /**
     * add some blank view to make child elements look in alignment
     */
    fun relayoutToAlign() {
        post { align() }
    }

    private fun align() {
        val childCount = this.childCount
        if (0 == childCount) {
            //no need to sort if flowlayout has no child view
            return
        }
        var count = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue
            }
            count++
        }
        val children = arrayOfNulls<View>(count)
        val spaces = IntArray(count)
        var n = 0
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is BlankView) {
                //BlankView is just to make childs look in alignment, we should ignore them when we relayout
                continue
            }
            children[n] = v
            val childLp = v.layoutParams
            val childWidth = v.measuredWidth
            if (childLp is MarginLayoutParams) {
                spaces[n] = childLp.leftMargin + childWidth + childLp.rightMargin
            } else {
                spaces[n] = childWidth
            }
            n++
        }
        var lineTotal = 0
        var start = 0
        removeAllViews()
        var i = 0
        while (i < count) {
            if (lineTotal + spaces[i] > usefulWidth) {
                val blankWidth = usefulWidth - lineTotal
                val end = i - 1
                val blankCount = end - start
                if (blankCount >= 0) {
                    if (blankCount > 0) {
                        val eachBlankWidth = blankWidth / blankCount
                        val lp = MarginLayoutParams(eachBlankWidth, 0)
                        for (j in start until end) {
                            this.addView(children[j])
                            val blank = BlankView(context)
                            this.addView(blank, lp)
                        }
                    }
                    this.addView(children[end])
                    start = i
                    i--
                    lineTotal = 0
                } else {
                    this.addView(children[i])
                    start = i + 1
                    lineTotal = 0
                }
            } else {
                lineTotal += spaces[i]
            }
            i++
        }
        for (i in start until count) {
            this.addView(children[i])
        }
    }

    /**
     * use both of relayout methods together
     */
    fun relayoutToCompressAndAlign() {
        post {
            compress()
            align()
        }
    }

    /**
     * cut the flowlayout to the specified num of lines
     *
     * @param line_num_now
     */
    fun specifyLines(line_num_now: Int) {
        post {
            var line_num = line_num_now
            var childNum = 0
            if (line_num > lineNumList.size) {
                line_num = lineNumList.size
            }
            for (i in 0 until line_num) {
                childNum += lineNumList[i]
            }
            val viewList: MutableList<View> = ArrayList()
            for (i in 0 until childNum) {
                viewList.add(getChildAt(i))
            }
            removeAllViews()
            for (v in viewList) {
                addView(v)
            }
        }
    }

    /**
     * To set the number of columns for layout
     * @param numberOfColumns number of columns
     */
    fun setNumberOfColumns(numberOfColumns: Int) {
        this.numberOfColumns = numberOfColumns
        post {
            val viewList: MutableList<View> = ArrayList()
            for (i in 0 until childCount) {
                viewList.add(getChildAt(i))
            }
            removeAllViews()
            for (v in viewList) {
                addView(v)
            }
        }
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(super.generateDefaultLayoutParams())
    }

    internal inner class BlankView(context: Context?) : View(context)
}