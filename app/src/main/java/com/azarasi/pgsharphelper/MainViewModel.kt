package com.azarasi.pgsharphelper

import android.app.Application
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.azarasi.pgsharphelper.models.Route
import com.azarasi.pgsharphelper.utils.FileUtils.getDataFile
import com.azarasi.pgsharphelper.utils.PackageManagerCompat.getCInstalledPackages
import com.azarasi.pgsharphelper.utils.SPParser
import com.azarasi.pgsharphelper.utils.UserHandleHidden.getUserId
import com.topjohnwu.superuser.io.SuFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private var _pgo: MutableLiveData<PackageInfo?> = MutableLiveData()
    val pgo: LiveData<PackageInfo?> = _pgo

    private var _routes: MutableLiveData<List<Route>> = MutableLiveData()
    val routes: LiveData<List<Route>> = _routes

    var saveRoute: Route? = null

    private var _toastMsg: MutableLiveData<String?> = MutableLiveData()
    val toastMsg: LiveData<String?> = _toastMsg

    fun clearToastMsg() {
        _toastMsg.value = null
    }

    fun loadPGOAndRoutes() {
        viewModelScope.launch(Dispatchers.Default) {
            val pgo = app.packageManager.getCInstalledPackages(0)
                .firstOrNull { it.packageName == "com.nianticlabs.pokemongo" }
            _pgo.postValue(pgo)

            if (pgo == null) {
                _toastMsg.postValue("pokemon goが見つかりません！")
            } else {
                withContext(Dispatchers.IO) {
                    try {
                        _routes.postValue(emptyList())
                        val pgSharpPrefs = SuFile(
                            File(
                                getDataFile(
                                    pgo.packageName, getUserId()
                                ), "shared_prefs"
                            ), "com.google.android.gms.chimera.hl2.xml"
                        )
                        val xmlText = pgSharpPrefs.newInputStream().bufferedReader().readText()
                        val prefs = SPParser.parseXmlText(xmlText)
                        val hlfavorRoute = prefs["hlfavorRoute"] ?: run {
                            _toastMsg.postValue("お気に入り登録されたルートがありません！")
                            return@withContext
                        }
                        val favoriteRoutes = JSONArray(hlfavorRoute.toString())
                        val routes: ArrayList<Route> = ArrayList()
                        for (i in 0 until favoriteRoutes.length()) {
                            val route = favoriteRoutes.getJSONObject(i)
                            val routeName = route.getString("name")
                            val routePoints = route.getJSONArray("points")
                            val points = (0 until routePoints.length()).map { pointIndex ->
                                val point = routePoints.getJSONArray(pointIndex)
                                val latitude = point.optDouble(0)
                                val longitude = point.optDouble(1)
                                Route.LatLng(latitude, longitude)
                            }
                            routes.add(Route(routeName, points))
                        }
                        _routes.postValue(routes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _toastMsg.postValue("ルートの取得に失敗しました！")
                    }
                }
            }
        }
    }

    fun saveRouteAsGpx(uri: Uri) {
        if (saveRoute == null) {
            return
        }
        viewModelScope.launch {
            val gpxText = buildString {
                appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                appendLine("<gpx version=\"1.1\" creator=\"PGSharp Route\">")
                saveRoute?.points?.forEach { point ->
                    appendLine("    <wpt lat=\"${point.latitude}\" lon=\"${point.longitude}\"></wpt>")
                }
                appendLine("</gpx>")
            }
            try {
                withContext(Dispatchers.IO) {
                    app.contentResolver.openOutputStream(uri, "w")?.bufferedWriter()
                        ?.use { writer ->
                            writer.write(gpxText)
                        }
                }
                _toastMsg.postValue("gpxで保存しました！")
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMsg.postValue(e.message)
            } finally {
                saveRoute = null
            }
        }
    }
}