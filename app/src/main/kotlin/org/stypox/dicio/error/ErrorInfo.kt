package org.stypox.dicio.error

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @implNote Taken with some modifications from NewPipe, file error/ErrorInfo.kt
 */
@Parcelize
class ErrorInfo(val stackTrace: String, val userAction: UserAction) : Parcelable {
    constructor(throwable: Throwable?, userAction: UserAction) : this(
        if (throwable == null) "" else getStackTraceString(throwable),
        userAction
    )
    
    companion object {
        private fun getStackTraceString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
        }
    }
}
