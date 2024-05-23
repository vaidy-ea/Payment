# pd-cp-paymentprocessing-service
This is a payment processing service developed using Java, SQL, Spring Boot, and Maven. 
The service is responsible for sending various types of payment requests to the Intelligent Router (IR), saving the details to the Payment DB(T_PAYMENT table), 
and returning the response or error back to the upstream systems.

### Prerequisites
- Java 8 or higher
- Maven
- SQL database

### Installing
1. Clone the repository
2. Navigate to the project directory
3. Run `mvn clean install` to build the project

## Running the tests
To run tests for this project, you would typically use the testing framework that's integrated with the build tool you're using.
In this case, the project is using Maven, so you would use the Maven command to run tests.

Here's how you can do it:
1. Open a terminal or command prompt.
2. Navigate to the project directory. You can do this using the `cd` command followed by the path to your project. For example:
```bash
cd path/to/your/project
```

3. Once you're in the project directory, you can run the tests using the following Maven command:
```bash
mvn test
```

This command tells Maven to run the `test` phase of the build lifecycle. 
During this phase, Maven will compile your code, then it will run any unit tests that you have in your `src/test` directory.
After the command is executed, you should see output in the terminal showing the results of the tests.
If a test fails, Maven will provide information about what test failed and why.

Remember, you need to have Maven and Java installed on your machine to run the tests.

## Deployment
The Deployment is done using Azure App Service and the steps are as follows:

1. **Create an Azure account**: If you don't have an Azure account, you can create one [here](https://azure.microsoft.com/en-us/free/).
2. **Install the Azure CLI**: The Azure CLI is a command-line tool that you can use to manage Azure resources. You can download it from [here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli).
3. **Login to Azure**: Search for pd-cp-paymentprocessing-service in the Container Apps, you will get all the details there.

## Built With
- [Java](https://www.java.com) - The programming language used
- [Spring Boot](https://spring.io/projects/spring-boot) - The web framework used
- [Maven](https://maven.apache.org/) - Dependency Management
- [SQL](https://www.mysql.com/) - Used to create the database

## How to use Postman Collections for this project
1. **Install Postman**: Download and install Postman from [here](https://www.postman.com/downloads/).
2. **Import the Collection**: ind the postman collections in 'postmancollections' folder and click on the `Import` button in Postman and choose your collection file.
3. **Send Requests**: Now you can send requests to the service. Click on a request in the collection, fill in any required parameters, and hit `Send`.

Remember to replace the base URL and any other parameters in the requests with the values for your local setup.

## Postman Collection Structure
A typical Postman collection for this project might have folders for each type of payment request that the service can handle:
- Initial Auth
- Incremental Authorization
- Capture
- Card Void
- Refund

Each folder would contain requests for the different operations that can be performed for that type of payment. 
For example, the UC2 folder will contain a request to send an Incremental Authorization request to the router.

Each request would need to include the appropriate headers and body content as defined by the service.
The headers can be added in the `Headers` tab or Prerequisite scripts and the body content can be added in the `Body` tab in Postman.

## Logging
Logging is configured in the `log4j2.xml` file. The logs are stored in the `cpPaymentProcessingLogs` directory.

## Configuration
The application can be configured via the `application.properties` file. Here you can set server port, active profiles, actuator endpoints, and resilience4j configurations.
There are also separate configuration files for the different environments.
All the configurations are stored in the `config` directory.