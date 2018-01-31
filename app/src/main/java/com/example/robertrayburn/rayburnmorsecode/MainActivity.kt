package com.example.robertrayburn.rayburnmorsecode

import android.app.Activity
import android.content.Context
import android.os.Bundle
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun appendTextAndScroll(text: String){
        if (mTextView != null){
            mTextView.append(text + "\n")
            val layout = mTextView.getLayout()
            if (layout != null) {
                val scrollDelta = (layout!!.getLineBottom(  mTextView.getLineCount() - 1)
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

}
