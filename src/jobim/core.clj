(ns jobim.core
  (:require [jobim.core.impl :as impl]))

(defmacro clojure-code
  "Given a width, and some code, evaluate the code and create a textual
   representation of it.
   <width>: The max width of your code, Jobim uses a pretty printer to
   represent your code. Fiddle with this to make it look nicer.
   <code>: Literal clojure code. Should not be unquoted. Several expressions
   are fine. defs and defns will be stored within the state, and jobim
   will be sure to evaluate any code that depends on those vars correctly."
  [width & code]
  `(jobim.core.impl/->ClojureCode '~code ~(impl/transform-code code) ~width nil))

(defmacro commented-clj
  "Like `clojure-code` but takes a comment for the code.
  <comment>: Hiccup"
  [width comment & code]
  `(jobim.core.impl/->ClojureCode '~code ~(impl/transform-code code) ~width ~comment))

(defmacro defclj
  "Like `clojure-code`, but defs your slide to the namespace. Recommended
   over `clojure-code` as this format improves testability."
  [name width & code]
  `(def ~name (clojure-code ~width ~@code)))

(defmacro defcommented-clj
  "Like `commmented-clj` but defs your slide to the namesspace."
  [name width comment & code]
  `(def ~name (commented-clj ~width ~comment ~@code)))

(defmacro pseudo-clj
  "Given a width and some Clojure code, create a textual representation of
   that code. Will not evaluate any code. Meant for code that is rough
   and/or otherwise untestable.
   <width>: The max width of your code, Jobim uses a pretty printer to
   represent your code. Fiddle with this to make it look nicer.
   <code>: Literal clojure code. Should not be unquoted. Several expressions
   are fine."
  [width & code]
  `(jobim.core.impl/->ClojureCode '~code {:length 0} ~width nil))

(defmacro commented-pseudo-clj
  "Like `psuedo-clj`, but takes a comment."
  [width comment & code]
  `(jobim.core.impl/->ClojureCode '~code {:length 0} ~width ~comment))

(defmacro inline [& code]
  `(jobim.core.impl/render-inline-code (str '~@code)))

(defmacro defshow
  "Given a state, a style, and some slides, renders a Jobim
   slide show.
  <show-state>: An atom, ideally created by `new-show` below.
  <style>: A CSS hashmap, like what you'd pass in to Reagent.
  <slides>: Any number of Jobim slides."
  [name state style & slides]
  `(def ~name (impl/slide-show ~state ~style ~@slides)))

(defmacro defblog
  ""
  [name state & slides]
  `(def ~name (impl/blog-post ~state ~@slides)))

(defmacro env
  "* [slide]
     Accesses the environment within a `clojure-code` slide.
   * [slide env-var]
     Accesses a specific env var within an environment.
   <slide>: a `clojure-code` slide.
   <env-var>: a symbol

   Usage:
   (defclj test-slide (def a 1) (defn b [n] (+ n 2)) (b a))

   (env test-slide)    ;;=> {:a 1 :b <function> :%2 3}
   (env test-slide a)  ;;=> 1
   (env test-slide %2) ;;=> 3

   Note that things that are not named are accessed by a %0, %1...
   format, where the number is, starting with 0, the order in
   which they were evaluated in the environment."
  ([slide]
   `(get ~slide :env))
  ([slide env-var]
   `(get-in ~slide [:env ~(keyword env-var)])))
