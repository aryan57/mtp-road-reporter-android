package com.example.myapplication.utils

object Constants {
    const val SERVER_BASE_URL = "https://server.aryan57.com"
    const val API_VERSION = "/api/v1/"

    const val API_PATH_LOGIN = "user/login"
    const val API_PATH_SIGNUP = "user/signup"
    const val API_PATH_WHOAMI = "user/whoami"
    const val API_PATH_REFRESH = "user/refresh"

    const val API_PATH_ALLPOSTS = "post/allPosts"
    const val API_PATH_DELETE_POST = "post/deletePost"
    const val API_PATH_GET_IMAGE = "post/getPostImage"
    const val API_PATH_UPLOAD_IMAGE = "post/uploadPostImage"
    const val API_PATH_CREATE_POST = "post/createPost"

    const val SHARED_PREFERENCES_NAME = "MTP_ROAD_REPORTER_SHARED_PREFERENCES"
    const val USER_CONFIG_NAME = "USER_CONFIG_NAME"
    const val USER_CONFIG_EMAIL = "USER_CONFIG_EMAIL"
    const val TOKEN_CONFIG_ACCESS_TOKEN = "access_token"
    const val TOKEN_CONFIG_REFRESH_TOKEN = "refresh_token"

    const val LOG_TAG_NAME = "TAG"
}