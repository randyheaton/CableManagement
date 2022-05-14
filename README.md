
<!-- Plugin description -->
a plugin description
<!-- Plugin description end -->



This is a plugin for Android Studio. I started the project from a template provided by IntelliJ, and some
of the names still probably reflect that. 

**This is a learning project. It's not intended to be used as a production product. Be cautioned that allowing a plugin to inject files into your project directories seems to really tax Android Studio's ability to index. I've been forced to invalidate caches to fix unresolved references.** 



# Purpose:

Cable Management is an architectural pattern compliance assistant. While I was refactoring InjectorSuite to be more
expressive of and adherent to its MVVM model, I realized that a visual aid would be pretty cool. Something that showed
the model layers graphically and drew lines between dependencies and callers at each level. Sort of like really pretty cable
management.

# How it works:

Inside of a few text fields, you provide us with a list of the layers of your model, specifying their names and an integer representing their
layer level in a hierarchy. Something like: "Views" are level 1, "Interface" is level 2, and "Serializer" is level 3. 

![Image](/readme_images/establish_model.png)


We take that information and use it to write a JSON file and a Kotlin annotation class file into
the resource folder of your project. 

![Image](/readme_images/injected_files.png)

From then on, you can mark your classes or functions, e.g.: @Views.

![Images](/readme_images/annotation_use.png)

Then when you're ready, we round up all of those annotated classes and make a neat graph showing the connections
between actors at different levels. If it all works out correctly, you should see nearly bipartite separation between each layer.
Like tidy cable management. 

![Images](/readme_images/generated_graph.png)

# Why it's cool:

The project is cool because we're using Compose for desktop inside of the IDE windows. That usage is not currently 
supported but it is possible through Compose's ComposePanel(). 

# Where we're at:


- Got the environment squared away. Lots of versioning issues with platform version (gradle properties in plugin build
needs correct version for receiving Android Studio instance) and JVM versions. Triple check those if you fork.

- Can build model from text field inputs.

- Can inject json config file and annotation class files into project.

- Can generate dependency graph that is aware of simple function and constructor calls of type ``val x=Foo()``, 
static invocations of type ``Foo.bar()``, and factory invocations of type ``val x=Builder<Foo>()``.

# What's next:

Lots of hardcoding of folder names relying on assumptions about project structure need to be fixed.

There are probably a lot of dependencies that don't come in the form of simple calls ``val x=Foo()``,
static invocations ``Foo.bar()``, or factory invocations ``val x=Builder<Foo>()``

Also, the geometry isn't scalable. It's all hardcoded for the demo you see in the screenshots. 



# The example from the screenshots

I tested the plugin on a one-screen app I made to demo use cases of LazyColumn and BottomModalSheet and learn more about using Retrofit with Compose. Here's a diagram for reference. 

```
/*
                        ┌────────────┐
                        │            │
                        │   Views    │
                        │            │
                        └┬─────────▲─┘
               notify    │         │ update state
                events   │         │   for
                 to      │         │
                       ┌─▼─────────┴───┐◄──────────────┐
                       │               │               │
                       │  ViewModel    ├────────┐      │
                       │               │        │      │
                       └───────────────┘        │      │ render JSON
                                       forward  │      │    for
                                       query    │      │
                                       through  │      │
                                                │      │
┌─────────────────────────────┐           ┌─────▼──────┴─────────┐
│                             │  provide  │   NetworkInterface   │
│                             │   key to  │   (data classes,     │
│  KeyProviderSingleInstance  ├───────────┼─►  ApiInterface,     │
│                             │           │    QueryGrammar)     │
│                             │           │                      │
│                             │           │                      │
└────┬─────────────────────▲──┘           └────┬─────────▲───────┘
     │                     │                   │         │
     │request key          │          request  │         │ provide JSON
     │   from              │           JSON    │         │    to
   ┌─▼──────────┐          │           from   ┌▼─────────┴───┐
   │            │          │                  │              │
   │            │          │                  │              │
   │  Resources ├──────────┘                  │   Server     │
   │            │   provide key               │              │
   │            │      to                     │              │
   └────────────┘                             └──────────────┘
 */
 ```

