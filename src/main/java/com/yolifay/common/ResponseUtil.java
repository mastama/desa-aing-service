package com.yolifay.common;

public final class ResponseUtil {
    private ResponseUtil() {
        throw new IllegalStateException("Utility Class");
    }
        /**
        * Set response service
        * @param httpStatus HTTP Status Code
        * @param serviceId Service Identifier
        * @param response CommonConstant.RESPONSE Enum
        * @param obj Data Object
        * @return ResponseService
        */
    public static ResponseService setResponse(int httpStatus, String serviceId, CommonConstants.RESPONSE response, Object obj) {
        var res = new ResponseService();
        res.setResponseCode(httpStatus + serviceId + response.getCode());
        res.setResponseDesc(response.getDescription());
        res.setData(obj);
        return res;
    }
}
