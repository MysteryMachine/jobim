# jobim

`jobim` is a ClojureScript library aimed allowing ClojureScript developers to rapidly
create slideshows and presentations that compile down to Javascript, and can be easily
hosted on a persons website. `jobim` takes advantage of ClojureScript's
homoiconic nature to allow developers to execute, and write tests for code snippets
included in their presentations. Additionally, `jobim` provides some limited support
for code snippets written in other languages.

## Getting Started

I'm almost finished releasing version 1.0 of this library. Once I do,
I will provide a command along the lines of `lein jobim` to create a
project with good defaults. Expect this soon.

If you're itching to use it now, clone the repository, and examine
both the example project provided, and the `project.clj` file. To
run the example, call `lein figwheel intro` from your command line.

`jobim` requires that the html file in which you are including the generated Javascript
file has an element named jobim. It also requires that you include some very basic
css on the page.

```css
body, pre{
    padding: 0px;
    margin: 0px;
}
```

Finally, it requires that you include [highlight.js](highlightjs.org).
You should include a tag for a stylesheet, and for the Javascript. Check
`index.html` in the project for an example.

## Usage

The philosophy of this library is that most slideshows utilize very similar
patterns, and do not need a high level of customizablity. On the other hand,
`jobim` also provides the user with a simple protocol to extend, so that
they can create their own slides.

### Creating a Presentation

`jobim` allows the user to define a presentation using the `defshow`
macro. This macro registers various listeners to keys, and
renders a Reagent component on an element with the id "jobim".
`jobim` is currently only meant to be used standalone on a page
that is landscape. It will not render properly on pages resized
to be portrait, or in an embedded environment, though this may
change in the future.

The macro takes a show name, where it will store your show. After that,
it takes Reagent style CSS map, where you can define some basic themes
for your show. If you'd rather use a CSS file, you can feel free to leave
this option as an empty map, however, I recommend checking out what
the default style is like so that your presentation looks right. After
the styles, the macro takes any number of slides.

```clojure
(ns example.core
  (:require [jobim.core :as jobim
             :refer [slide-show default-style ->Title ->Text]
             :refer-macros [defshow]]))

(defshow show-name
  default-style
  (->Title "Presentation Title" "Subtitle")
  (->Text  "Some Text"))
```

### Basic Slides

`jobim` provides a large array of macros and constructors to cover
commonly used slides. The below are the the slides that do not interface
with code in any way. The only notable one is `->CustomSlide`, which expects
a Reagent style component, like `[:div [:span "a"] [:button "hi"]]`.

```clojure
(->Title title subtitle)
(->Text text)
(->Picture url)
(->CaptionedPic url caption)
(->CustomSlide reagent-style-html)
```

When you want a slide with Clojure code on it, use the `pseudo-clj` and `clojure-code`
macros. `pseudo-clj` is meant for code that will not run in any way. It is meant
for incomplete examples, or for code that just shouldn't be run on the browser.
If you're using `jobim` to write presentations about Java interop in Clojure, for
example, you could not test that code in the browser. Stick that in `psuedo-clj`.
Support for testing on the JVM not currently planned, but it might happen.

### Code Slides

```clojure
(psuedo-clj character-width code-form)
(pseudo-clj 40 (+ 1 2 3 4)) ;; an example

(defshow show default-style
  (psuedo-clj 40 (+ 1 2 3 4)))
```

`clojure-code` defines a `ClojureCode` slide. This macro evaluates your expressions,
and stores them for you under the `:env` key. Calling `(:env your-clojure-code-slide)`
will return a map where all the expressions are evaluated.
For your convinience, you can use `defclj` as a replacement for
`(def slide (clojure-code ...))`.

```clojure
(defclj code-slide 40
  (+ 1 2)              ;; The result of the 0th expression is stored in the key :%0
  (def b 3)            ;; This expression has a name, so it is stored under :b
  (defn a [c] (+ c 1)) ;; This expression is stored in the key :a
  (a 10))              ;; This expression is stored in the key :%3

(get (:env code-slide) :%0)       ;; 3
(get (:env code-slide) :b)      ;; 3
((get (:env code-slide) :a) 5)  ;; 6
(get (:env code-slide) :%3)       ;; 11
```

This allows you to write tests against your presentation. To see an example of this,
check the the `examples/intro/test` directory.

`jobim` uses highlight.js and [fipp](https://github.com/brandonbloom/fipp) to pretty
print and color your code automatically. Since we are using Clojure's homoiconic nature
to run our code, we can't let you define your own whitespace (because Clojure ignores
whitespace). If defining your own whitespace is super important to you, consider
creating your own non-testable slides. Alternatively, consider using the `code` macro.

`code` is a macro meant to be used to create short snippets in other languages. `code`
determines whitespace through checking vector nestedness. The more vectors
`code` finds around a form, the more whitespace it adds. It also adds a newline
to every separate string it finds. This saves you from
having to type in the whitespace and newlines yourself. Of course, you still CAN
write it all as one string. The first argument to the macro is the highlight.js
class associated with your language. As long as your highligh.js package supports
the language you included, everything should work. Not all languages are
included in highlight.js by default. If your language isn't working, try
and create a custom package of highlight.js where your language is included.

```clojure
(code
  "javascript"
  "function test(){"
  ["console.log(\"This is a JS function\");"]
  "};")

(code
  "python"
  "def test():"
  ["print \"Jobim can do Python too!\""])
```

### Your Own Slides

To create your custom slides, make sure your slides implement the `Slide`
protocol. A `Slide` has three functions, `render-slide`, `next-slide`,
and `prev-slide`. `render` slide should return a Reagent data structure
like `[:div "hi"]`. `next-slide` and `prev-slide` are functions that recieve
that current state of the app, a hashmap, and can modify that state. For most slides,
just calling the `std-next` or `std-prev` functions should be adequate.
Both these functions increase the `:page` value in the state by one. Your
functions are free to modify this state as you wish, and are responsible for
any clean up. See the implementation of any of our existing slides for examples
on how to extend the protocol.

As a development tip, if you are doing any complex state work, and you need to see
the current state of the slideshow, check the `jobim.core/show-state` atom.

## License

Copyright © 2015 Salomao Becker 

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
