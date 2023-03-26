package io.wf9a5m75.redis

import android.content.Context
import android.os.Bundle
import android.util.Log
import java.io.File
import java.util.ArrayList
import java.util.Locale

class RedisAndroid  {
  companion object {
    const val TAG = "RedisService"

  }

  init {
    System.loadLibrary("redis")
  }

  private external fun native_redisStart(config: String): Int

  fun start(context: Context, options: Bundle?) {
    val packageName = context.getPackageName()
    var port = "6379"

    options?.let {
      if (options.containsKey("port")) {
        port = "${options.getString("port")}"
      }
    }

    val configs = Bundle()
      .apply {
        // General settings
        putString("bind", "127.0.0.1")
        putString("protected-mode", "yes")
        putString("port", port)

        // WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn
        // is set to the lower value of 128. Instead of 511 (https://github.com/docker-library/redis/issues/35#issuecomment-339973076)
        putString("tcp-backlog", "128")

        putString("tcp-keepalive", "300")
        putString("timeout", "0")
        putString("daemonize", "no")
        putString("supervised", "no")
        putString("pidfile", port + ".pid")
        putString("loglevel", "notice")
        putString("logfile", "")
        // putString("syslog-enabled", "no")
        // putString("syslog-ident", "redis")
        // putString("syslog-facility", "local0")
        putString("databases", "16")
        putString("always-show-logo", "yes")

        // Snapshotting
        putStringArrayList("save", arrayListOf(
          "900 1",
          "300 10",
          "60 10000"
        ))

        putString("stop-writes-on-bgsave-error", "yes")
        putString("rdbcompression", "yes")
        putString("rdbchecksum", "yes")
        putString("dbfilename", port + ".rdb")

        // putString("dir", "./")  <-- Don't set here. The value is specified later in this file.

        // Replication
        // putString("replicaof", "no one")
        // putString("masterauth", "")
        putString("replica-serve-stale-data", "yes")
        putString("replica-read-only", "yes")
        putString("repl-diskless-sync", "no")
        putString("repl-diskless-sync-delay", "5")
        // putString("repl-ping-replica-period", "10")
        // putString("repl-timeout", "60")
        putString("repl-disable-tcp-nodelay", "no")
        // putString("repl-backlog-size", "1mb")
        // putString("repl-backlog-ttl", "3600")
        putString("replica-priority", "100")
        // putString("replica-announce-ip", "5.5.5.5")
        // putString("replica-announce-port", "1234")

        // Security
        putString("requirepass", packageName)

        // Clients
        putString("maxclients", "100")

        // Memory management
        putString("maxmemory", "10mb")
        // putString("maxmemory-policy", "noeviction")
        // putString("maxmemory-samples", "5")
        // putString("replica-ignore-maxmemory", "yes")

        // Lazy freeing
        putString("lazyfree-lazy-eviction", "no")
        putString("lazyfree-lazy-expire", "no")
        putString("lazyfree-lazy-server-del", "no")
        putString("replica-lazy-flush", "no")

        // Append only mode
        putString("appendonly", "yes")  // <--- Should be "yes" by default
        putString("appendfilename", port + ".aof")
        putString("appendfsync", "everysec")
        putString("no-appendfsync-on-rewrite", "no")
        putString("auto-aof-rewrite-percentage", "100")
        putString("auto-aof-rewrite-min-size", "5mb")
        putString("aof-load-truncated", "yes")
        putString("aof-use-rdb-preamble", "yes")

        // Lua scripting
        putString("lua-time-limit", "5000")

        // Redis cluster
        // putString("cluster-enabled", "no")
        // putString("cluster-config-file", "nodes-" + hashCode + ".conf")
        // putString("cluster-node-timeout", "15000")
        // putString("cluster-replica-validity-factor", "10")
        // putString("cluster-migration-barrier", "1")
        // putString("cluster-require-full-coverage", "yes")

        // Cluster docker/nat support
        // .putString("cluster-announce-ip", "10.1.1.5")
        // putString("cluster-announce-port", hashCode + "")
        // putString("cluster-announce-bus-port", (hashCode + 1) + "")

        // Slow log
        putString("slowlog-log-slower-than", "10000")
        putString("slowlog-max-len", "128")

        // Latency monitor
        putString("latency-monitor-threshold", "0")

        // Event notification
        putString("notify-keyspace-events", "")

        // Advanced config
        putString("hash-max-ziplist-entries", "512")
        putString("hash-max-ziplist-value", "64")
        putString("list-max-ziplist-size", "-2")
        putString("list-compress-depth", "0")
        putString("set-max-intset-entries", "512")
        putString("zset-max-ziplist-entries", "128")
        putString("zset-max-ziplist-value", "64")
        putString("hll-sparse-max-bytes", "3000")
        putString("stream-node-max-bytes", "4096")
        putString("stream-node-max-entries", "100")
        putString("activerehashing", "yes")
        putString("activerehashing", "yes")

        putStringArrayList("client-output-buffer-limit", arrayListOf(
          "normal 0 0 0",
          "replica 256mb 64mb 60",
          "pubsub 32mb 8mb 60"
        ))
        // putString("client-query-buffer-limit", "1gb")
        // putString("proto-max-bulk-len", "512mb")

        putString("hz", "10")
        putString("dynamic-hz", "yes")
        putString("aof-rewrite-incremental-fsync", "yes")
        // putString("lfu-log-factor", "10")
        // putString("lfu-decay-time", "1")

        // Active defragmentation
        // putString("activedefrag", "yes")
        // putString("active-defrag-ignore-bytes", "100mb")
        // putString("active-defrag-threshold-lower", "10")
        // putString("active-defrag-threshold-upper", "100")
        // putString("active-defrag-cycle-min", "5")
        // putString("active-defrag-cycle-max", "75")
        // putString("active-defrag-max-scan-fields", "1000")
      }

      val stringBuilder = StringBuilder()
      val stringBuilderDebug = StringBuilder()
      var i = 2
      var value: String? = null

      if (options != null) {
        configs.putAll(options)
      }

      // working directory
      if (configs.containsKey("dir")) {
        val dir = File(configs.getString("dir"))

        if (!dir.exists()) {
          if (!dir.mkdirs()) {
            configs.remove("dir")
          }
        }
      }

      if (!configs.containsKey("dir")) {
        val dir = File(context.getCacheDir().getAbsolutePath() + "/redis/")

        configs.putString("dir", dir.getAbsolutePath())

        if (!dir.exists()) {
          if (!dir.mkdirs()) {
            configs.remove("dir")
          }
        }
      }

      for (key in configs.keySet()) {
        val valueObj = configs.get(key)

        if (valueObj is ArrayList<*>) {
          val values = valueObj as ArrayList<String>

          values.forEach { value ->
            if (value.isNotEmpty()) {
              stringBuilderDebug.append(String.format(Locale.US, "%02d: %s %s\n", i++, key, value))
              stringBuilder.append(String.format(Locale.US, "%s %s\n", key, value))
            }
          }
        } else {
          value = valueObj.toString()

          if (value.isNotEmpty()) {
            stringBuilderDebug.append(String.format(Locale.US, "%02d: %s %s\n", i++, key, value))
            stringBuilder.append(String.format(Locale.US, "%s %s\n", key, value))
          }
        }
      }

      if (configs.getString("loglevel") == "debug" || configs.getString("loglevel") == "verbose") {
        Log.d(TAG, stringBuilderDebug.toString())
      }

      val redisConfig = stringBuilder.toString()

      Log.i(TAG, "--------------------------------")
      Log.i(TAG, "Redis start on port " + configs.getString("port"))
      Log.i(TAG, "--------------------------------")

      if (native_redisStart(redisConfig) == 0) {
        Log.i(TAG, "--------------------------------")
        Log.i(TAG, "Redis exit safely")
        Log.i(TAG, "--------------------------------")
      } else {
        Log.e(TAG, "--------------------------------")
        Log.e(TAG, "Redis abnormally exit")
        Log.e(TAG, "--------------------------------")
      }
    }
}
