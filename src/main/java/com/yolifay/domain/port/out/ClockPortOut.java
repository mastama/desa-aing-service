package com.yolifay.domain.port.out;

import java.time.Instant;

public interface ClockPortOut {
    Instant now();
}
