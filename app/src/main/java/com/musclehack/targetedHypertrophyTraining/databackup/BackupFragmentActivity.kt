package com.musclehack.targetedHypertrophyTraining.databackup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musclehack.targetedHypertrophyTraining.R

class BackupFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        
        // Add the fragment if this is the first creation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BackupFragment())
                .commit()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.online_backup_title)
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }
}