package vn.hiep.demobilling.base

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
        onInitView()
        onInitViewModel()
        onClickView()
    }


    abstract fun onClickView()

    @LayoutRes
    abstract fun getLayout(): Int

    abstract fun onInitViewModel()

    abstract fun onInitView()
}