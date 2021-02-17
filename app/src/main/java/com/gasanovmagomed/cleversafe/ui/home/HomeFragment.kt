package com.gasanovmagomed.cleversafe.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gasanovmagomed.cleversafe.R

class HomeFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val storageBtn = root.findViewById<Button>(R.id.storageView)
        val browserBtn = root.findViewById<Button>(R.id.browserView)
        storageBtn.setOnClickListener(this)
        browserBtn.setOnClickListener(this)
        return root
    }

    override fun onClick(v: View?) {
        when(setButtonIndex(v!!.id)){
            1 -> loadImages()
            2 -> gotoBrowser()
        }
    }

    private fun gotoBrowser() {
        Toast.makeText(activity, "Realise function goto browses", Toast.LENGTH_SHORT).show()
    }

    private fun loadImages() {
        Toast.makeText(activity, "Realise function upload photos", Toast.LENGTH_SHORT).show()
    }

    private fun setButtonIndex(id: Int): Int {
        var index = -1
        when(id){
            R.id.storageView -> index = 1
            R.id.browserView -> index = 2
        }

        return index
    }
}