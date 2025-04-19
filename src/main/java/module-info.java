module net.woggioni.jwo {
    requires static java.xml;
    requires static java.sql;
    requires static lombok;
    requires static org.slf4j;

    exports net.woggioni.jwo;
    exports net.woggioni.jwo.exception;
    exports net.woggioni.jwo.url.classpath;
    exports net.woggioni.jwo.url.jpms;
    exports net.woggioni.jwo.xml;
}