package com.musclehack.targetedHypertrophyTraining.databackup

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.data.source.local.AppDatabase
import com.roomdbexportimport.RoomDBExportImport
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class BackupFragment : DaggerFragment() {

    @Inject
    lateinit var appDatabase: AppDatabase

    private lateinit var dbHelper: RoomDBExportImport

    private val exportDbLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip"),
        ) { uri ->
            uri?.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dbHelper.export(requireContext(), it)
                    Toast.makeText(
                        requireContext(),
                        "Database exported successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private val importDbLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dbHelper.import(requireContext(), it, true)
                    Toast.makeText(
                        requireContext(),
                        "Database restored successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backup, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            // Return CONSUMED if you don't want the window insets to keep passing down
            // to descendant views.
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the database helper
        dbHelper = RoomDBExportImport(appDatabase.openHelper)

        val backupButton = view.findViewById<Button>(R.id.btn_backup)
        val restoreButton = view.findViewById<Button>(R.id.btn_restore)

        backupButton.setOnClickListener {
            exportDbLauncher.launch("tht_backup_${System.currentTimeMillis()}.zip")
        }

        restoreButton.setOnClickListener {
            importDbLauncher.launch(arrayOf("application/zip"))
        }
    }
}
