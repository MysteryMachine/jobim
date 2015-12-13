# jobim

`jobim` is a Clojurescript presentation library, meant for building
simple presentations quickly, out of pure data. It's currently usable
alpha software that should evolve as I make presentations and my needs
change.

The core abstraction of `jobim` is the Slide protocol. `jobim` provides
a few slides to you by default, but anything that implements the protocol
can be rendered. This allows you to create your own custom

Here is what an example presentation can look like.

```clojure
(ns examples.intro
  (:require [jobim.core :as jobim
             :refer [slide-show default-style ->Title
                     ->CaptionedPic ->ClojureCode ->Picture]]))

(slide-show
 default-style
 (->Title
  "Unit Testing In Clojure"
  "Built for the Recurse Center by Sal Becker as a demonstration of Jobim")
 (->CaptionedPic
   "jobim.png"
   "I started naming all my libraries after Brazilian things.")
 (->ClojureCode
  ['(def a (+ 1 2))
   '(def b (+ 2 3))])
 (->Picture "meme.jpg"))
```

One of the coolest features of `jobim` is that it allows you to include
Clojure code as a quoted form, allowing you to use ACTUAL code inside
code examples.

## Feature Wishlist

* Highlighting of Clojure expressions
* A built in REPL
* Features for more languages

## Setup

Setup information coming as the project achieves more stability. For now,
checkout the examples directory to see running code. A presentation should
be buildable as long as you copy all HTML, CSS, and you run cljsbuild on
some sort of presentation to get it running.

More specific information coming soon.

## License

Copyright © 2015 Salomao Becker 

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
