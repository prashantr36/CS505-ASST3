README
------------------------------------------------------------
How TO RUN:

To clean, build and create JARs for all projects' code please type in the KeyVs folder:


					$"ant clean"
					
					$"ant jar" 



Following which change directory to point to project you're testing, for example:

					$"cd deliver/project3"

After which, 

					$"bash runJavaRMI.sh"

This will run the unit tests as well as the integration tests written in python found in the RMIClient folder.
The RMIClient is a driver program to run on top of the RMIServers to make them behave like Peers in P2P would.
The RMICoordinator is the Coordinator
The RMIServer is any peer in the P2P.
