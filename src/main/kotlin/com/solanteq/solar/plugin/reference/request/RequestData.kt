package com.solanteq.solar.plugin.reference.request

fun parseRequestString(requestString: String): RequestData? {
    val requestSplit = requestString.split(".")
    if(requestSplit.size != 3 || requestSplit.any { it.isEmpty() }) return null
    val (groupName, serviceName, methodName) = requestSplit
    return RequestData(groupName, serviceName, methodName)
}

data class RequestData(
    val groupName: String,
    val serviceName: String,
    val methodName: String
)