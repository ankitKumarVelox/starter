# Velox Starter
This is a starter application building an application on the Velox platform.
## How to Build and Run
At the minimum you need to have Java 11 installed locally for building and running the app. Although Gradle is used for building this repository, gradle wrapper is provided hence pre-installed Gradle is not necessary. However, if you prefer pre-installed Gradle, please install Gradle version 7.2 or above. 
### Build The Default Way
If you have unrestricted network:
1. clone or download the source files from the master branch
1. cd to the root of the project folder
1. use `./gradlew build` (or `gradlew.bat build` on Windows) to build
1. use`./gradlew run` (or `gradlew.bat run` on Windows) to start the app server 
1. use `http://localhost:6061/starter` to access the gui in Chrome once app server starts

### Work with the project in Intellij
In Intellij, open the settings.gradle file. When prompted, click on "Open as a project". You can then use the tasks in the Gradle panel to clean, build and run the project. You can also use taks velox -> generateVelox to run the Velox code gen whenever you change the dictionary.
