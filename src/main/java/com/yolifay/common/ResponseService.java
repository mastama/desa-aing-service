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
}
