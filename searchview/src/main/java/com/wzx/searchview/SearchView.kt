package com.wzx.searchview

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/7/26
 * 更新时间：
 * 更新内容：
 */
class SearchView : View, View.OnClickListener, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    companion object {
        const val STATE_NONE = 0
        const val STATE_STARTING = 1
        const val STATE_SEARCHING = 2
        const val STATE_ENDING = 3
        const val STATE_CANCEL = 4
    }


    private val defaultSize = 48f
    private val defaultWidth = 2f
    // 默认的搜索动画周期 20s
    private val defaultSearchDuration = 20000
    private val defaultStartDuration = 600

    var strokeColor = Color.WHITE
    var strokeWidth = 2
    var duration: Int = 0

    // 画笔
    private lateinit var mPaint: Paint

    // View 宽高
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    // 当前的状态(非常重要)
    private var mCurrentState = STATE_NONE

    // 放大镜与外部圆环
    private var path_srarch: Path? = null
    private var path_circle: Path? = null

    // 测量Path 并截取部分的工具
    private var mMeasure: PathMeasure? = null


    // 控制各个过程的动画
    private var mStartingAnimator: ValueAnimator? = null
    private var mSearchingAnimator: ValueAnimator? = null
    private var mEndingAnimator: ValueAnimator? = null

    // 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private var mAnimatorValue = 0f

    // 用于控制动画状态转换
    private var mAnimatorHandler: Handler? = null

    // 判断是否已经搜索结束
    private var isStart = false

    private var listener: OnSearchListener? = null

    interface OnSearchListener {
        fun onSearchStart()
        fun onSearchEnd()
        fun onSearchCancel()
    }


    constructor(context: Context?) : super(context) {
        initPaint()

        initHandler()

        initAnimator()

        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchView)
        strokeColor = typedArray.getColor(R.styleable.SearchView_strokeColor, Color.WHITE)
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.SearchView_strokeWidth, dp2px(defaultWidth))
        duration = typedArray.getInteger(R.styleable.SearchView_duration, defaultSearchDuration)
        typedArray.recycle()

        initPaint()

        initHandler()

        initAnimator()

        setOnClickListener(this)
    }

    /**
     * 初始化画笔
     */
    private fun initPaint() {
        mPaint = Paint()
        mPaint.style = Paint.Style.STROKE
        mPaint.color = strokeColor
        mPaint.strokeWidth = this.strokeWidth.toFloat()
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.isAntiAlias = true
    }

    /**
     *
     */
    private fun initPath() {

        //搜索图标圆半径
        val radius1 = mViewWidth / 4 - max(paddingLeft, paddingRight, paddingTop, paddingBottom)

        //加载圆半径
        val radius2 = mViewWidth / 2 - max(paddingLeft, paddingRight, paddingTop, paddingBottom)

        path_srarch = Path()
        path_circle = Path()

        mMeasure = PathMeasure()

        // 注意,不要到360度,否则内部会自动优化,测量不能取到需要的数值
        val oval1 = RectF((-radius1 - radius1 / 2).toFloat(), (-radius1 - radius1 / 2).toFloat(), (radius1 - radius1 / 2).toFloat(), (radius1 - radius1 / 2).toFloat())          // 放大镜圆环
        path_srarch!!.addArc(oval1, 45f, 359.9f)

        val oval2 = RectF((-radius2).toFloat(), (-radius2).toFloat(), (radius2).toFloat(), (radius2).toFloat())      // 外部圆环
        path_circle!!.addArc(oval2, 45f, 359.9f)

        val pos = FloatArray(2)

        // 放大镜把手的位置
        mMeasure!!.setPath(path_circle, false)
        mMeasure!!.getPosTan(0f, pos, null)

        // 放大镜把手
        path_srarch!!.lineTo(pos[0], pos[1])
    }

    private fun initHandler() {
        mAnimatorHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    STATE_STARTING -> {
                        isStart = true
                        mCurrentState = STATE_STARTING
                        mStartingAnimator!!.start()
                    }
                    STATE_SEARCHING -> {
                        mCurrentState = STATE_SEARCHING
                        mSearchingAnimator!!.start()
                    }
                    STATE_ENDING -> {
                        if (isStart) {
                            isStart = false
                            mCurrentState = STATE_ENDING
                            stopAnimator()
                            mEndingAnimator!!.start()

                            if (listener != null) {
                                listener!!.onSearchEnd()
                            }
                        }
                    }
                    STATE_CANCEL -> {
                        if (isStart) {
                            isStart = false
                            mCurrentState = STATE_CANCEL
                            stopAnimator()
                            mEndingAnimator!!.start()

                            if (listener != null) {
                                listener!!.onSearchCancel()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 停止开始和搜索动画
     */
    private fun stopAnimator() {
        if (mStartingAnimator!!.isRunning) {
            mStartingAnimator!!.end()
        }
        if (mSearchingAnimator!!.isRunning) {
            mSearchingAnimator!!.end()
        }
    }

    /**
     * 创建搜索动画
     */
    private fun initSearchAnimator(): ValueAnimator {
        var animator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration.toLong())
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        return animator
    }

    private fun initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(defaultStartDuration.toLong())
        mSearchingAnimator = initSearchAnimator()
        mEndingAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(defaultStartDuration.toLong())

        mStartingAnimator!!.addUpdateListener(this)
        mSearchingAnimator!!.addUpdateListener(this)
        mEndingAnimator!!.addUpdateListener(this)

        mStartingAnimator!!.addListener(this)
        mSearchingAnimator!!.addListener(this)
        mEndingAnimator!!.addListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measureSize(widthMeasureSpec)
        mViewHeight = measureSize(heightMeasureSpec)

        initPath()

        setMeasuredDimension(mViewWidth, mViewHeight)
    }

    /**
     * 设置size
     */
    fun measureSize(measureSpec: Int): Int {

        val specModel = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        when (specModel) {
            MeasureSpec.EXACTLY -> {
                //andorid:layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸
                return specSize
            }
            else -> {
                //包含：MeasureSpec.AT_MOST和MeasureSpec.UNSPECIFIED两种模式
                return dp2px(defaultSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate((mViewWidth / 2).toFloat(), (mViewHeight / 2).toFloat())

        when (mCurrentState) {
            STATE_NONE -> {
                canvas.drawPath(path_srarch, mPaint)
            }
            STATE_STARTING -> {
                mMeasure!!.setPath(path_srarch, false)
                val dst = Path()
                mMeasure!!.getSegment(mMeasure!!.length * mAnimatorValue, mMeasure!!.length, dst, true)
                canvas.drawPath(dst, mPaint)
            }
            STATE_SEARCHING -> {
                mMeasure!!.setPath(path_circle, false)
                val dst2 = Path()
                val stop = mMeasure!!.length * mAnimatorValue
                val start = (stop - (0.5 - Math.abs(mAnimatorValue - 0.5)) * 200f).toFloat()
                mMeasure!!.getSegment(start, stop, dst2, true)
                canvas.drawPath(dst2, mPaint)
            }
            else -> {
                //结束和取消
                mMeasure!!.setPath(path_srarch, false)
                val dst3 = Path()
                mMeasure!!.getSegment(mMeasure!!.length * mAnimatorValue, mMeasure!!.length, dst3, true)
                canvas.drawPath(dst3, mPaint)
            }
        }
    }

    /**
     * 点击监听
     */
    override fun onClick(view: View) {
        if (!isStart) {
            // 进入开始动画
            mAnimatorHandler!!.sendEmptyMessage(STATE_STARTING)
        }
    }

    /**
     * 动画变化监听
     */
    override fun onAnimationUpdate(animation: ValueAnimator) {
        mAnimatorValue = animation.animatedValue as Float
        postInvalidate()
    }

    /**
     * 动画状态变化 start
     */
    override fun onAnimationEnd(animator: Animator?) {
        when (animator) {
            mStartingAnimator -> {
                Log.i("SearchView", "mStartingAnimator end")
                if (isStart) {
                    mAnimatorHandler!!.sendEmptyMessage(STATE_SEARCHING)
                }
            }
            mSearchingAnimator -> {
                Log.i("SearchView", "mSearchingAnimator end")
//                mAnimatorHandler!!.sendEmptyMessage(STATE_SEARCHING)

            }
            mEndingAnimator -> {
                Log.i("SearchView", "mEndingAnimator end")

            }
        }
    }

    override fun onAnimationStart(animator: Animator?) {
        when (animator) {
            mStartingAnimator -> {
                Log.i("SearchView", "mStartingAnimator start")
                if (listener != null) {
                    listener!!.onSearchStart()
                }
            }
            mSearchingAnimator -> {
                Log.i("SearchView", "mSearchingAnimator start")
            }
            mEndingAnimator -> {
                Log.i("SearchView", "mEndingAnimator start")
            }
        }
    }

    override fun onAnimationRepeat(animation: Animator?) {

    }

    override fun onAnimationCancel(animation: Animator?) {

    }

    /**
     * 动画状态变化 end
     */

    fun addOnSearchListener(listener: OnSearchListener) {
        this.listener = listener
    }

    /**
     * 取消搜索
     */
    fun cancelSearch() {
        mAnimatorHandler!!.sendEmptyMessage(STATE_CANCEL)
    }

    /**
     * 停止搜索
     */
    fun endSearch() {
        mAnimatorHandler!!.sendEmptyMessage(STATE_ENDING)
    }

    /**
     * 释放Animator占用内存
     */
    fun release() {
        if (mStartingAnimator != null) {
            mStartingAnimator!!.cancel()
        }

        if (mSearchingAnimator != null) {
            mSearchingAnimator!!.cancel()
        }

        if (mEndingAnimator != null) {
            mEndingAnimator!!.cancel()
        }

    }

    /**
     *
     */
    fun isSearching(): Boolean = isStart

    /**
     * 求最大
     */
    fun max(vararg num: Int): Int {
        var max = num[0]
        for (i in 1 until num.size) {
            if (num[i] > max) {
                max = num[i]
            }
        }
        return max
    }

    /**
     * dp转px
     */
    fun dp2px(dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}