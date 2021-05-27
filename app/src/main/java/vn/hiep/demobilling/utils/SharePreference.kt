package vn.hiep.demobilling.utils

import android.content.Context
import android.content.SharedPreferences

object SharePreference {
    private val PREF_FILE = "MyPref"
    private val PURCHASE_KEY = "purchase"
    private val SUBSCRIBE_KEY = "purchase"


    private fun getPreferenceObject(applicationContext: Context): SharedPreferences {
        return applicationContext.getSharedPreferences(PREF_FILE, 0)
    }

    private fun getPreferenceEditObject(applicationContext: Context): SharedPreferences.Editor {
        val pref = applicationContext.getSharedPreferences(PREF_FILE, 0)
        return pref.edit()
    }

    fun getPurchaseValueFromPref(applicationContext: Context): Boolean {
        return getPreferenceObject(applicationContext).getBoolean(PURCHASE_KEY, false)
    }

    fun savePurchaseValueToPref(value: Boolean, applicationContext: Context) {
        getPreferenceEditObject(applicationContext).putBoolean(PURCHASE_KEY, value).commit()
    }

    fun saveSubscribeValueToPref(value: Boolean, applicationContext: Context) {
        getPreferenceEditObject(applicationContext).putBoolean(SUBSCRIBE_KEY, value).commit()
    }

    fun getSubscribeValueFromPref(applicationContext: Context): Boolean {
        return getPreferenceObject(applicationContext).getBoolean(SUBSCRIBE_KEY, false)
    }

}