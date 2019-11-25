package com.angcyo.tablayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * 指示器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/11/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class TabIndicator(val tabLayout: DslTabLayout) : DslDrawable() {

    companion object {
        //不绘制指示器
        const val INDICATOR_STYLE_NONE = 0

        //指示器绘制在[itemView]的底部
        const val INDICATOR_STYLE_BOTTOM = 1

        //指示器绘制[itemView]的背部, [itemView] 请不要设置background, 否则可能看不见
        const val INDICATOR_STYLE_BACKGROUND = 2
    }

    /**指示器绘制的样式*/
    var indicatorStyle = INDICATOR_STYLE_BOTTOM

    /**指示器在流向下一个位置时, 是否采用[Flow]流线的方式改变宽度*/
    var indicatorEnableFlow: Boolean = true

    /**指示器绘制实体*/
    var indicatorDrawable: Drawable? = null

    /**
     * 指示器的宽度
     * WRAP_CONTENT: [childView]内容的宽度,
     * MATCH_PARENT: [childView]的宽度
     * 40dp: 固定值
     * */
    var indicatorWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    /**宽度补偿*/
    var indicatorWidthOffset = 0

    /**
     * 指示器的高度
     * WRAP_CONTENT: [childView]内容的高度,
     * MATCH_PARENT: [childView]的高度
     * 40dp: 固定值
     * */
    var indicatorHeight = 4 * dpi
    /**高度补偿*/
    var indicatorHeightOffset = 0

    /**XY轴方向补偿*/
    var indicatorXOffset = 0
    var indicatorYOffset = -2 * dpi

    /**
     * 宽高[WRAP_CONTENT]时, 内容view的定位索引
     * */
    var indicatorContentIndex = -1

    init {
        callback = tabLayout
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
    }

    /**
     * [childview]对应的中心x坐标
     * */
    fun getChildCenterX(index: Int): Int {

        var result = 0

        tabLayout.dslSelector.visibleViewList.getOrNull(index)?.also { childView ->
            val lp = childView.layoutParams as DslTabLayout.LayoutParams

            //如果child强制指定了index, 就用指定的.
            val contentIndex =
                if (lp.indicatorContentIndex >= 0) lp.indicatorContentIndex else indicatorContentIndex

            result = childView.left + childView.paddingLeft + childView.viewDrawWidth / 2

            if (contentIndex >= 0) {
                //有指定
                if (childView is ViewGroup && contentIndex in 0 until childView.childCount) {
                    val contentChildView = childView.getChildAt(contentIndex)

                    val contentLp = contentChildView.layoutParams
                    val contentLeftMargin =
                        (contentLp as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0

                    result =
                        childView.left + childView.paddingLeft +
                                contentLeftMargin + contentChildView.paddingLeft + contentChildView.viewDrawWidth / 2
                }
            } else {
                //没有指定
            }
        }

        return result
    }

    fun getIndicatorDrawWidth(index: Int): Int {
        var result = indicatorWidth

        when (indicatorWidth) {
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                tabLayout.dslSelector.visibleViewList.getOrNull(index)?.also { childView ->
                    val lp = childView.layoutParams as DslTabLayout.LayoutParams

                    //如果child强制指定了index, 就用指定的.
                    val contentIndex =
                        if (lp.indicatorContentIndex >= 0) lp.indicatorContentIndex else indicatorContentIndex

                    result = childView.viewDrawWidth

                    if (contentIndex >= 0) {
                        //有指定
                        if (childView is ViewGroup && contentIndex in 0 until childView.childCount) {
                            val contentChildView = childView.getChildAt(contentIndex)

                            result = contentChildView.viewDrawWidth
                        }
                    }
                }
            }
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                tabLayout.dslSelector.visibleViewList.getOrNull(index)?.also { childView ->
                    result = childView.measuredWidth
                }
            }
        }

        return result + indicatorWidthOffset
    }

    fun getIndicatorDrawHeight(index: Int): Int {
        var result = indicatorHeight

        when (indicatorHeight) {
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                tabLayout.dslSelector.visibleViewList.getOrNull(index)?.also { childView ->
                    val lp = childView.layoutParams as DslTabLayout.LayoutParams

                    //如果child强制指定了index, 就用指定的.
                    val contentIndex =
                        if (lp.indicatorContentIndex >= 0) lp.indicatorContentIndex else indicatorContentIndex

                    result = childView.viewDrawHeight

                    if (contentIndex >= 0) {
                        //有指定
                        if (childView is ViewGroup && contentIndex in 0 until childView.childCount) {
                            val contentChildView = childView.getChildAt(contentIndex)

                            result = contentChildView.viewDrawHeight
                        }
                    }
                }
            }
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                tabLayout.dslSelector.visibleViewList.getOrNull(index)?.also { childView ->
                    result = childView.measuredHeight
                }
            }
        }

        return result + indicatorHeightOffset
    }

    //当前选中的index
    val currentSelectIndex: Int
        get() = tabLayout.dslSelector.dslSelectorConfig.dslSelectIndex

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isVisible || indicatorStyle == INDICATOR_STYLE_NONE || indicatorDrawable == null) {
            //不绘制
            return
        }

        val currentIndex = currentSelectIndex
        val drawCenterX = getChildCenterX(currentIndex)
        val drawWidth = getIndicatorDrawWidth(currentIndex)
        val drawHeight = getIndicatorDrawHeight(currentIndex)

        val drawLeft = drawCenterX - drawWidth / 2 + indicatorXOffset
        val drawTop = when (indicatorStyle) {
            INDICATOR_STYLE_BOTTOM -> {
                //底部绘制
                viewHeight - drawHeight
            }
            else -> {
                //居中绘制
                paddingTop + viewDrawHeight / 2 - drawHeight / 2
            }
        } + indicatorYOffset

        indicatorDrawable?.apply {
            setBounds(drawLeft, drawTop, drawLeft + drawWidth, drawTop + drawHeight)
            draw(canvas)
        }
    }
}