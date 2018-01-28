package com.example.robertrayburn.rayburnmorsecode

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    val text_dictionary : HashMap<String,String> = HashMap<String,String>();
    val morse_dictionary : HashMap<String,String> = HashMap<String,String>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // Needed for scrolling.
        mTextView.movementMethod = ScrollingMovementMethod();

        // Need to see if this keeps the screen from scrolling when using the keyboard.
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Wires on button.
        testButton.setOnClickListener { _ ->
            mTextView.text = ""
            appendTextAndScroll(inputText.text.toString());
            hideKeyboard();
        }

        loadFile();

        codesButton.setOnClickListener{ _ ->
            mTextView.text = ""
            for (key in text_dictionary.keys.sorted()) {
                appendTextAndScroll(key.toUpperCase() + ": " + text_dictionary[key]);
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun appendTextAndScroll(text: String){
        if (mTextView != null){
            mTextView.append(text + "\n");
            val layout = mTextView.getLayout();
            if (layout != null) {
                val scrollDelta = (layout!!.getLineBottom(  mTextView.getLineCount() - 1)
                        - mTextView.getScrollY() - mTextView.getHeight());
                if (scrollDelta > 0)
                    mTextView.scrollBy( 0, scrollDelta);
            }

        }
    }

    fun Activity.hideKeyboard(){
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus);
    }

    fun Context.hideKeyboard(view: View){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0);
    }

    fun loadFile() {
        // for loading json file into string.
        //
        val jsonString: String = application.assets.open("morse.json").bufferedReader().use{
            it.readText()
        }

        // Converting json stirng into json object
        val json_object = JSONObject(jsonString)

        // Storing key/value pairs into hashmaps.
        // https://stackoverflow.com/questions/9151619/how-to-iterate-over-a-jsonobject
        for (key in json_object.keys()) {
            text_dictionary.put(key,json_object.getString(key))
            morse_dictionary.put(json_object.getString(key),key)
        }
    }

}
