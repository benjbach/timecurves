# Time curves

Time curves are a visualization for temporal data, explained in detail here: www.aviz.fr/~bbach/timecurves. 

The code for time curves is divided into a client and a server. The client front end is written in java scrpt and relies on d3. The backend, is written in Java and calculates the curves which are then visualized by the client side. Those files have the extension curve and are in json format. 

Currently, the repo contains the client side. The server side calculating curves for wikipedia articles, videos, and dynamic networks comes soon. 


## Client Code

The client code constist of an index.php, which is a PHP in order to track sessions. Curve files are loaded from a directory called timecurves. 

While index.php sets up the visualization context, the core file is called 'curvepile.js'. 

Curvepile.js corresponds to an svg that can display multiple curves and make individial curves visible or hide them. 


## Server Code

The server code imports similarity matrices, as indidated on the website: www.aviz.fr/~bbach/timecurves. 
The compiled version (foldSimilarityMatrix.jar) takes two commandline parameters: input and output folder: 

'java -jar foldSimilarityMatrix.jar myInputDir myOutputDir'

This command translates all similarity matrices in 'myInputDir' into .curve' files in the 'myOutputDir'. 

The class that loads the matrices in '.json' format is 'fr.inria.aviz.progresio.server.matrixImport.DistanceMatrixImporter.java'. 


