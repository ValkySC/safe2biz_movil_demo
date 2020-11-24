package pe.dominiotech.movil.safe2biz.utils

import android.app.AlertDialog
import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject
import pe.dominiotech.movil.safe2biz.R

fun mapObject(obj: Any): Map<String, String> {
    val jsonObject = JSONObject(Gson().toJson(obj))
    val fieldsMap = java.util.HashMap<String, String>()
    for (key in jsonObject.keys()) {
        fieldsMap[key] = jsonObject.get(key).toString()
    }
    return fieldsMap
}

fun createDialog(context: Context, msg: String): AlertDialog {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(false)
    builder.setView(R.layout.layout_loading_dialog)
    builder.setMessage(msg)
    return builder.create()
}
