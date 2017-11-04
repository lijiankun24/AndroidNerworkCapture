package com.lijiankun24.networkcapture.library.filter.util;


import com.lijiankun24.networkcapture.library.har.HarResponse;

public class HarCaptureUtil {

    public static final String HTTP_VERSION_STRING_FOR_FAILURE = "unknown";

    public static final int HTTP_STATUS_CODE_FOR_FAILURE = 0;

    public static final String HTTP_REASON_PHRASE_FOR_FAILURE = "";

    private static final String RESOLUTION_FAILED_ERROR_MESSAGE = "Unable to resolve host: ";

    private static final String CONNECTION_FAILED_ERROR_MESSAGE = "Unable to connect to host";

    private static final String RESPONSE_TIMED_OUT_ERROR_MESSAGE = "Response timed out";

    private static final String NO_RESPONSE_RECEIVED_ERROR_MESSAGE = "No response received";

    public static HarResponse createHarResponseForFailure() {
        return new HarResponse(HTTP_STATUS_CODE_FOR_FAILURE, HTTP_REASON_PHRASE_FOR_FAILURE, HTTP_VERSION_STRING_FOR_FAILURE);
    }

    public static String getResolutionFailedErrorMessage(String hostAndPort) {
        return RESOLUTION_FAILED_ERROR_MESSAGE + hostAndPort;
    }

    public static String getConnectionFailedErrorMessage() {
        return CONNECTION_FAILED_ERROR_MESSAGE;
    }

    public static String getResponseTimedOutErrorMessage() {
        return RESPONSE_TIMED_OUT_ERROR_MESSAGE;
    }

    public static String getNoResponseReceivedErrorMessage() {
        return NO_RESPONSE_RECEIVED_ERROR_MESSAGE;
    }
}
