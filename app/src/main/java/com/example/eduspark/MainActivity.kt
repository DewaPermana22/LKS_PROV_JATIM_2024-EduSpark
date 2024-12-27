package com.example.eduspark

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eduspark.databinding.ActivityMainBinding
import com.example.eduspark.databinding.ItemMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch{
            val conn = URL("http://10.0.2.2:5000/api/games").openStream().bufferedReader().readText()
            val res = JSONArray(conn)
            withContext(Dispatchers.Main){
                binding.rcMenu.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.rcMenu.adapter = adapterHome(res){
                    val intent = Intent(this@MainActivity, GameActivity::class.java)
                    intent.putExtra("id", it.getInt("id"))
                    startActivity(intent)
                }
            }
        }
    }
    class adapterHome(val data : JSONArray, val Click : (JSONObject) -> Unit) : RecyclerView.Adapter<adapterHome.viewHolder>(){
        inner class viewHolder(val binding : ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): adapterHome.viewHolder {
            val view = LayoutInflater.from(parent.context)
            val binding = ItemMenuBinding.inflate(view, parent, false)
            return viewHolder(binding)
        }
        override fun onBindViewHolder(holder: adapterHome.viewHolder, position: Int) {
            val item = data.getJSONObject(position)
            holder.binding.title = item.getString("name")
            holder.binding.category = item.getString("category")
            holder.binding.totalPlayer = item.getInt("totalPlayer").toString()
            holder.itemView.setOnClickListener{
                Click(item)
            }
        }
        override fun getItemCount(): Int {
            return data.length()
        }
    }
}