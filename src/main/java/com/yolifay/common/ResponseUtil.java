package com.yolifay.common;

/**
 * Utility class pembentuk ResponseService secara fleksibel,
 * mendukung kode lama (deprecated) dan pola baru: httpStatus + serviceId + caseCode.
 */
public final class ResponseUtil {

    private ResponseUtil() { throw new IllegalStateException("Utility Class"); }

    @Deprecated
    public static ResponseService setResponse(CommonConstants.RESPONSE response, Object obj) {
        return setResponse("200", "GEN", response, obj);
    }

    @Deprecated
    public static ResponseService setResponse(CommonConstants.RESPONSE response, String customMessage, Object obj) {
        ResponseService res = new ResponseService();
        res.setResponseCode(response.getCode());
        res.setResponseDesc(customMessage);
        res.setData(obj);
        return res;
    }

    @Deprecated
    public static ResponseService setResponse(String responseCode, String responseDesc, Object obj) {
        ResponseService res = new ResponseService();
        res.setResponseCode(responseCode);
        res.setResponseDesc(responseDesc);
        res.setData(obj);
        return res;
    }

    public static ResponseService setResponse(String httpStatus, String serviceId, String caseCode, String description, Object obj) {
        ResponseService res = new ResponseService();
        res.setResponseCode(httpStatus + serviceId + caseCode);
        res.setResponseDesc(description);
        res.setData(obj);
        return res;
    }

    public static ResponseService setResponse(int httpStatus, String serviceId, String caseCode, String description, Object obj) {
        return setResponse(String.valueOf(httpStatus), serviceId, caseCode, description, obj);
    }

    public static ResponseService setResponse(String httpStatus, String serviceId, CommonConstants.RESPONSE response, Object obj) {
        return setResponse(httpStatus, serviceId, response.getCode(), response.getDescription(), obj);
    }

    public static ResponseService setResponse(int httpStatus, String serviceId, CommonConstants.RESPONSE response, Object obj) {
        return setResponse(String.valueOf(httpStatus), serviceId, response.getCode(), response.getDescription(), obj);
    }
}
