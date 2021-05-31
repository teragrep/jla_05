# Log4j RELP Plugin
[![Build Status](https://scan.coverity.com/projects/23199/badge.svg)](https://scan.coverity.com/projects/jla_05)

## Please note
[[CVE-2020-9488]](https://nvd.nist.gov/vuln/detail/CVE-2020-9488) Improper validation of certificate with host mismatch in Apache Log4j SMTP appender.

[[CVE-2019-17571]](https://nvd.nist.gov/vuln/detail/CVE-2019-17571) Included in Log4j 1.2 is a SocketServer class that is vulnerable to deserialization of untrusted data which can be exploited to remotely execute arbitrary code when combined with a deserialization gadget when listening to untrusted network traffic for log data.

## Example log4j.properties

```
log4j.rootLogger=DEBUG, CONSOLE, RELPAPPENDER
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.Threshold=INFO
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=[%p] [%F#%L] [%C] %m%n
log4j.appender.RELPAPPENDER=com.teragrep.jla_05.RelpAppender
log4j.appender.RELPAPPENDER.Threshold=INFO
log4j.appender.RELPAPPENDER.layout=org.apache.log4j.PatternLayout
log4j.appender.RELPAPPENDER.layout.ConversionPattern=[%p] [%F#%L] [%C] %m%n
log4j.appender.RELPAPPENDER.relpAddress=localhost
log4j.appender.RELPAPPENDER.relpPort=1666
log4j.appender.RELPAPPENDER.appName=jla_05
log4j.appender.RELPAPPENDER.hostname=localhost
log4j.appender.RELPAPPENDER.connectionTimeout=5000
log4j.appender.RELPAPPENDER.writeTimeout=2000
log4j.appender.RELPAPPENDER.readTimeout=15000
log4j.appender.RELPAPPENDER.reconnectInterval=3000
log4j.appender.RELPAPPENDER.useSD=true
```


## Usage

Pass log4j.properties file as property

```
-Dlog4j.configuration=file:/path/to/log4j.properties
```

Using the logger

```
static Logger logger = Logger.getLogger(MyClass.class.getName());
logger.info("Info Message");
logger.warn("Warning message");
logger.trace("Trace message");
// Finally shutdown so RELP can disconnect gracefully
LogManager.shutdown();
```

## Maven dependency definition

```
<dependency>
    <groupId>com.teragrep</groupId>
    <artifactId>jla_05</artifactId>
    <version>%VERSION%</version>
</dependency>
```
