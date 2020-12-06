package com.myoptimind.getexpress.features.shared.api

sealed class Result<out T> {
    class Progress(val isLoading: Boolean) : Result<Nothing>()
    class Success<out R>(val data: R?): Result<R>()
    class Error(val metaResponse: MetaResponse): Result<Nothing>()
    class HttpError(val error: Exception): Result<Nothing>()
}