# AIAgent

A Java-based wrapper agent that integrates Google Gemini AI with Minecraft, enabling the AI to interpret commands and perform automated building tasks directly in-game.

## Overview
AIAgent acts as a bridge between the sophisticated reasoning capabilities of the Gemini model and the Minecraft game engine. By sending building prompts to the AI, this agent translates natural language instructions into actionable building sequences within the game environment.

## Features
* **Gemini AI Integration:** Utilizes Google’s Gemini API for intelligent interpretation of complex building requests.
* **Automated Construction:** Translates AI-generated plans into block-placing logic within Minecraft.
* **Java-based Architecture:** Built with a modular structure for easy extension.

## Prerequisites
* Java 17+
* Maven
* Google Gemini API Key from [Google AI Studio](https://aistudio.google.com/)

## Getting Started

### 1. Configuration
Set your Google Gemini API Key in your environment variables or the configuration file provided in the project.

### 2. Building the Project
Navigate to the project root directory and run:

```bash
mvn clean package
```

### 3. Usage
Run the generated JAR file:

```bash
java -jar target/AIAgent-1.0.0.jar
```

## How it Works
1. **Input:** The user provides a natural language prompt (e.g., "Build a small wooden cottage").
2. **Processing:** The AIAgent sends this prompt to the Gemini API.
3. **Execution:** The AI returns the necessary instructions, which the Java wrapper parses and executes via the Minecraft API.

## Contributing
Contributions are welcome. Please open an issue or submit a pull request for improvements.

## License
This project is licensed under the MIT License.
