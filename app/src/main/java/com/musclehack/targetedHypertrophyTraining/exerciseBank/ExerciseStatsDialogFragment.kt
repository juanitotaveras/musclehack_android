package com.musclehack.targetedHypertrophyTraining.exerciseBank

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.musclehack.targetedHypertrophyTraining.R
import com.musclehack.targetedHypertrophyTraining.utilities.KEY_EXERCISE_NAME
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_INSTALL_DATE
import java.util.*

/**
 * Created by juanito on 3/13/2018.
 */
class ExerciseStatsDialogFragment : androidx.fragment.app.DialogFragment(),
    DialogInterface.OnClickListener {
    companion object {
        fun newInstance(exerciseName: String): ExerciseStatsDialogFragment {
            val bundle = Bundle()
            bundle.putString(KEY_EXERCISE_NAME, exerciseName)
            val frag = ExerciseStatsDialogFragment()
            frag.arguments = bundle
            return frag
        }
    }

    private var form: View? = null
    private var graph: GraphView? = null
    private var infoText: TextView? = null

    private var progressBar: ProgressBar? = null

    //    private var anyChart: AnyChartView? = null
    private val exerciseName: String
        get() = arguments!!.getString(KEY_EXERCISE_NAME)!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        form = requireActivity().layoutInflater
            .inflate(R.layout.dialog_exercise_stats, null)
        progressBar = form!!.findViewById(R.id.exerciseStatsProgress)
        graph = form!!.findViewById(R.id.graph)
        infoText = form!!.findViewById(R.id.noDataRecordedInfo)


        val builder = AlertDialog.Builder(activity)

        val deleteExerciseHeader = String.format(
            getString(R.string.exercise_stats_dialog_header), exerciseName
        )
        LoadExerciseStats().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, exerciseName)
//        val deleteMessage = "Are you sure you want to delete this set? All recorded" + "exercises will be deleted."
        return builder.setTitle(deleteExerciseHeader).setView(form)
            .setPositiveButton(getString(R.string.close), this)
            .create()
        //.setNegativeButton(android.R.string.cancel, null)*/
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        dismiss()
    }

    private class CompareDates {
        companion object : Comparator<WeightStat> {
            override fun compare(a: WeightStat, b: WeightStat): Int {
                val aDate = Date(a.date)
                val bDate = Date(b.date)
                return when {
                    aDate == bDate -> 0
                    aDate.after(bDate) -> 1
                    else -> -1
                }
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private inner class LoadExerciseStats : AsyncTask<String, Void, Void?>() {
        var weightStats: ArrayList<WeightStat>? = null
        override fun doInBackground(vararg params: String?): Void? {
            val exerciseName = params[0]
//            weightStats = DatabaseHelper.getInstance(requireActivity().applicationContext)
//                    .makeWeightStats(exerciseName!!, 100)
            weightStats!!.sortWith(CompareDates)

//            })
//            weightStats!!.sortWith(kotlin.Comparator(function =comp(x, y)))
            return null
        }

        override fun onPostExecute(unused: Void?) {
//            = setsLoadedIntoModel(threadSets!!)
//            val progress =
            // date on x-axis, weight on y-axis
            progressBar!!.visibility = View.GONE
            // app install date should be default, if date == 0
            val defaultDate: Long = 1520955242086 // March 13
//            Log.e("tag", Date().time.toString())
            val installDate = Date(
                PreferenceManager
                    .getDefaultSharedPreferences(requireActivity().applicationContext)
                    .getLong(PREF_INSTALL_DATE, defaultDate)
            ) // Mar 19, 2018 is default
            val set = ArrayList<DataPoint>()
//            val oneDay = 86400000L
//            val testWeightStats = arrayListOf(
//                    WeightStat(defaultDate, 200.0),
//                    WeightStat(defaultDate+1*oneDay, 205.0),
//                    WeightStat(defaultDate+2*oneDay, 210.0),
//                    WeightStat(defaultDate+3*oneDay, 215.0),
//                    WeightStat(defaultDate+4*oneDay, 220.0),
//                    WeightStat(defaultDate+5*oneDay, 225.0),
//                    WeightStat(defaultDate+6*oneDay, 230.0),
//                    WeightStat(defaultDate+7*oneDay, 230.0),
//                    WeightStat(defaultDate+8*oneDay, 235.0),
//                    WeightStat(defaultDate+9*oneDay, 240.0)
//            )
            for (stat in weightStats!!) {
                val date: Date = if (stat.date == 0L) installDate else Date(stat.date)
//                Log.e(javaClass.name, "set x: ${date} y: ${stat.weight}")

                set.add(DataPoint(date, stat.weight))
            }
            graph!!.gridLabelRenderer.labelFormatter =
                DateAsXAxisLabelFormatter(requireActivity().applicationContext)
//            graph!!.gridLabelRenderer.numHorizontalLabels = 4 // only 4 because of the space
            val series = set.toTypedArray()
//            series.sortBy(DataPoint)
//            for (s in set) {
//                Log.e(javaClass.name, "set x: ${s.x} y: ${s.y}")
//
//            }
//            for (s in series) {
//                Log.e(javaClass.name, "set x: ${s.x} y: ${s.y}")
//                val p = DataPoint(Date(), 49.0)
//            }
            if (series.isEmpty()) {
                // make graph invisible
                graph!!.visibility = View.GONE
                infoText!!.visibility = View.VISIBLE
            } else {
                val finalSeries = LineGraphSeries<DataPoint>(series)
                finalSeries.isDrawDataPoints = true
                graph!!.addSeries(finalSeries)
                graph!!.gridLabelRenderer.verticalAxisTitle =
                    getString(R.string.weight_in_pounds_label)
                graph!!.gridLabelRenderer.horizontalAxisTitle = getString(R.string.date_label)
                graph!!.isHorizontalScrollBarEnabled = true
//                graph!!.gridLabelRenderer.numHorizontalLabels = 2
                graph!!.canScrollHorizontally(1)
            }

        }
    }
}