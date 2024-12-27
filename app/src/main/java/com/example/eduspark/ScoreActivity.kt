package com.example.eduspark

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eduspark.databinding.ActivityScoreBinding
import com.example.eduspark.databinding.ItemLeaderboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("Score", 0)
        val id = intent.getIntExtra("id", 0)
        val rc = binding.rcLeaderBoard
        val submit = binding.submitBtn
        val inputNick = binding.etNickname

        binding.toHome.setOnClickListener{
            startActivity(Intent(this@ScoreActivity, MainActivity::class.java))
        }
        binding.userScore.text = score.toString()

        getLeaderboard(rc, id)

        submit.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                val conn = URL("http://10.0.2.2:5000/api/leaderboards").openConnection() as HttpURLConnection
                conn.setRequestProperty("Content-Type", "application/json")
                conn.requestMethod = "POST"
                val req = JSONObject().apply {
                    put("gameID", id)
                    put("nickname", inputNick.text.toString())
                    put("totalPoint", score)
                }
                conn.outputStream.write(req.toString().toByteArray())
                val res_code = conn.responseCode
                if (res_code in 200..299){
                    val read = InputStreamReader(conn.inputStream).readText()
                    if (read.isNotEmpty()){
                        withContext(Dispatchers.Main){
                        Toast.makeText(this@ScoreActivity, "Succes Input Data!", Toast.LENGTH_SHORT).show()
                            getLeaderboard(rc, id)
                        }
                    }
                }
            }
        }
    }

    class adapterLeader(val data : JSONArray) : RecyclerView.Adapter<adapterLeader.viewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): viewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = ItemLeaderboardBinding.inflate(inflater, parent, false)
            return viewHolder(view)
        }

        override fun onBindViewHolder(
            holder: viewHolder,
            position: Int
        ) {
            val item = data.getJSONObject(position)
            holder.binding.nickname = item.getString("nickname")
            holder.binding.totalPoint = item.getInt("totalPoint").toString()
            holder.binding.no = (position + 1).toString()
        }

        override fun getItemCount(): Int = data.length()

        inner class viewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)
    }

    fun getLeaderboard(rc : RecyclerView, id : Int){
        CoroutineScope(Dispatchers.IO).launch{
            val conn = URL("http://10.0.2.2:5000/api/leaderboards/$id").openStream().bufferedReader().readText()
            val res = JSONArray(conn)
            withContext(Dispatchers.Main){
                val sort = (0 until res.length()).map { res.getJSONObject(it) }.sortedByDescending { it.getInt("totalPoint") }
                    .let { sortList -> JSONArray(sortList) }
                rc.layoutManager = LinearLayoutManager(this@ScoreActivity)
                rc.adapter = adapterLeader(sort)
            }
        }
    }
}