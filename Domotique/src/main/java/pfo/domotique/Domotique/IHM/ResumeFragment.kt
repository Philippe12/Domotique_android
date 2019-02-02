package pfo.domotique.Domotique.IHM

import android.app.Fragment
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.simple.JSONArray
import pfo.domotique.Domotique.R
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.Exception
import java.net.HttpURLConnection


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ResumeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var timerdate: Timer = Timer("horloge", true)
    private var timerdata: Timer = Timer("updatedata", true)
    private var indata: Int = -1
    private var outdata: Int = -1
    private var presdata: Int = -1

    private fun setindata(value: Int): Int {
        indata = value
        return value
    }

    private fun setoutdata(value: Int): Int {
        outdata = value
        return value
    }

    private fun setpresdata(value: Int): Int {
        presdata = value
        return value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        getProp("outdoorcaptor") { a -> setindata(a)}
        getProp("indoorcaptor") { a -> setoutdata(a)}
        getProp("presurecaptor") { a -> setpresdata(a)}
    }

    fun getProp(prop: String, value: (Int) -> Int) {
        val thread = Thread(
            Runnable {
                try {
                    val url = URL("http://10.0.2.2:9000/getconfig?id=$prop")
                    val urlConnection = url.openConnection() as HttpURLConnection
                    val responseCode = urlConnection.responseCode

                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        val response = urlConnection.inputStream.bufferedReader().readText()

                        val parser = JSONParser()
                        val obj = parser.parse(response) as JSONObject
                        value( obj["value"].toString().toInt() )
                    }
                    urlConnection.disconnect()
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        )
        thread.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resume, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        timerdate.scheduleAtFixedRate( 0, 1000 ) {
            getActivity()?.runOnUiThread {
                val textViewTime: TextView? = view?.findViewById(R.id.textViewTime)
                val formattime = SimpleDateFormat("HH:mm:ss")
                textViewTime?.text = formattime.format(java.util.Calendar.getInstance().getTime())
                val textViewdate: TextView? = view?.findViewById(R.id.textViewDate)
                val formatdate = SimpleDateFormat("EEEE dd MMMM")
                textViewdate?.text = formatdate.format(java.util.Calendar.getInstance().getTime())
            }
        }
        timerdata.scheduleAtFixedRate( 0, 1000 ) {
            val tread = Thread(
                Runnable {
                    try {
                        val url = URL("http://10.0.2.2:9000/room")
                        val urlConnection = url.openConnection() as HttpURLConnection
                        //urlConnection.connect()
                        val responseCode = urlConnection.responseCode

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = urlConnection.inputStream.bufferedReader().readText()

                            val parser = JSONParser()
                            val obj = parser.parse(response) as JSONObject
                            val rooms = obj.get("Rooms") as JSONArray
                            setDisplayData(rooms, R.id.textExTemp, "Temperature", outdata)
                            setDisplayData(rooms, R.id.textExHum, "Humidity", outdata)
                            setDisplayData(rooms, R.id.textIntTemp, "Temperature", indata)
                            setDisplayData(rooms, R.id.textIntHum, "Humidity", indata)
                        }
                        urlConnection.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
            tread.start()
        }
    }

    fun setDisplayData(list: JSONArray, viewid: Int, data: String, dataid: Int ) {
        for (id in 0 until list.size) {
            val room = list[id] as JSONObject
            if (room.get("Id").toString().toInt() == dataid) {
                getActivity()?.runOnUiThread {
                    var textView: TextView? = view?.findViewById(viewid)
                    var unit = room[data] as JSONObject
                    var value = unit.get("Inst").toString().toFloat()
                    textView?.text = "$value??"
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        timerdate.cancel();
        timerdata.cancel();
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResumeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
