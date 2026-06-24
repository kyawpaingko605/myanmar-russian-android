package com.myanmarrussian

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myanmarrussian.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 💡 ၁။ မူရင်းကုဒ်အတိုင်း View ကို အရင်ဆုံး အောင်မြင်စွာ တည်ဆောက်ခြင်း
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

        // 💡 ၂။ Library မလိုသော Native နည်းလမ်းဖြင့် လုံခြုံရေးနှင့် အင်တာနက် စစ်ဆေးခြင်း
        checkSecurityAndInternet()
    }

    private fun checkSecurityAndInternet() {
        // ⚠️ Library မသုံးဘဲ Native စနစ်ဖြင့် Root စစ်ဆေးခြင်း
        if (isDeviceRooted()) {
            showSecurityDialog(
                "လုံခြုံရေး သတိပေးချက်", 
                "ဤဖုန်းသည် Root ဖောက်ထားသဖြင့် လုံခြုံရေးအရ App အသုံးပြုခွင့်ကို ပိတ်ထားပါသည်။"
            )
            return
        }

        // ⚠️ အင်တာနက် ချိတ်ဆက်ထားခြင်း ရှိ/မရှိ စစ်ဆေးခြင်း
        if (!isInternetAvailable()) {
            showSecurityDialog(
                "အင်တာနက် လိုအပ်ပါသည်", 
                "ဤ App ကို အသုံးပြုရန် အင်တာနက် ချိတ်ဆက်မှု လိုအပ်ပါသည်။ ကျေးဇူးပြု၍ အင်တာနက်ပြန်ဖွင့်ပြီး ပြန်ဝင်ပေးပါ။"
            )
            return
        }
    }

    /**
     * ဖုန်းအတွင်းရှိ Root Files များကို တိုက်ရိုက်ရှာဖွေပေးသော စိတ်ချရသည့် Native Function
     */
    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        try {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in paths) {
                if (File(path).exists()) return true
            }
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }

    /**
     * အင်တာနက် ရှိ၊ မရှိ စစ်ဆေးပေးသော ကူညီမည့် Function
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * စည်းကမ်းမကိုက်ညီပါက App ပိတ်ပစ်မည့် ဒိုင်ယာလော့ခ် ပြသခြင်း
     */
    private fun showSecurityDialog(title: String, message: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("ထွက်ရန်") { _, _ ->
                    finish() // App အား လုံးဝပိတ်ချပစ်ခြင်း
                }
                .show()
        }
    }
}
