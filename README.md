# NoosphereAgent

Noosphere Agent is a Java-based agent that interacts with Web3 smart contracts to perform decentralized off-chain computations. This agent listens for blockchain events, executes the requested computation tasks in Docker containers, and sends the results back to the smart contract or a Noosphere Hub.

## Key Features

- **Blockchain Integration**: Listens to real-time events from a smart contract (Router) and submits computation results as transactions.
- **Dynamic Container Management**: Automatically manages the lifecycle of Docker containers defined in `config.json` (image pull, creation, execution, health checks, and restarts).
- **Computation Orchestration**: Can chain multiple containers together to perform sequential computation tasks.
- **Versatile Request Handling**: Processes various types of computation requests, including on-chain (from smart contracts), off-chain (via HTTP API), and delegated requests.
- **Hub Integration**: Registers itself with the Noosphere Hub, periodically reports its status, and can execute delegated tasks assigned by the Hub.

## Prerequisites

The following are required for development and execution:

- Java 17+
- Docker
- Node.js (for frontend development)
- A local blockchain testing environment (e.g., anvil)

## Configuration

The agent's primary configuration is managed through the `src/main/resources/config.json` file. The path to this file is specified in `application.yml` under the `application.noosphere.configFilePath` property.

Key configuration sections in `config.json` include:

- `chain`: Blockchain network information (RPC URL, Router contract address, wallet private key, etc.).
- `docker`: Docker Registry authentication credentials (Username, Password).
- `containers`: A list of Computation containers to be managed by the agent (image, port, environment variables, access control rules, etc.).

### Environment Variables

Sensitive information must be injected via environment variables.

## Project Structure

Node is required for generation and recommended for development. `package.json` is always generated for a better development experience with prettier, commit hooks, scripts and so on.

In the project root, JHipster generates configuration files for tools like git, prettier, eslint, husky, and others that are well known and you can find references in the web.

- `/src/*` structure follows default Java structure.
- `/src/main/docker` - Docker configurations for the application and services that the application depends on

## Development

The build system will install automatically the recommended version of Node and npm.

We provide a wrapper to launch npm.
You will only need to run this command when dependencies change in [package.json](package.json).

```
./npmw install
```

We use npm scripts and [Webpack][] as our build system.

If you are using hazelcast as a cache, you will have to launch a cache server.
To start your cache server, run:

```
docker compose -f src/main/docker/hazelcast-management-center.yml up -d
```

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

```
./gradlew -x webapp
./npmw start
```

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `./npmw update` and `./npmw install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `./npmw help update`.

The `./npmw run` command will list all the scripts available to run for this project.

### Generating Web3j Wrappers

To interact with smart contracts, you need to generate Java wrapper classes. Use the `generateWeb3jWrappers` task in `build.gradle`.

1.  Ensure your compiled contract artifacts (`.json`) are present in the `./contract/out` directory (e.g., by running `forge build`).
2.  Add the names of the contracts you want to generate to the `selectedContracts` list in `build.gradle`.
3.  Run the following command:

## Building for production

### Packaging as jar

To build the final jar and optimize the noosphereAgent application for production, run:

```
./gradlew -Pprod clean bootJar
```

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

```
java -jar build/libs/*.jar
```

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

```
./gradlew -Pprod -Pwar clean bootWar
```

## Testing

### Spring Boot tests

To launch your application's tests, run:

```
./gradlew test integrationTest jacocoTestReport
```

### Gatling

Performance tests are run by [Gatling][] and written in Scala. They're located in [src/test/java/gatling/simulations](src/test/java/gatling/simulations).

You can execute all Gatling tests with

```
./gradlew gatlingRun.
```

### Client tests

Unit tests are run by [Jest][]. They're located near components and can be run with:

```
./npmw test
```

UI end-to-end tests are powered by [Cypress][]. They're located in [src/test/javascript/cypress](src/test/javascript/cypress)
and can be run by starting Spring Boot in one terminal (`./gradlew bootRun`) and running the tests (`./npmw run e2e`) in a second one.

#### Lighthouse audits

You can execute automated [Lighthouse audits](https://developers.google.com/web/tools/lighthouse/) with [cypress-audit](https://github.com/mfrachet/cypress-audit) by running `./npmw run e2e:cypress:audits`.
You should only run the audits when your application is packaged with the production profile.
The lighthouse report is created in `build/cypress/lhreport.html`

## Others

### Code quality using Sonar

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker compose -f src/main/docker/sonar.yml up -d
```

Note: we have turned off forced authentication redirect for UI in [src/main/docker/sonar.yml](src/main/docker/sonar.yml) for out of the box experience while trying out SonarQube, for real use cases turn it back on.

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the gradle plugin.

Then, run a Sonar analysis:

```
./gradlew -Pprod clean check jacocoTestReport sonarqube -Dsonar.login=admin -Dsonar.password=admin
```

Additionally, Instead of passing `sonar.password` and `sonar.login` as CLI arguments, these parameters can be configured from [sonar-project.properties](sonar-project.properties) as shown below:

```
sonar.login=admin
sonar.password=admin
```

For more information, refer to the [Code quality page][].

### Docker Compose support

JHipster generates a number of Docker Compose configuration files in the [src/main/docker/](src/main/docker/) folder to launch required third party services.

For example, to start required services in Docker containers, run:

```
docker compose -f src/main/docker/services.yml up -d
```

To stop and remove the containers, run:

```
docker compose -f src/main/docker/services.yml down
```

[Spring Docker Compose Integration](https://docs.spring.io/spring-boot/reference/features/dev-services.html) is enabled by default. It's possible to disable it in application.yml:

```yaml
spring:
  ...
  docker:
    compose:
      enabled: false
```

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a Docker image of your app by running:

```sh
npm run java:docker
```

Or build a arm64 Docker image when using an arm64 processor os like MacOS with M1 processor family running:

```sh
npm run java:docker:arm64
```

Then run:

```sh
docker compose -f src/main/docker/app.yml up -d
```
