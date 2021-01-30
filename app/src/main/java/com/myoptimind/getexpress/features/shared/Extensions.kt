package com.myoptimind.getexpress.features.shared

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.loader.content.CursorLoader
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.myoptimind.getexpress.features.customer.cart.data.CartLocation
import com.myoptimind.getexpress.features.login.data.Address
import com.myoptimind.getexpress.features.shared.api.MetaErrorResponse
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.suzuki_app.features.shared.DatePickerDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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
): Flow<Result<T>> {
    Timber.d("debugging...")
    return retryWhen { cause, _ ->
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
                Timber.d(this.toString())
                if(nullOnComplete){
                    emit(Result.Success(null))
                }

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

fun EditText.izBlank(): Boolean {
    return this.text.toString().isBlank()
}

fun DateTime.toReadableDateTime(): String {
//    val pattern = "MMMM dd, yyyy"
    val pattern = "yyyy-MM-dd"
    return DateTimeFormat.forPattern(pattern).print(this)
}

fun EditText.initDatePicker(
    fragmentManager: FragmentManager,
    targetFragment: Fragment,
    dialogTag: String,
    requestCode: Int,
    maxDate: Long? = DateTime().millis,
    minDate: Long? = null
) {
    this.setOnClickListener {
        val dt = if (this.text.toString().isEmpty()) {
            DateTime()
        } else {
            DateTime.parse(this.text.toString(), DateTimeFormat.forPattern("yyyy-MM-dd"))
        }

        DatePickerDialogFragment.newInstance(
            dt.year,
            dt.monthOfYear - 1,
            dt.dayOfMonth,
            maxDate,
            minDate
        ).also {
            it.setTargetFragment(targetFragment, requestCode)
            it.show(fragmentManager, dialogTag)
        }


    }


}

fun String.validateEmail(): Boolean {
//    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    val emailPattern = Patterns.EMAIL_ADDRESS.toRegex()
    return this.matches(emailPattern)
}

fun Place.toAddress(): Address {
    return Address(
            "0",
            "0",
            label = name!!,
            address!!,
            latLng!!.latitude.toString(),
            latLng!!.longitude.toString()
    )
}

fun Place.toCartLocation(): CartLocation {
    return CartLocation(
            name!!,
            address!!,
            latLng!!.latitude.toString(),
            latLng!!.longitude.toString(),
            ""
    )
}
