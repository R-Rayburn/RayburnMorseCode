package com.example.robertrayburn.rayburnmorsecode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {

    private val text_dictionary : HashMap<String,String> = HashMap<String,String>()
    private val morse_dictionary : HashMap<String,String> = HashMap<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // Needed for scrolling.
        mTextView.movementMethod = ScrollingMovementMethod()

        // Wires on button.
        testButton.setOnClickListener { _ ->
            mTextView.text = ""
            appendTextAndScroll(inputText.text.toString().toUpperCase())
            hideKeyboard()
        }

        // Loads JSON file into JSON_OBJECT
        val jsonObj = loadMorseJSONFile();

        // Stores key->value and value->key dictionary mappings.
        buildDictsWithJSON(jsonObj)

        // Wires on button.
        codesButton.setOnClickListener{ _ ->
            mTextView.text = ""
            // Prints the codes to screen.
            showCodes()
            hideKeyboard()
        }

        transButton.setOnClickListener { _ ->
            mTextView.text = ""
            val input = inputText.text.toString()

            appendTextAndScroll(input.toUpperCase())

            // Regex for exclusive Morse Code: [\.-]{1,5}(?> [\.-]{1,5})*(?> / [\.-]{1,5}(?> [\.-]{1,5})*)*
            if (input.matches("(\\.|-|/|\\s)+".toRegex())) { //Old Regex: (\.|-|\s/\s|\s)+
                val transMorse = translateMorse(input)
                appendTextAndScroll(transMorse.toUpperCase())
            }
            else {
                val transText = translateText(input)
                appendTextAndScroll(transText)
            }
            hideKeyboard()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun appendTextAndScroll(text: String){
        if (mTextView != null){
            mTextView.append(text + "\n")
            val layout = mTextView.getLayout()
            if (layout != null) {
                val scrollDelta = (layout.getLineBottom(  mTextView.getLineCount() - 1)
                        - mTextView.getScrollY() - mTextView.getHeight())
                if (scrollDelta > 0)
                    mTextView.scrollBy( 0, scrollDelta)
            }

        }
    }

    fun Activity.hideKeyboard(){
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    fun Context.hideKeyboard(view: View){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadMorseJSONFile() : JSONObject {

        val filePath = "morse.json"

        val jsonStr = application.assets.open(filePath).bufferedReader().use { it.readText() }

        val jsonObj = JSONObject(jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf("}") + 1))

        return jsonObj

    }

    private fun buildDictsWithJSON(jsonObj : JSONObject) {
        for ( key in jsonObj.keys() ) {
            val code : String = jsonObj[key] as String

            text_dictionary.put(key,code)

            morse_dictionary.put(code,key)

            Log.d("log", "$key: $code")

        }
    }

    private fun showCodes() {
        appendTextAndScroll("HERE ARE THE CODES")
        for (key in text_dictionary.keys.sorted()){
            appendTextAndScroll("${key.toUpperCase()}: ${text_dictionary[key]}")
        }
    }

    private fun translateText(input : String) : String {
        var r = ""
        val s = input.toLowerCase()
        for (c in s) {
            if (c == ' ') r += "/ "
            else if (text_dictionary.containsKey(c.toString())) r += "${text_dictionary[c.toString()]} "
            else r += "? "
        }

        Log.d("log", "Morse: $r")

        return r

    }

    fun isMorse(s: String) :Boolean {
        for (c in s)
            if (c != ' ' && c != '-' && c != '.' && c != '/')
                return false
        return true
    }

    private fun translateMorse(input: String) : String {
        var r = ""
        val s = input.split("(\\s)+".toRegex())
        Log.d("log", "Split stirng: $s")
        for (item in s) {
            if (item == "/") r += " "
            else if (item == "") r += ""
            else if (morse_dictionary.containsKey(item)) r += morse_dictionary[item]
            else r += 248.toChar().toString() //The value 248 is the lowercase value of the empty set symbol. "[NA]"
        }

        Log.d("log", "Text: $r")

        return r
    }

    fun playString(s:String, i: Int = 0) : Unit {
        if (i>s.length-1)
            return;
        var mDelay: Long = 0;

        var thenFun: () -> Unit = { ->
            this@MainActivity.runOnUiThread(java.lang.Runnable {playString(s, i+1)})
        }

        var c = s[i]
        Log.d("Log", "Processing pos: " + i + " char: [" + c + "]")
        if (c=='.')
            playDot(thenFun)
        else if (c=='-')
            playDash(thenFun)
        else if (c=='/')
            pause(6*dotLength, thenFun)
        else if (c==' ')
            pause(2*dotLength, thenFun)
    }

    val dotLength:Int = 50
    val dashLength:Int = dotLength*3

    val dotSoundBuffer:ShortArray = genSineWaveSoundBuffer(550.0, dotLength)
    val dashSoundBuffer:ShortArray = genSineWaveSoundBuffer(550.0, dashLength)

    fun playDash(onDone:()->Unit={}){
        Log.d("DEBUG", "playDash")
        playSoundBuffer(dashSoundBuffer,{->pause(dotLength, onDone)})
    }
    fun playDot(onDone: () -> Unit={}){
        Log.d("DEBUG", "playDot")
        playSoundBuffer(dotSoundBuffer,{ -> pause(dotLength, onDone)})
    }

    fun pause(durationMSec:Int, onDone: () -> Unit={}){
        Log.d("DEBUG", "pause: ${durationMSec}")
        Timer().schedule(timerTask { onDone()  }, durationMSec.toLong())
    }

    private fun genSineWaveSoundBuffer(frequency:Double, durationMSec: Int):ShortArray{
        val duration : Int = round((durationMSec/1000.0) * SAMPLE_RATE).toInt()

        var mSound: Double
        val mBuffer = ShortArray(duration)
        for(i in 0 until duration) {
            mSound= Math.sin(2.0*Math.PI*i.toDouble()/(SAMPLE_RATE/frequency))
            mBuffer[i] = (mSound*java.lang.Short.MAX_VALUE).toShort()
        }
        return mBuffer
    }

    private fun playSoundBuffer

}
