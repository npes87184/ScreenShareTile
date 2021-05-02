package com.npes87184.screenshottile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
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
        val cardAbout = v.findViewById(R.id.about_card) as CardView

        cardIntro.setOnClickListener {
            onIntroClick()
        }

        cardSetting.setOnClickListener {
            onSettingClick()
        }

        cardAbout.setOnClickListener {
            onAboutClick()
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

    private fun onAboutClick() {
        val aboutFragment = AboutFragment()
        val transaction = fragmentManager!!.beginTransaction()

        transaction.replace(R.id.container, aboutFragment)
        transaction.addToBackStack(null)

        transaction.commit()
    }
}
