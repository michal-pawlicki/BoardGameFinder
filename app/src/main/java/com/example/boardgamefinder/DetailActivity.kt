package com.example.boardgamefinder

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.caverock.androidsvg.SVG
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt
import java.io.InputStream

class DetailActivity : AppCompatActivity() {
    private lateinit var gameId: String
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var textName: TextView
    private lateinit var textMSRP: TextView
    private lateinit var textPlayers: TextView
    private lateinit var textDescription: TextView
    private lateinit var textYear: TextView
    private lateinit var textRank: TextView
    private lateinit var textStrategy: TextView
    private lateinit var textLearn: TextView
    private lateinit var textPrice: TextView

    private var game: Game = Game()

    companion object {
        const val EXTRA_GAME_ID = "id"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.image)
        textName = findViewById(R.id.name)
        textMSRP = findViewById(R.id.msrp)
        textPlayers = findViewById(R.id.players)
        textDescription = findViewById(R.id.description)
        textYear = findViewById(R.id.year)
        textRank = findViewById(R.id.rank)
        textStrategy = findViewById(R.id.strategy)
        textLearn = findViewById(R.id.learn)
        textPrice = findViewById(R.id.price)


        supportActionBar?.hide()

        val svgImageView: AppCompatImageView = findViewById(R.id.svgBrain)
        val svgResource: InputStream = resources.openRawResource(R.raw.brain)
        val svg: SVG = SVG.getFromInputStream(svgResource)


        val drawable: Drawable = PictureDrawable(svg.renderToPicture())
        svgImageView.setImageDrawable(drawable)

        val svgImageView2: AppCompatImageView = findViewById(R.id.svgChess)
        val svgResource2: InputStream = resources.openRawResource(R.raw.chess)
        val svg2: SVG = SVG.getFromInputStream(svgResource2)
        val drawable2: Drawable = PictureDrawable(svg2.renderToPicture())
        svgImageView2.setImageDrawable(drawable2)

        gameId = intent.getStringExtra(EXTRA_GAME_ID).toString()


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
                val url = URL("https://api.boardgameatlas.com/api/search?ids=$gameId&client_id=LEFoinkgne")
                val connection = url.openConnection() as HttpsURLConnection

                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    val request = Gson().fromJson(inputStreamReader, Request::class.java)
                    inputStreamReader.close()
                    inputSystem.close()
                    game = request.games[0]
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

    @SuppressLint("SetTextI18n")
    private fun viewData() {
        Glide.with(this)
            .load(game.thumb_url)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(imageView)
        textName.text = game.name
        textMSRP.text = "MSRP: " + game.msrp.toString() + " $"
        textPrice.text = "Price: " + game.price.toString() + " $"
        textPlayers.text = "Players: ${game.min_players} - ${game.max_players}"
        textYear.text = "Year published: " + game.year_published.toString()
        textRank.text = "Rank: " + game.rank.toString()
        textLearn.text = game.average_learning_complexity.roundToInt().toString()
        textStrategy.text = game.average_strategy_complexity.roundToInt().toString()
        val document: Document = Jsoup.parse(game.description)
        val parsedText: String = document.text()
        textDescription.text = parsedText
    }


}
