package com.example.boardgamefinder

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class ListActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_GAME_NAME = "game_name"
    }

    private lateinit var queryText: TextView
    private var gameName: String = "Catan"
    private var games: Array<Game> = arrayOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        gameName = intent.getStringExtra(EXTRA_GAME_NAME).toString()
        queryText = findViewById<TextView?>(R.id.text).apply {
            text = gameName
        }

        supportActionBar?.hide()
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager


        val adapter = GameAdapter(games.toList())
        recyclerView.adapter = adapter

        val cd = GamesDownloader()
        cd.execute()
    }


    @Suppress("DEPRECATION")
    private inner class GamesDownloader: AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressBar.visibility = View.GONE
            viewData()
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                val url = URL("https://api.boardgameatlas.com/api/search?name=$gameName&client_id=LEFoinkgne")
                val connection = url.openConnection() as HttpsURLConnection

                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    val request = Gson().fromJson(inputStreamReader, Request::class.java)
                    inputStreamReader.close()
                    inputSystem.close()
                    games = request.games
                }else{
                    queryText.text = "Error"
                }
            } catch (e: MalformedURLException){
                return "Zły URL"
            } catch (e: FileNotFoundException){
                return "Brak pliku"
            } catch (e: IOException){
                return "Wyjątek IO"
            }
            return "Success"
        }
    }

    private fun viewData() {
        val adapter = GameAdapter(games.toList())
        recyclerView.adapter = adapter
    }


}