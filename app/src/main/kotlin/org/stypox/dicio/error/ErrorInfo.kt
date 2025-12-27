package org.stypox.dicio.error

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.stypox.dicio.util.ExceptionUtils

/**
 * @implNote Taken with some modifications from NewPipe, file error/ErrorInfo.kt
 */
@Parcelize
class ErrorInfo(val stackTrace: String, val userAction: UserAction) : Parcelable {
    constructor(throwable: Throwable?, userAction: UserAction) : this(
        if (throwable == null) "" else ExceptionUtils.getStackTraceString(throwable),
        userAction
    )
}

