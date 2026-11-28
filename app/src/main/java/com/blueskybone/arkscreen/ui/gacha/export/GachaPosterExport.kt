package com.blueskybone.arkscreen.ui.gacha.export

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.DialogOperatorPosterPreviewBinding
import com.blueskybone.arkscreen.ui.common.view.StarterLoadingView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.toast.Toaster
import kotlinx.coroutines.*
import java.io.File
import java.io.OutputStream

/** Owns the image export lifecycle separately from record import/export. */
class GachaPosterExport(private val activity: AppCompatActivity, private val setBusy: (Boolean) -> Unit) {
    private var job: Job? = null
    private var pendingSave: Bitmap? = null
    private val saveLauncher = activity.registerForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val bitmap = pendingSave
        pendingSave = null
        if (uri != null && bitmap != null) activity.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val output = activity.contentResolver.openOutputStream(uri)
                        ?: error(activity.getString(R.string.error_export_file_create))
                    output.use { encode(bitmap, it) }
                }
                Toaster.show(activity.getString(R.string.operation_export_completed))
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                Toaster.show(activity.getString(R.string.operation_export_failed, error.message))
            }
        }
    }

    fun generate(poster: GachaPoster) {
        if (job?.isActive == true) return
        val loading = activity.layoutInflater.inflate(R.layout.dialog_loading, null) as StarterLoadingView
        loading.setMessage(R.string.operator_poster_preparing)
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.gacha_share_image).setView(loading)
            .setNegativeButton(R.string.cancel) { _, _ -> job?.cancel() }
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnCancelListener { job?.cancel() }
        dialog.show()
        setBusy(true)
        job = activity.lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    GachaPosterRenderer(activity.applicationContext).render(poster) { message ->
                        withContext(Dispatchers.Main.immediate) { loading.setMessage(message) }
                    }
                }
                showPreview(bitmap)
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                Toaster.show(activity.getString(R.string.operator_poster_generation_failed, error.message))
            } finally {
                dialog.dismiss()
                setBusy(false)
                job = null
            }
        }
    }

    private fun showPreview(bitmap: Bitmap) {
        val binding = DialogOperatorPosterPreviewBinding.inflate(activity.layoutInflater)
        // Preview stays within texture limits; the original bitmap is used for saving/sharing.
        val scale = minOf(1f, 4096f / maxOf(bitmap.width, bitmap.height))
        val preview = if (scale < 1f) Bitmap.createScaledBitmap(bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt(), true) else bitmap
        binding.Poster.setImageBitmap(preview)
        binding.Poster.contentDescription = activity.getString(R.string.gacha_poster_preview)
        MaterialAlertDialogBuilder(activity).setTitle(R.string.gacha_poster_preview)
            .setView(binding.root).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                pendingSave = bitmap
                saveLauncher.launch("gacha_${System.currentTimeMillis()}.png")
            }
            .setNeutralButton(R.string.share) { _, _ -> share(bitmap) }
            .show()
    }

    private fun share(bitmap: Bitmap) {
        activity.lifecycleScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val directory = File(activity.cacheDir, "gacha_posters")
                    check(directory.exists() || directory.mkdirs()) { activity.getString(R.string.error_share_cache_create) }
                    val file = File(directory, "gacha_${System.currentTimeMillis()}.png")
                    file.outputStream().use { encode(bitmap, it) }
                    FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newUri(activity.contentResolver, "gacha poster", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)))
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                Toaster.show(activity.getString(R.string.operator_poster_share_prepare_failed, error.message))
            }
        }
    }

    private fun encode(bitmap: Bitmap, output: OutputStream) {
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
            activity.getString(R.string.error_image_encode)
        }
    }
}
