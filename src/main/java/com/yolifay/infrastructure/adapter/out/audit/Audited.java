package com.yolifay.infrastructure.adapter.out.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    /**
     * AOP for auto audit
     */
    AuditAction action() default AuditAction.NONE;  // untuk AUTH (type-safe)
    String value() default "";                      // untuk event bebas: "USER_ROLE_ASSIGN", "LETTER_APPROVE", dll.
    String targetType() default "";                 // opsional: "User","Population","Letter"
    String targetId() default "";                   // opsional: isi ID target (bisa kamu isi manual di aspect atau nanti pakai SpEL)
}
