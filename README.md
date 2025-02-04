# Steal-Split Tournament
_Steal-Split Tournament_ is an **Intelligent Steal-Split Game** developed as part of the course "[Programación de Sistemas Inteligentes](https://secretaria.uvigo.gal/docnet-nuevo/guia_docent/?ensenyament=V05G301V01&assignatura=V05G301V01403&any_academic=2024_25)" in the Telecommunications Engineering Degree at the Universidad de Vigo (2024 - 2025).

## About The Project
This project implements a multi-agent simulation of a Steal-Split Tournament, where autonomous agents compete by making strategic financial decisions.

The project features:
- Multi-agent system using JADE.
- Steal-Split decision-making framework.
- Adaptative strategies based on past interactions.
- Structured tournament format with performance tracking.
- Intuitive and user-friendly interface.

## How To Run
### Compilation
Make sure you have a [Java JDK](https://www.oracle.com/java/technologies/downloads/) installed on your system. Then compile all Java classes and generate the `.class` files with:
```bash
javac -p lib/javafx/lib --add-modules javafx.controls,javafx.fxml -cp 'lib/jade.jar;lib/javafx/lib' -d bin src/*.java src/agents/*.java
```
This command creates the compiled files inside the `bin/` directory.

### Execution
Once compiled, you can run the system with:
```bash
java -p lib/javafx/lib --add-modules javafx.controls,javafx.fxml -cp 'lib/jade.jar;bin;src/resources' jade.Boot -agents 'MainAgent:MainAgent[;Agent1:agents.Agent1Class[;Agent2:agents.Agent2Class[;...]]]'
```

## About The Code
Refer to [`Specifications.pdf`](docs/Specifications.pdf) for an in-depth explanation of the project, how the game works, the communication between the agents, the development process, and more.