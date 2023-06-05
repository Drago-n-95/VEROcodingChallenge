package com.drago.veroassignment

interface ResourcesCallback {
    fun onSuccess(resources: List<Resource>)
    fun onFailure()
}