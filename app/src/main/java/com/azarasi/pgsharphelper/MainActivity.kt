package com.azarasi.pgsharphelper

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azarasi.pgsharphelper.databinding.ActivityMainBinding
import com.azarasi.pgsharphelper.models.Route
import com.azarasi.pgsharphelper.utils.PackageInfoCompat.getCVersionCode
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.topjohnwu.superuser.Shell

class MainActivity : AppCompatActivity(), RouteListAdapter.Listener {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Shell.isAppGrantedRoot() == false) {
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle("起動させないよ")
                .setMessage("Root権限を付与してください！")
                .setPositiveButton("閉じる") { _, _ ->
                    finish()
                }
                .show()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.toastMsg.observe(this) {
            it?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMsg()
            }
        }

        viewModel.loadPGOAndRoutes()

        viewModel.pgo.observe(this) { packageInfo: PackageInfo? ->
            binding.proLoading.visibility = View.GONE
            if (packageInfo == null) {
                showNotInstalledState()
            } else {
                showInstalledPGOState(packageInfo)
            }
        }

        val routeListAdapter = RouteListAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        val itemDecoration = MaterialDividerItemDecoration(this, layoutManager.orientation)
        itemDecoration.isLastItemDecorated = false
        val rvFavoriteRoute: RecyclerView = findViewById(R.id.rvFavoriteRoute)
        rvFavoriteRoute.layoutManager = layoutManager
        rvFavoriteRoute.adapter = routeListAdapter
        rvFavoriteRoute.addItemDecoration(itemDecoration)

        viewModel.routes.observe(this) { routes ->
            routeListAdapter.routes = routes
        }
    }

    private fun showNotInstalledState() {
        binding.tvNotInstalled.visibility = View.VISIBLE
        Glide.with(this)
            .load(android.R.mipmap.sym_def_app_icon)
            .into(binding.ivPGOIcon)
        binding.tvPGOName.text = null
        binding.tvPGOPackageName.text = null
        binding.tvPGOVersion.text = null
    }

    private fun showInstalledPGOState(packageInfo: PackageInfo) {
        val appInfo = packageInfo.applicationInfo
        binding.tvNotInstalled.visibility = View.GONE
        Glide.with(this)
            .load(packageInfo)
            .error(android.R.mipmap.sym_def_app_icon)
            .into(binding.ivPGOIcon)
        binding.tvPGOName.text = appInfo.loadLabel(packageManager)
        binding.tvPGOPackageName.text = packageInfo.packageName
        val versionText = getString(
            R.string.version_format,
            packageInfo.versionName,
            packageInfo.getCVersionCode()
        )
        binding.tvPGOVersion.text = HtmlCompat.fromHtml(
            versionText,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private val saveLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/gpx+xml")
        ) { uri ->
            uri?.let { safeUri ->
                viewModel.saveRouteAsGpx(safeUri)
            } ?: run {
                viewModel.saveRoute = null
            }
        }

    override fun saveRouteAsGpx(route: Route) {
        if (viewModel.saveRoute == null) {
            viewModel.saveRoute = route
            saveLauncher.launch("${route.name}.gpx")
        }
    }
}