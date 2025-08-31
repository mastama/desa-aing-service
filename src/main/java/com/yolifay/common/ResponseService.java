package com.yolifay.common;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseService {
    private String responseCode;
    private String responseDesc;
    private Object data;


    public static ResponseService of(Constant.RESPONSE r, Object data) {
        return ResponseService.builder()
                .responseCode(r.getCode())
                .responseDesc(r.getDescription())
                .data(data)
                .build();
    }
}
