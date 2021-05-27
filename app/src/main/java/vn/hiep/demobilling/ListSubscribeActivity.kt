package vn.hiep.demobilling

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_list_subscribe.*
import vn.hiep.demobilling.adapter.SubscribeAdapter
import vn.hiep.demobilling.model.Product

class ListSubscribeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_subscribe)



    }
}