
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


- Got the environment squared away. Lots of versioning issues with platform version (gradle properties in plugin build
needs correct version for receiving Android Studio instance) and JVM versions. Triple check those if you fork.

- Can build model from text field inputs.

- Can inject json config file and annotation class files into project.

- Can generate dependency graph that is aware of simple function and constructor calls of type ``val x=Foo()``, 
static invocations of type ``Foo.bar()``, and factory invocations of type ``val x=Builder<Foo>()``.

<font size=5>What's next:</font>

Lots of hardcoding of folder names relying on assumptions about project structure need to be fixed.

And then we have to figure out a good way to generate the graphic. My instinct says a Canvas because leaning on 
ready-made composables doesn't seem feasible because of the geometry. If the IDE windows will play well with a 
webview, we could dispatch graphics to js. I don't know yet. 
