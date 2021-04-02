module com.jpipeline.common {
    requires lombok;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires reactor.core;

    exports com.jpipeline.common.dto;
    exports com.jpipeline.common.entity;
    exports com.jpipeline.common.util;
    exports com.jpipeline.common.util.annotations;
    exports com.jpipeline.common.util.exception;
}
