# BORG_diagnostic_tool
An MOEAFramework plugin for parameterization visualizations and diagnostics with the Borg MOEA.

## Prerequisites
- MOEAFramework demo jar application, which is distributed freely on the MOEAFramework website: [http://moeaframework.org](http://moeaframework.org).
- Configured Borg MOEA jar file. Borg source code is private and can be requested here: [http://borgmoea.org](http://borgmoea.org).

## Installation
1. Download the release application.
2. Within the `BorgDiagnosticTool.jar` directory, include a `BorgDiatnosticTool_lib` folder with 
```
mkdir BorgDiatnosticTool_lib
```
3. Import the MOEAFramework demo application (with `borg.jar` properly configured and included in the build path) to `BorgDiagnosticTool_lib`.
4. Change the name of the MOEAFramework application to `MOEAFramework-2.13-Demo.jar`.
5. Run `BorgDiagnosticTool.jar` as an executable.

## Example
Visually explore various parameterizations and run them with the MOEAFramework diagnostic tool.
![Image of application](https://github.com/andrewdircks/Borg_diagnostic_tool/blob/master/examples/visualizations.png)

## Acknowledgements
- ***MOEAFramework*** is an open sourced library for developing, testing, and experimenting with multiobjective evolutionary algorithms, created by Dave Hadka. More information can be found [here](http://moeaframework.org). For this plugin, the `diagnostics` package within the MOEAFramework source was altered to support the Borg MOEA and parameter visualizations.
- The ***Borg MOEA*** is a multiobjective evolutionary algorithm developed by David Hadka and Patrick Reed. Information and access to the private repository can be found [here](http://borgmoea.org). 

## Supporting Resources
- Information on individual mating and mutation operators, as well as an interactive visualization webpage, can be found [here](https://github.com/andrewdircks/visualize_operators).
- For information on the Borg MOEA, see: *Hadka, D. and P. Reed. Borg: An Auto-Adaptive Many-Objective Evolutionary Computing Framework. Evolutionary Computation, 21(2):231-259, 2013.*

## Support
- For issues and feedback, contact Andrew Dircks at *abd93@cornell.edu*
