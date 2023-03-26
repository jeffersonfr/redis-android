package example.redis.android.wf9a5m75.exampleapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.wf9a5m75.redis.RedisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    RedisService.startRedis(this)

    GlobalScope.launch(Dispatchers.IO) {
      delay(2000)
      Log.e("MyLog", "set value[foo]: ${RedisService.set("foo", "21")}")
      Log.e("MuLog", "get value[foo]: ${RedisService.get("foo")}")
    }
  }

}
