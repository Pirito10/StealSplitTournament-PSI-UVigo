# Steal-Split Tournament
_Steal-Split Tournament_ is an **Intelligent Steal-Split Game** developed as part of the course "[Programación de Sistemas Inteligentes](https://secretaria.uvigo.gal/docnet-nuevo/guia_docent/?centre=305&ensenyament=V05G301V01&assignatura=V05G301V01403&any_academic=2024_25)" in the Telecommunications Engineering Degree at the Universidad de Vigo (2024 - 2025).

## About The Project
This project implements a multi-agent simulation of a steal-split tournament, where autonomous agents compete by making strategic financial decisions.

The project features:
- Multi-agent system using [JADE](https://jade-project.gitlab.io).
- Steal-Split decision-making framework.
- Adaptative strategies based on past interactions.
- Structured tournament format with real-time performance tracking.
- User-friendly interface for easy interaction and visualization.

## How To Run
*The following instructions are for Windows only. If you are using another operating system, you will need to download the appropiate [JavaFX](https://gluonhq.com/products/javafx/) version for your system and adjust the compilation and execution commands accordingly.*

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
| Option | Description | Example |
|--------|-------------|---------|
| `RandomAgent` | Selects actions randomly without a predefined strategy | `Agent1:agents.RandomAgent` |
| `RL_Agent` | Uses reinforcement learning to adapt based on past interactions | `Agent2:agents.RL_Agent` |
| `NN_Agent` | Implements a neural network-based decision model for complex strategies | `Agent3:agents.NN_Agent` |

## About The Code
Refer to [`Specifications.pdf`](docs/Specifications.pdf) for an in-depth explanation of the project, the game mechanics, the communication between agents, the tournament structure, and more.
