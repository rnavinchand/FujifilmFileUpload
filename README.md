# FujifilmFileUpload

## Android file upload helper class

### Introduction

Provides a simple interface to upload a file to a remote server using underlying HttpURLConnection class provided by the Android SDK.


### Overview

FujifilmFileUpload consists of two files :-

1. `FujifilmFileUpload.java` where all the magic happens
2. `FujifilmFileUploadHandler.java` An abstract class that requires the calling app/class to implement 2 callbacks.

#### FujifilmFileUpload.java

Has to be called on a background thread, can not be called/instantiated on the main thread. The class provides the following public methods to upload a `File` object.

    public void addHeader(String name, String value)
    public void addFilePart(String fieldName, File file)
    public void finish(FujifilmFileUploadHandler handler)


#### FujifilmFileUploadHandler.java

An implementation of the class must be passed in as an argument to the `finish` method of `FujifilmFileUpload` class. Two listeners are provided that notify the calling app/class the result of the file upload request.

    public void onSuccess(String response, int statusCode)
    public void onFailure(String errorResponse, int statusCode)
