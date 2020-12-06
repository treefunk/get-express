package com.myoptimind.getexpress.features.shared

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import androidx.loader.content.CursorLoader
import com.google.gson.Gson
import com.myoptimind.getexpress.features.shared.api.MetaErrorResponse
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException


fun HttpException.toMetaResponse(): MetaResponse {
    val errorJsonString = this.response()?.errorBody()?.string()
    val metaErrorResponse = Gson().fromJson(errorJsonString!!, MetaErrorResponse::class.java)
    return metaErrorResponse.meta
}

fun Exception.handleErrors(
    exception: (e: Exception) -> Unit,
    httpException: (e: MetaResponse) -> Unit
) {
    if (this is HttpException) {
        httpException(this.toMetaResponse())
    } else {
        exception(this)
    }
}

suspend fun Throwable.handleErrors(
    exception: suspend (e: Exception) -> Unit,
    httpException: suspend (e: MetaResponse) -> Unit
) {
    if (this is HttpException) {
        httpException(this.toMetaResponse())
    } else {
        exception(this as Exception)
    }
}


fun <T : Any> Flow<Result<T>>.applyDefaultEffects(
    enableRetry: Boolean = true,
    nullOnComplete: Boolean = false
): Flow<Result<T>> =
    retryWhen { cause, _ ->
        when (cause) {
            !is HttpException -> {
                if (enableRetry) {
                    for (i in 10 downTo 1) {
                        Timber.e("retrying request in $i")
                        delay(1000)
                    }
                }
                enableRetry
            }
            else -> false
        }
    }.catch { exception ->
        exception.handleErrors({
            if (it is UnknownHostException) {
                emit(Result.HttpError(Exception("Internet Connection is required. Please try again.")))
            } else {
                emit(Result.HttpError(it))
            }
        }, {
            emit(Result.Error(it))
        })
    }.onStart { emit(Result.Progress(isLoading = true)) }
        .onCompletion {
            delay(100)
            emit(Result.Progress(isLoading = false))
            if(nullOnComplete){
                emit(Result.Success(null))
            }else{
                Timber.v("last emit - " + this@applyDefaultEffects.first())
                emit(this@applyDefaultEffects.first())
            }

        }


fun Array<EditText>.clearTextViews() {
    forEach { editText ->
        editText.setText("")
    }
}

fun Uri.getRealPath(context: Context): String? {
    val proj = arrayOf(MediaStore.Images.Media.DATA)
//    val loader = CursorLoader(context, this, proj, null, null, null)
    val cursor: Cursor? = context.contentResolver.query(
        this,proj,null,null,null
    )
    val columnIndex: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor?.moveToFirst()
    val result = if(columnIndex != null){
        cursor.getString(columnIndex)
    }else{
        null
    }
    cursor?.close()
    return result
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun EditText.izNotBlank(): Boolean {
    return this.text.toString().isNotBlank()
}