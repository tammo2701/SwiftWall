package com.tammo2701.wallpaperswitcher

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class FolderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WallpaperAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var folder: Folder
    private val images = mutableListOf<String>()

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = saveImageLocally(it) ?: return@let
            images.add(path)
            folder.images.add(path)
            adapter.notifyItemInserted(images.size - 1)
            saveData()
            updateEmpty()
        }
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImage.launch("image/*")
        else Toast.makeText(this, "Berechtigung benötigt!", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        val folderId = intent.getStringExtra("folder_id") ?: run { finish(); return }
        val allFolders = DataManager.load(this)
        folder = allFolders.find { it.id == folderId } ?: run { finish(); return }

        findViewById<TextView>(R.id.toolbar).text = folder.name
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)

        images.addAll(folder.images.filter { File(it).exists() })

        adapter = WallpaperAdapter(
            images,
            onClick = { path -> setWallpaper(path) },
            onLongClick = { path, index -> showImageOptions(path, index) }
        )

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        updateEmpty()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            checkPermissionAndPick()
        }
    }

    private fun checkPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            pickImage.launch("image/*")
        else
            requestPermission.launch(permission)
    }

    private fun setWallpaper(path: String) {
        try {
            val wm = WallpaperManager.getInstance(this)
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            var bitmap = BitmapFactory.decodeFile(path) ?: return

            // EXIF-Rotation korrigieren
            val exif = androidx.exifinterface.media.ExifInterface(path)
            val orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = android.graphics.Matrix()
            when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            bitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Auf Bildschirmgröße zuschneiden
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()
            val scaledBitmap = if (bitmapRatio > screenRatio) {
                val newWidth = (bitmap.height * screenRatio).toInt()
                val x = (bitmap.width - newWidth) / 2
                android.graphics.Bitmap.createBitmap(bitmap, x, 0, newWidth, bitmap.height)
            } else {
                val newHeight = (bitmap.width / screenRatio).toInt()
                val y = (bitmap.height - newHeight) / 2
                android.graphics.Bitmap.createBitmap(bitmap, 0, y, bitmap.width, newHeight)
            }

            wm.setBitmap(scaledBitmap)
            Toast.makeText(this, "✅ Hintergrundbild gesetzt!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageOptions(path: String, index: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Optionen")
            .setItems(arrayOf("🖼️  Als Hintergrund setzen", "🗑️  Bild löschen")) { _, which ->
                when (which) {
                    0 -> setWallpaper(path)
                    1 -> {
                        images.removeAt(index)
                        folder.images.remove(path)
                        adapter.notifyItemRemoved(index)
                        File(path).delete()
                        saveData()
                        updateEmpty()
                    }
                }
            }
            .show()
    }

    private fun saveImageLocally(uri: Uri): String? {
        return try {
            val file = File(filesDir, "wp_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    private fun saveData() {
        val allFolders = DataManager.load(this)
        val idx = allFolders.indexOfFirst { it.id == folder.id }
        if (idx >= 0) { allFolders[idx] = folder; DataManager.save(this, allFolders) }
    }

    private fun updateEmpty() {
        tvEmpty.visibility = if (images.isEmpty()) View.VISIBLE else View.GONE
    }
}