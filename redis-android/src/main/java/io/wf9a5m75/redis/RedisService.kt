package io.wf9a5m75.redis

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RedisService() : JobService() {
  companion object {
    const val REDIS_SERVICE_ID = 1000

    const val TAG = "RedisService"

    var isStarted = false

    fun startRedis(context: Context) {
      if (isStarted) {
        return
      }

      val componentName = ComponentName(context, RedisService::class.java)
      val jobInfo = JobInfo.Builder(REDIS_SERVICE_ID, componentName)
        .build()
      (context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler)
        .run {
          if (schedule(jobInfo) == JobScheduler.RESULT_SUCCESS) {
            Log.e(TAG, "job<redis> scheduled")

            isStarted = true
          } else {
            Log.e(TAG, "unable to schdule job<redis>")
          }
        }
    }

    suspend fun set(key: String, value: String) = withContext(Dispatchers.IO) {
      try {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
          client.set(key, value)
        }

        true
      } catch (e: RuntimeException) {
        e.printStackTrace()

        false
      }
    }

    suspend fun get(key: String) = withContext(Dispatchers.IO) {
      try {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
          client.get(key)
        }
      } catch (e: RuntimeException) {
        e.printStackTrace()

        null
      }
    }

    suspend fun clear(key: String) = withContext(Dispatchers.IO) {
      try {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
          client.getDel(key)
        }

        true
      } catch (e: RuntimeException) {
        e.printStackTrace()

        false
      }
    }

    private fun reset() {
      try {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
          runBlocking {
            client.reset()
          }
        }
      } catch (e: RuntimeException) {
        e.printStackTrace()
      }
    }
    private fun quit() {
      try {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
          runBlocking {
            client.close()
            client.quit()
          }
        }
      } catch (e: RuntimeException) {
        e.printStackTrace()
      }
    }
  }

  override fun onStartJob(params: JobParameters?): Boolean {
    Log.e(TAG, "onStartJob<redis>")

    Thread {
      val configs = Bundle().apply {
        putString("port", "6379")
        putString("protected-mode", "no")  // "no" for demo or development
        putString("requirepass", "")
        putString("loglevel", "verbose")
      }

      val redisAndroid = RedisAndroid()

      redisAndroid.start(this, configs)

      jobFinished(params, false)
    }.start()

    return true
  }

  override fun onStopJob(params: JobParameters?): Boolean {
    Log.e(TAG, "onStopJob<redis>")

    quit()

    return false
  }
}
