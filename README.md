# wso2apim-soap-api-importer
A command line utility to generate importable API definition for WSO2 APIM

## Build Instruction
 `mvn clean install`
## Usage Instruction
### Prerequisites
1.Setup WSO2 API Manager

2.A WSDL file
### Executing the command
`java -jar target/wso2-api-archive-generator-1.0-SNAPSHOT-jar-with-dependencies.jar  -workingDir=target -apiName=TestAPI  -apiVersion=1.0.0 -businessOwner=susan -businessOwnerEmail=susan@wso2.com -context=/test -description="This is a test API" -productionEndpoint=http://ws.cdyne.com/phoneverify/phoneverify.asmx -sandboxEndpoints=http://ws.cdyne.com/phoneverify/phoneverify.asmx  -tags=wso2,soap,test -technicalOwner=sander -technicalOwnerEmail=sander@wso2.com`
