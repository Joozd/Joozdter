package nl.joozd.joozdter.utils.extensions

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT

internal fun Intent.getParcelableExtraUri(): Uri? =
    if(SDK_INT >= 33)
        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    else
        @Suppress("DEPRECATION") getParcelableExtra(Intent.EXTRA_STREAM) as? Uri