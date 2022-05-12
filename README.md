
<!-- Plugin description -->
a plugin description
<!-- Plugin description end -->


This is a plugin for Jetbrains IDEs. I started the project from a template provided by IntelliJ, and some
of the naming conventions still reflect that. 



<font size=5> Purpose:</font>

Cable Management is an architectural pattern compliance assistant. While I was refactoring InjectorSuite to be more
expressive of and adherent to its MVVM model, I realized that a visual aid would be pretty cool. Something that showed
the model layers graphically and drew lines between dependencies and callers at each level. Sort of like really pretty cable
management.

<font size=5> How it works:</font>

Inside of a few text fields, you provide us with a list of the layers of your model, specifying their names and an integer representing their
layer level in a hierarchy. Something like: "Views" are level 1, "Interface" is level 2, and "Disk" is level 3. 

We take that and make a JSON file to configure everything. From the JSON file, we write a Kotlin annotation class file into
the resource folder of your project. 

From then on, you can mark your classes or functions, e.g.: @Views.

Then when you're ready, we round up all of those annotated classes and make a neat graph showing the connections
between actors at different levels. If it all works out correctly, you should see bipartite separation between each layer.
Like tidy cable management. 

<font size=5> Why it's cool:</font>

The project is cool because we're using Compose for desktop inside of the IDE windows. That usage is not currently 
supported but it is possible through Compose's ComposePanel(). 

<font size=5> Where we're at:</font>


I spent a lot of time in environment purgatory between Gradle and versioning problems, but now the environment is all 
squared away. And if you fork this within the next few months, it might be plug-and-go for you, but you'll probably have
to triple check your java compiler versions. 

Right now, we can take inputs from text fields to build a model. From that model, we can inject annotation classes into the project and also build a json config file of the model. And we can also build a graph of all dependencies that use annotations from the model. 


<font size=5>What's next:</font>


We have to figure out a good way to generate the graphic. My instinct says a Canvas because leaning on 
ready-made composables doesn't seem feasible because of the geometry. If the IDE windows will play well with a 
webview, we could dispatch graphics to js. I don't know yet. 
