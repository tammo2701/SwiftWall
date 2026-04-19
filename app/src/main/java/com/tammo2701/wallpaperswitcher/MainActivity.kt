package com.tammo2701.wallpaperswitcher

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FolderAdapter
    private lateinit var tvEmpty: TextView
    private val folders = mutableListOf<Folder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = FolderAdapter(
            folders,
            onClick = { folder, _ -> openFolder(folder) },
            onLongClick = { folder, index -> showFolderOptions(folder, index) }
        )

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        folders.addAll(DataManager.load(this))
        adapter.notifyDataSetChanged()
        updateEmpty()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showNewFolderDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        folders.clear()
        folders.addAll(DataManager.load(this))
        adapter.notifyDataSetChanged()
        updateEmpty()
    }

    private fun showNewFolderDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_new_folder)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etName = dialog.findViewById<TextInputEditText>(R.id.etFolderName)
        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnCreate).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            folders.add(Folder(name = name))
            adapter.notifyItemInserted(folders.size - 1)
            DataManager.save(this, folders)
            updateEmpty()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openFolder(folder: Folder) {
        startActivity(Intent(this, FolderActivity::class.java).apply {
            putExtra("folder_id", folder.id)
        })
    }

    private fun showFolderOptions(folder: Folder, index: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(folder.name)
            .setItems(arrayOf("🗑️  Ordner löschen")) { _, _ ->
                folders.removeAt(index)
                adapter.notifyItemRemoved(index)
                DataManager.save(this, folders)
                updateEmpty()
            }
            .show()
    }

    private fun updateEmpty() {
        tvEmpty.visibility = if (folders.isEmpty()) View.VISIBLE else View.GONE
    }
}