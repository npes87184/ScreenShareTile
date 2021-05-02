package com.npes87184.screenshottile.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.npes87184.screenshottile.R
import com.npes87184.screenshottile.utils.Define
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element


class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val versionElement = Element()
        var versionNum: String? = ""
        versionNum = try {
            activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
                .versionName
        } catch (e: Exception) {
            getString(R.string.about_version)
        }
        versionElement.title = getString(R.string.about_version_tag) + versionNum

        val authorElement = Element()
        authorElement.title = getString(R.string.about_author_tag) + getString(R.string.about_author)

        val emailElement = Element()
        emailElement.title = getString(R.string.about_contact)
        emailElement.iconDrawable = R.drawable.about_icon_email
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", "npes87184@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        emailElement.intent = emailIntent

        val webElement = Element()
        webElement.title = getString(R.string.about_web_tag)
        webElement.iconDrawable = R.drawable.about_icon_link
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Define.MY_WEB_URL))
        webElement.intent = webIntent

        val playStoreElement = Element()
        playStoreElement.title = getString(R.string.about_rate)
        playStoreElement.iconDrawable = R.drawable.about_icon_google_play
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Define.APP_URL))
        playStoreElement.intent = playStoreIntent

        val aboutPage = AboutPage(activity)
        aboutPage.setDescription(getString(R.string.app_name))
        aboutPage.addItem(versionElement)
        aboutPage.addItem(authorElement)
        aboutPage.addItem(emailElement)
        aboutPage.addItem(webElement)
        aboutPage.addItem(playStoreElement)

        return aboutPage.create()
    }

}