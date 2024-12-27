package com.example.eduspark

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eduspark.databinding.ActivityGameBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = mutableListOf<Pair<String, String>>()
        var index = 0
        var CorrectAnswer : Int = 0

        val btnPrev = binding.prev
        val btnNext = binding.next
        val input = binding.etAnswer

        val idGame = intent.getIntExtra("id", 0)
        if (index == 0){
            btnPrev.isEnabled = false
            btnPrev.setBackgroundResource(R.drawable.disabled_buton)
        }

        CoroutineScope(Dispatchers.IO).launch{
            val conn = URL("http://10.0.2.2:5000/api/words/$idGame").openStream().bufferedReader().readText()
            val item = JSONArray(conn)

            for (i in 0 until item.length()){
                val dataWord = item.getJSONObject(i)
                val word = dataWord.getString("word")
                val nameImage = dataWord.getString("image")
                data.add(Pair(word,nameImage))
            }

            withContext(Dispatchers.Main){
                if (data.isNotEmpty()){
                    val (words, nameImage) = data[index]
                    binding.nameRandom.text = randomWord(words)
                    withContext(Dispatchers.IO){
                        val urlImg = URL("http://10.0.2.2:5000/images/$nameImage").openStream()
                        val dataImg = BitmapFactory.decodeStream(urlImg)
                        withContext(Dispatchers.Main){
                        binding.imageView.setImageBitmap(dataImg)
                        }
                    }
                }
            }
        }

        btnNext.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch{
                val (words,_) = data[index]
                if (input.text.toString().trim().equals(words, ignoreCase = true)){
                    CorrectAnswer++
                }
                if (index < data.size -1){
                    index++
                    if (data.isNotEmpty()){
                        val (words, nameImage) = data[index]
                        binding.nameRandom.text = randomWord(words)
                        withContext(Dispatchers.IO){
                            val urlImg = URL("http://10.0.2.2:5000/images/$nameImage").openStream()
                            val dataImg = BitmapFactory.decodeStream(urlImg)
                            withContext(Dispatchers.Main){
                                binding.imageView.setImageBitmap(dataImg)
                            }
                        }
                    }
                    btnPrev.isEnabled = true
                    btnPrev.setBackgroundResource(R.drawable.bg_button)
                    if (index == data.size -1){
                        btnNext.setText(R.string.finish)
                        btnNext.setOnClickListener{
                            val intent = Intent(this@GameActivity, ScoreActivity::class.java)
                            if ( CorrectAnswer  > 3){
                                CorrectAnswer += 1
                            }
                            val score = CorrectAnswer * 10
                            intent.putExtra("Score", score)
                            intent.putExtra("id", idGame)
                            startActivity(intent)
                        }
                    }
                    input.setText("")
                }
            }
        }

        btnPrev.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch{
                if (index > 0){
                    index--
                    if (data.isNotEmpty()){
                        val (words, nameImage) = data[index]
                        binding.nameRandom.text = randomWord(words)
                        withContext(Dispatchers.IO){
                            val urlImg = URL("http://10.0.2.2:5000/images/$nameImage").openStream()
                            val dataImg = BitmapFactory.decodeStream(urlImg)
                            withContext(Dispatchers.Main){
                                binding.imageView.setImageBitmap(dataImg)
                            }
                        }
                    }
                    if (index == 0){
                        btnPrev.isEnabled = false
                        btnPrev.setBackgroundResource(R.drawable.disabled_buton)
                    }
                    btnNext.setText(R.string.next)
                }
            }
        }
    }

    fun randomWord(char : String) : String{
        return char.toCharArray().apply { shuffle() }.concatToString()
    }
}