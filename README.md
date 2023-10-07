# GreenPipelineDB

GreenPipelineDB has been designed in the initiative to support green computing. Due to energy over-consumming in Datacenters, energy management has become a critical aspect for DBMSs designers in the order to identify new 
potentials way to save energy consumption and reduce the carbon footprint. Our proposition follows this directives. Our tool was created atop the PostgreSQL system, incorporating modifications to accommodate our cost model
and query plan evaluation model. To enhance user-friendliness, we introduced a Graphical User Interface (GUI) for defining specific parameter values and real-time visualization of energy consumption for queries. 
This tool is developed in Java. The workflow of our Framework, developed in Java, is detailed in the following Figure:

<center><img src="https://github.com/dembeles/GreenPipelineDB/assets/57449625/7c174872-92a9-43ab-a30a-19f696e5e7d7" width="600" align="center" alt="Workflow of our Framework"></center>

## Data Storage Systems (DSS) installation (PostgreSQL)
To build and install the release of PostgreSQL that we modify, you will find the steps in INSTALL file or follow from the official documentation:[https://www.postgresql.org/download/]

## Requirements

-  Operating System: Ubuntu Version 14.04 LTS or higher,
-  Compiler: gcc version 4.8 or higher, java 8 or higher,
-  Driver: Watts Up? Pro power meter (Optional)

## For Power Estimation (necessary librairies)
### Rjava:(https://cran.r-project.org/web/packages/rJava/index.html)

- Sudo apt-get install default-jdk
- Sudo R CMD javareconf
- Sudo apt-get install r-cran-rjava
- Sudo apt-get install libgdal-dev libproj-dev
- Install.packages("rJava")
- Location of "R Home"
- Appache netbeans.conf location and add export R HOME=path at the end of file
- Locate rjava path folder
- Add jar librairy in App

