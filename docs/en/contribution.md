# Development Environment Setup

## Prerequisites

### Frontend
* Vue: 3.4.x
* Vite: 5.x
* NodeJS: v18.x
* Pnpm: v8.x
* Component Library: Ant Design Vue 4.x

### Backend
* Git: Any version
* JDK: JDK17 or higher
* Database: Postgres(16+) or MySQL(8+)
* Maven: It is recommended to use version 3.8 or higher
* Development Tool: Intellij IDEA

## Setup

### Get the Code
First, you need to pull the Bigtop Manager source code from GitHub using the following command:

`git clone git@github.com:apache/bigtop-manager.git`

### Compile
After getting the code, some dependencies require you to compile the project first before you can use them; otherwise, an error will occur. Please run the following command:

`./mvnw clean install -DskipTests`

### Initialize
First, you need to initialize your database. The database files are in the `bigtop-manager-server/src/main/resources/ddl/` directory. Please use the corresponding scripts to initialize your database. Currently, only `Postgres` and `MySQL` are supported.

And modify your database information in the `bigtop-manager-server/src/main/resources/application.yml` file.

### Development
After compiling the project, you can start development.

For Java projects, you can directly use the `Debug` feature of Intellij IDEA.

For Vue projects, please run the following commands:

```
cd bigtop-manager-ui
pnpm dev
```

Then access `localhost:5173` in your browser. Next, enjoy your development journey!

### Development Mode
To reduce the complexity of development environment dependencies for big data components, we support development mode:

#### Environment Decoupling Design
Traditional deployment requires developers to set up a complete Linux service cluster, with the following pain points:
* High environmental configuration complexity raises the development threshold
* Non-component issues (such as scheduling problems, etc.) easily hinder development

#### Lightweight Debugging Mechanism
Activate developer mode via the `DEV_MODE=true` environment variable to achieve:
* **Mock component operations**: The Agent automatically intercepts component calls and returns a preset success status
* **Cross-platform support**: Fully compatible with Windows/MacOS/Linux development environments (IntelliJ IDEA recommended)

#### Quick Activation
Users can enable it through the following method: 
![DEV_MODE](https://github.com/user-attachments/assets/d0e59fad-4287-4be5-a57c-d5c656e0dbb2)

# Modules and Functions
| Module                    | Introduction                                                                                                               | 
|---------------------------|----------------------------------------------------------------------------------------------------------------------------|
| **bigtop-manager-agent**  | It will be installed on each host to manage the services on each host.                                                     |
| **bigtop-manager-ai**     | It contains some code related to the AI assistant.                                                                         |
| **bigtop-manager-bom**    | It defines all the dependencies and their versions in the project.                                                         |
| **bigtop-manager-common** | It contains some common utility classes.                                                                                   |
| **bigtop-manager-dao**    | It interacts with the database.                                                                                            |
| **bigtop-manager-dist**   | The packaged content will be placed in this module, including the tar packages of `Server` and `Agent`.                    |
| **bigtop-manager-grpc**   | The Server application and the Agent application interact through gRPC. This module contains all gRPC service definitions. |
| **bigtop-manager-server** | It is the main code of the management end.                                                                                 |
| **bigtop-manager-stack**  | It contains the components and their operation scripts in each component stack.                                            |
| **bigtop-manager-ui**     | It is the front - end code.                                                                                                |

# Contribution Process

## Code Style
We need to ensure that our code format meets the requirements.

Java:
```
./mvnw clean spotless:apply
```

Vue:
```
cd bigtop-manager-ui
pnpm prettier
```

## Unit Tests
Before submitting your code, please ensure that all unit tests pass.

Java:
```
./mvnw clean test -Dskip.pnpm -Dskip.installnodepnpm -Dskip.pnpm.test
```

Vue:
```
./mvnw -pl bigtop-manager-ui test
```

## Submit Code
1. Fork the project on GitHub.
2. Clone your Forked repository locally, e.g., `git clone git@github.com:your-username/bigtop-manager.git`.
3. Create a local branch, e.g., `git checkout -b your-branch-name`.
4. Commit your code to that branch and push the branch to your Forked repository on GitHub.
5. Create a Pull Request, ensuring that the Title follows the [Conventional Commits Specification](https://www.conventionalcommits.org/).
6. Ensure that all GitHub CI checks pass; if any fail, the PR will not be reviewed.
7. After CI passes, wait for a Maintainer to review your PR. If there are comments, address them promptly. Once the review is approved, it can be merged.
