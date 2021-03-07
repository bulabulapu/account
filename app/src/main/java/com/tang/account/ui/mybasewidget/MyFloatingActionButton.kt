package com.tang.account.ui.mybasewidget

import android.animation.Animator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tang.account.R
import com.tang.account.logic.util.OtherUtil.dip2px

/*带标签浮动按钮类*/
class MyFloatingActionButton(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : RelativeLayout(context, attrs, defStyleAttr) {
    /*标签的圆角边框*/
    private val mCardView: CardView = CardView(context)

    /*标签文字*/
    private val mTextView: TextView = TextView(context)

    /*浮动按钮*/
    private val mButton: FloatingActionButton = FloatingActionButton(context)

    /*按钮出现动画*/
    private val scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up_fab)

    /*标签滑入动画*/
    private val slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right)

    /*标签滑出动画*/
    private val slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_right)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context) : this(context, null) {
    }

    /*初始化操作*/
    init {
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = layoutParams
        this.addView(mCardView)
        this.addView(mButton) // RelativeLayout初始化
        val cardLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        cardLayoutParams.addRule(CENTER_VERTICAL)
        mCardView.layoutParams = cardLayoutParams
        mCardView.id = R.id.label_card
        mCardView.addView(mTextView)
        mCardView.visibility = View.INVISIBLE
        mCardView.radius = 10f    // CardView初始化
        val buttonParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        buttonParams.setMargins(
            dip2px(context, 5f),
            dip2px(context, 15f),
            dip2px(context, 15f),
            dip2px(context, 15f)
        )
        buttonParams.addRule(RIGHT_OF, mCardView.id)
        mButton.layoutParams = buttonParams  // FloatingActionButton初始化
        val textViewParams =
            FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textViewParams.setMargins(
            dip2px(context, 5f),
            dip2px(context, 3f),
            dip2px(context, 5f),
            dip2px(context, 3f)
        )
        mTextView.layoutParams = textViewParams
        mTextView.textSize = 18f //  TextView初始化
        mButton.addOnHideAnimationListener(object : Animator.AnimatorListener {
            // 设置Button隐藏动画完成后的动作
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                mCardView.visibility = View.INVISIBLE
                this@MyFloatingActionButton.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
    }

    /**
     * 设置标签文字
     * @param text 标签文字
     */
    fun setLabel(text: String) {
        mTextView.text = text
    }

    /**
     * 设置标签文字
     * @param resId 文字id
     */
    fun setLabel(resId: Int) {
        mTextView.setText(resId)
    }

    /**
     * 设置按钮图标
     * @param resId 图片id
     */
    fun setIcon(resId: Int) {
        mButton.setImageResource(resId)
    }

    /**
     * 设置点击事件监听
     * @param onClickListener 监听器
     */
    fun setOnclickListener(onClickListener: OnClickListener) {
        mButton.setOnClickListener(onClickListener)
    }

    /**
     * 设置点击事件监听
     * @param listener 执行的语句的lambda表达式
     */
    fun setOnclickListener(listener: () -> Unit) {
        mButton.setOnClickListener {
            listener()
        }
    }

    /**
     * 显示按钮
     */
    fun show() {
        this.visibility = View.VISIBLE
        mButton.startAnimation(scaleUp)
        mButton.show()
        if (!TextUtils.isEmpty(mTextView.text)) { // 显示非空标签
            mCardView.startAnimation(slideIn)
            mCardView.visibility = View.VISIBLE
        }
    }

    /**
     * 隐藏按钮
     */
    fun hide() {
        mCardView.visibility = View.INVISIBLE
        mButton.hide()
        mCardView.startAnimation(slideOut)
    }
}