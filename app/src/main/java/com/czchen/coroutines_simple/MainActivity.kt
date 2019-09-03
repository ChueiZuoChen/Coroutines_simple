package com.czchen.coroutines_simple

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    val PROGRESS_MAX = 100
    val PROGRESS_START = 0
    val PROGRESS_TIME = 3000 //ms
    lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }
            job_progressBar.startJobOrCancel(job)
        }

    }

    fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            println("$job is already active. Cancelling...")
            resetJob()
        } else {
            job_button.setText("Cancel job #1")
            CoroutineScope(IO + job).launch {
                println("coroutine $this is activiated with job $job")

                for (i in PROGRESS_START..PROGRESS_MAX) {
                    delay((PROGRESS_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                GlobalScope.launch(Main) {
                    display_Text.setText("job is complete")
                }
            }
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Restting job"))
        }
        initJob()
    }

    private fun initJob() {
        job_button.setText("Start job #1")
        GlobalScope.launch(Main) {
            display_Text.setText("")
        }
        display_Text.setText("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "Unknow cancellation error."
                }
                println("$job was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        job_progressBar.max = PROGRESS_MAX
        job_progressBar.progress = PROGRESS_START

    }

    fun showToast(message: String) {
        GlobalScope.launch(Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

}
