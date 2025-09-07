package com.musclehack.targetedHypertrophyTraining

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.musclehack.targetedHypertrophyTraining.about.AboutFragmentActivity
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.databackup.BackupFragmentActivity
import com.musclehack.targetedHypertrophyTraining.more.ContributeFragmentActivity
import com.musclehack.targetedHypertrophyTraining.preferences.PreferenceActivity
import com.musclehack.targetedHypertrophyTraining.testimonials.TestimonialsFragmentActivity
import com.musclehack.targetedHypertrophyTraining.utilities.ContextErrorEvent
import com.musclehack.targetedHypertrophyTraining.utilities.EXTRA_BLOG_LINK
import com.musclehack.targetedHypertrophyTraining.utilities.EXTRA_IS_TIMER_INTENT
import com.musclehack.targetedHypertrophyTraining.utilities.InternetConnectionErrorEvent
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_WORKOUT_TRACKER_STATE
import com.musclehack.targetedHypertrophyTraining.workoutTracker.SaveErrorEvent
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.TrackerHomeFragmentDirections
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.TrackerWorkoutsFragmentDirections
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), BottomNavigationActivity {
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private val _fabClickedEvent: MutableLiveData<Event<Int>> = MutableLiveData()
    val fabClickedEvent: LiveData<Event<Int>> = _fabClickedEvent
    private val _backPressedEvent = MutableLiveData<Event<Boolean>>()
    val backPressedEvent: LiveData<Event<Boolean>> = _backPressedEvent
    private lateinit var timerReceiver: TimerReceiver

    @Inject
    lateinit var trackerRepository: TrackerRepository

    @Inject
    lateinit var preferences: SharedPreferences
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                val toast = Toast.makeText(
                    this,
                    String.format(getString(R.string.no_notification_permission_error)),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.TOP, 0, 250)
                toast.show()
            }
        }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupFab()
        timerReceiver = TimerReceiver()
        setupBottomNavigationBar()
        setupNavigationDrawer()
        setupBlogNotificationClickHandler()
        checkForTimerIntent()
    }

    private fun setupBlogNotificationClickHandler() {
        val link = intent.getStringExtra(EXTRA_BLOG_LINK)
        if (link != null) {
            intent.removeExtra(EXTRA_BLOG_LINK)
            val uri = Uri.parse(link)
            if (uri != null && Patterns.WEB_URL.matcher(link).matches()) {
                preferences.edit().putString(EXTRA_BLOG_LINK, link).commit()
                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
                bottomNavigationView.selectedItemId = R.id.blog_nav_graph
            }
        }
    }

    /** Check to see if this came from a timer. */
    private fun checkForTimerIntent() {
        val isTimerIntent = intent.getBooleanExtra(EXTRA_IS_TIMER_INTENT, false)
        if (isTimerIntent) {
            val workoutState = preferences.getString(PREF_WORKOUT_TRACKER_STATE, "")
            if (workoutState.isNullOrEmpty()) {
                return
            }
            val ids = workoutState.split("-")
            if (ids.count() < 2) {
                return
            }
            val cId = ids[0]
            val wId = ids[1]
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            bottomNavigationView.selectedItemId = R.id.tracker_nav_graph
            val action = TrackerHomeFragmentDirections
                .actionTrackerHomeFragmentToTrackerWorkoutsFragmentDest(cId.toLong())
            val action2 = TrackerWorkoutsFragmentDirections
                .actionTrackerWorkoutsFragmentDestToTrainingPagerFragment(
                    cycleId = cId.toLong(),
                    workoutId = wId.toLong()
                )
            currentNavController?.value?.navigate(action)
            currentNavController?.value?.navigate(action2)
        }
    }

    override fun onStart() {
        super.onStart()
        trackerRepository.addTimerChangeSource(timerReceiver.getData())
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(timerReceiver, IntentFilter(TimerReceiver.TIMER_CHANGE_ACTION))
    }

    override fun onStop() {
        super.onStop()
        trackerRepository.removeTimerChangeSource(timerReceiver.getData())
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(timerReceiver)
    }

    private fun setupFab() {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            _fabClickedEvent.value = Event(0)
        }
    }

    /** Called to clear our Floating Action Button */
    fun clearFab() {
        findViewById<FloatingActionButton>(R.id.fab)?.visibility = View.GONE
    }

    fun onSaveError(event: SaveErrorEvent) {
        val toast = Toast.makeText(
            this,
            String.format(getString(R.string.save_error_prompt)),
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 150)
        toast.show()
    }

    fun onContextError(event: ContextErrorEvent) {
        val toast = Toast.makeText(
            this,
            String.format(getString(R.string.save_error_prompt)),
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 150)
        toast.show()
    }

    fun onInternetConnectionError(event: InternetConnectionErrorEvent) {
        val toast = Toast.makeText(
            this,
            String.format(getString(R.string.internet_connection_error)),
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 0, 250)
        toast.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun launchNotificationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        /*
         shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                //            showInContextUI(...)
            }
         */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /** We only open the drawer if there is nothing to navigate up to (i.e. we are at a base
         * screen) */
        if (currentNavController?.value?.previousBackStackEntry == null &&
            actionBarDrawerToggle.onOptionsItemSelected(item)
        ) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun forceBottomNavigationToAppear() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        val layoutParams = bottomNavigationView.layoutParams as CoordinatorLayout.LayoutParams
        val bottomNavigationBehavior = layoutParams.behavior as BottomNavigationBehavior
        bottomNavigationBehavior.slideUp(bottomNavigationView)
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        val navGraphIds = listOf(
            R.navigation.tracker_nav_graph,
            R.navigation.book_nav_graph,
            R.navigation.premium_nav_graph,
            R.navigation.blog_nav_graph,
            R.navigation.exercise_bank_nav_graph
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
            setupActionBarWithNavController(navController, drawerLayout)
        })
        currentNavController = controller
    }

    private fun setupNavigationDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        val drawerView = findViewById<NavigationView>(R.id.nav_drawer_view)
        drawerView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.testimonials_option -> {
                    startActivity(Intent(this, TestimonialsFragmentActivity::class.java))
                }

                R.id.backup_option -> {
                    startActivity(Intent(this, BackupFragmentActivity::class.java))
                }

                R.id.settings_option -> {
                    startActivity(Intent(this, PreferenceActivity::class.java))
                }

                R.id.about_option -> {
                    startActivity(Intent(this, AboutFragmentActivity::class.java))
                }

                R.id.contribute_option -> {
                    startActivity(Intent(this, ContributeFragmentActivity::class.java))
                }
            }
            drawerLayout.close()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val result = currentNavController?.value?.navigateUp() ?: super.onSupportNavigateUp()
        /** Overriding this so fragments can know if back pressed event has occurred. */
        _backPressedEvent.value = Event(true)
        return result
    }
}