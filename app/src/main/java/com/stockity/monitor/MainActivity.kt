package com.stockity.monitor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val captureRequestCode = 501

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 24)
        }

        val title = TextView(this).apply {
            text = "STOCKITY AI\nFLOATING MONITOR"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val info = TextView(this).apply {
            text = "\nVERSI 0.1\n\nUji panel mengambang dan izin tangkap layar.\n\nPembaca candle belum diaktifkan."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val permissionButton = Button(this).apply {
            text = "AKTIFKAN IZIN FLOATING"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Izin floating sudah aktif",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val startButton = Button(this).apply {
            text = "MULAI MONITOR"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(
                        this@MainActivity,
                        "Aktifkan izin floating dahulu",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val manager = getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE
                ) as MediaProjectionManager

                startActivityForResult(
                    manager.createScreenCaptureIntent(),
                    captureRequestCode
                )
            }
        }

        val stopButton = Button(this).apply {
            text = "HENTIKAN MONITOR"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java))
                stopService(Intent(this@MainActivity, FloatingOverlayService::class.java))
                Toast.makeText(
                    this@MainActivity,
                    "Monitor dihentikan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 14
        }

        layout.addView(title, params)
        layout.addView(info, params)
        layout.addView(permissionButton, params)
        layout.addView(startButton, params)
        layout.addView(stopButton, params)

        setContentView(layout)
    }

    @Deprecated("Compatibility with Android activity result")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == captureRequestCode) {
            if (resultCode == RESULT_OK && data != null) {
                val serviceIntent = Intent(
                    this,
                    ScreenCaptureService::class.java
                ).apply {
                    putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                    putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                startService(
                    Intent(this, FloatingOverlayService::class.java)
                )
            } else {
                Toast.makeText(
                    this,
                    "Izin tangkap layar dibatalkan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
