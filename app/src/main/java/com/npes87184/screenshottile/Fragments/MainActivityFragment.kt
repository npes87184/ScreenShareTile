package com.npes87184.screenshottile.Fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import android.content.Intent
import com.npes87184.screenshottile.IntroActivity
import com.npes87184.screenshottile.R

class MainActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main, container, false)
        val cardIntro = v.findViewById(R.id.intro_card) as CardView
        val cardSetting = v.findViewById(R.id.setting_card) as CardView

        cardIntro.setOnClickListener {
            onIntroClick()
        }

        cardSetting.setOnClickListener {
            onSettingClick()
        }

        return v
    }

    private fun onIntroClick() {
        val intent = Intent(activity, IntroActivity::class.java)
        startActivity(intent)
    }

    private fun onSettingClick() {
        val settingFragment = SettingFragment()
        val transaction = fragmentManager!!.beginTransaction()

        transaction.replace(R.id.container, settingFragment)
        transaction.addToBackStack(null)

        transaction.commit()
    }
}
