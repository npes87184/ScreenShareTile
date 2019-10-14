package com.npes87184.screenshottile

import android.graphics.Color
import com.github.paolorotolo.appintro.model.SliderPage
import android.os.Bundle
import androidx.annotation.Nullable
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment


class IntroActivity : AppIntro2() {
    private val greenColor = Color.parseColor("#009688")

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sliderPage0 = SliderPage()
        sliderPage0.title = getString(R.string.intro_title_0)
        sliderPage0.description = getString(R.string.intro_description_0)
        sliderPage0.imageDrawable = R.mipmap.ic_launcher
        sliderPage0.bgColor = greenColor
        addSlide(AppIntro2Fragment.newInstance(sliderPage0))

        val sliderPage1 = SliderPage()
        sliderPage1.title = getString(R.string.intro_title_1)
        sliderPage1.description = getString(R.string.intro_description_1)
        sliderPage1.imageDrawable = R.drawable.intro_1
        sliderPage1.bgColor = greenColor
        addSlide(AppIntro2Fragment.newInstance(sliderPage1))

        val sliderPage2 = SliderPage()
        sliderPage2.title = getString(R.string.intro_title_2)
        sliderPage2.description = getString(R.string.intro_description_2)
        sliderPage2.imageDrawable = R.drawable.intro_2
        sliderPage2.bgColor = greenColor
        addSlide(AppIntro2Fragment.newInstance(sliderPage2))

        val sliderPage3 = SliderPage()
        sliderPage3.title = getString(R.string.intro_title_3)
        sliderPage3.description = getString(R.string.intro_description_3)
        sliderPage3.imageDrawable = R.drawable.intro_3
        sliderPage3.bgColor = greenColor
        addSlide(AppIntro2Fragment.newInstance(sliderPage3))

        val sliderPage4 = SliderPage()
        sliderPage4.title = getString(R.string.intro_title_4)
        sliderPage4.description = getString(R.string.intro_description_4)
        sliderPage4.imageDrawable = R.mipmap.ic_launcher
        sliderPage4.bgColor = greenColor
        addSlide(AppIntro2Fragment.newInstance(sliderPage4))

        setBarColor(greenColor)

        // Hide Skip/Done button.
        showSkipButton(false)
        isProgressButtonEnabled = true
    }

    override fun onDonePressed() {
        finish()
    }
}