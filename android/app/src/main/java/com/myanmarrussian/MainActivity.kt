package com.myanmarrussian

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myanmarrussian.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🌓 ⚡ App စဖွင့်လိုက်တာနဲ့ သိမ်းဆည်းထားဖူးတဲ့ Theme mode ကို အရင်ဆုံး ဖတ်ယူပြီး သက်ရောက်စေခြင်း
        val sharedPref = getSharedPreferences("AppState", Context.MODE_PRIVATE)
        val savedTheme = sharedPref.getInt("APP_THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        super.onCreate(savedInstanceState)
        
        // 💡 ၁။ View ကို အရင်ဆုံး အောင်မြင်စွာ တည်ဆောက်ခြင်း
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // Set selected item color
        bottomNav.itemIconTintList = resources.getColorStateList(R.color.nav_item_color, theme)
        bottomNav.itemTextColor = resources.getColorStateList(R.color.nav_item_color, theme)

        // 💡 ၂။ ဖုန်းတိုင်းတွင် လုံးဝ Crash မဖြစ်စေရန် Handler သုံးပြီး ဘေးကင်းစွာ စစ်ဆေးခြင်း
        Handler(Looper.getMainLooper()).post {
            checkSecurityAndInternet()
        }
    }

    /**
     * ⚙️ အသုံးပြုသူမှ Light/Dark/Auto Theme ကို ကိုယ်တိုင်ရွေးချယ်ပြောင်းလဲချိန်တွင် ခေါ်ယူရန် Function
     * (ဥပမာ - Settings ထဲတွင် ခလုတ်နှိပ်ပြီး ပြောင်းလဲသည့်အခါ ခေါ်သုံးနိုင်သည်)
     * @param themeMode AppCompatDelegate.MODE_NIGHT_NO (သို့) MODE_NIGHT_YES (သို့) MODE_NIGHT_FOLLOW_SYSTEM
     */
    fun updateAppTheme(themeMode: Int) {
        // SharedPreferences ထဲတွင် အမြဲတမ်းသိမ်းဆည်းထားမည်
        val sharedPref = getSharedPreferences("AppState", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("APP_THEME_MODE", themeMode).apply()
        
        // Theme ကို ချက်ချင်းပြောင်းလဲသက်ရောက်စေမည်
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun checkSecurityAndInternet() {
        // ⚠️ Root စစ်ဆေးခြင်း
        if (isDeviceRooted()) {
            showUniversalSafeWarning(
                "လုံခြုံရေး သတိပေးချက်", 
                "ဤဖုန်းသည် Root ဖောက်ထားသဖြင့် လုံခြုံရေးအရ App အသုံးပြုခွင့်ကို ပိတ်ထားပါသည်။"
            )
            return
        }

        // ⚠️ အင်တာနက် စစ်ဆေးခြင်း
        if (!isInternetAvailable()) {
            showUniversalSafeWarning(
                "အင်တာနက် လိုအပ်ပါသည်", 
                "ဤ App ကို အသုံးပြုရန် အင်တာနက် ချိတ်ဆက်မှု လိုအပ်ပါသည်။ ကျေးဇူးပြု၍ အင်တာနက်ပြန်ဖွင့်ပြီး ပြန်ဝင်ပေးပါ။"
            )
            return
        }
    }

    /**
     * 💡 ဖုန်းမော်ဒယ်စုံ (Oppo, Vivo, Samsung) တို့တွင် Theme Error ကြောင့် Crash ဖြစ်ခြင်းမှ ကာကွယ်ပေးသော စနစ်
     */
    private fun showUniversalSafeWarning(title: String, message: String) {
        if (isFinishing || isDestroyed) return

        try {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("ထွက်ရန်") { _, _ ->
                    finishAffinity()
                }
                .show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "$title\n$message", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({
                finishAffinity()
            }, 2500)
        }
    }

    /**
     * Native နည်းလမ်းဖြင့် Root စစ်ဆေးခြင်း
     */
    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        return try {
            val paths = arrayOf(
                "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
                "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"
            )
            paths.any { File(it).exists() }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ဗားရှင်းအားလုံးအတွက် စိတ်ချရသော အင်တာနက် စစ်ဆေးခြင်း
     */
    private fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            true
        }
    }
}
