# JPipeline
[Node-RED](https://github.com/node-red/node-red) analogue, written fully in Java.  
Requires Java 11

## Modules

### jpipeline-executor
Application that contains all logic to execute pipelines

### jpipeline-manager
Spring Boot application that manages the executor application  
`./gradlew :jpipeline-manager:bootRun`
  
### jpipeline-javafx-client 
Client, desktop application  
`./gradlew :jpipeline-javafx-client:run`

#### [Client packaging](https://github.com/iamhook/jpipeline-client-packaging)
