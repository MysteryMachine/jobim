(ns intro.core
  (:require [jobim.core :as jobim])
  (:require-macros [jobim.core :as jobim]))

(defonce show-state (jobim/new-show))

(jobim/defclj code-slide 40
  (def a (+ 1 2))
  (def b (+ a 3))
  (defn c [d] (+ d b a))
  (defn d [e]
    (if (= e :do-it)
      (c 10)
      (c 15)))
  (d :do-it))

(jobim/defshow intro-to-clojure
  show-state
  jobim/default-style
  (jobim/title
   "Jobim: Testable and Extensible CLJS Presentations"
   "Made by Sal Becker for the Fall 2 batch of the Recurse Center")
  (jobim/bullets "Have You Ever?"
                 "Included code snippets in slide shows?"
                 "Wrote tests for code in your slides?"
                 "Written incorrect code into a slideshow?")
  (jobim/img "https://vierbergenlars.files.wordpress.com/2013/08/40434677.jpg?w=594")
  (jobim/captioned-img
   "https://i.ytimg.com/vi/84J0UXxxBXQ/maxresdefault.jpg"
   "Brazilian Bossa Nova musician, Tom Jobim, has nothing to do with unit tests.")
  (jobim/text "...but he was really good on stage, so maybe the name fits.")
  (jobim/img "http://cdn.meme.am/instances/500x/55480046.jpg")
  (jobim/text "Most of our technical slide shows are just plain strings or files!")
  (jobim/text "What if we could write techincal presentations in actual testable code!")
  (jobim/text "The Basics")
  (jobim/pseudo-clj 80
    (defshow show-name
      jobim/default-style
      (jobim/title "title" "subtitle")
      (jobim/text "text")
      (jobim/img "url")
      (jobim/captioned-img "url" "text")))
  code-slide
  (jobim/pseudo-clj 80
   (defclj code-slide 40
     (def a (+ 1 2))
     (def b (+ a 3))
     (defn c [d] (+ d b a))
     (defn d [e]
       (if (= e :do-it)
         (c 10)
         (c 15)))
     (d :do-it)))
  (jobim/pseudo-clj 80
    (deftest code-slide-test
      (let [{:keys [a b c %4]} (jobim/env show/code-slide)]
        (is (= a 3))
        (is (= b 6))
        (is (= (c 1) 10))
        (is (= %4 19)))))
  (jobim/text "Tests run live in a figwheel environment while you develop.")
  (jobim/text "The Slide Abstraction: How to Extend Jobim")
  (jobim/pseudo-clj 80
   (defprotocol Slide
     (render-slide [this])
     (next-slide   [this state])
     (prev-slide   [this state])))
  (jobim/text "What about other languages?")
  (jobim/code*
   "javascript"
   "function test(){"
   ["console.log(\"This is a JS function\");"]
   "};")
  (jobim/pseudo-clj 40
   (jobim/code*
    "javascript"
    "function test(){"
    ["console.log(\"This is a JS function\");"]
    "};"))
  (jobim/code*
   "python"
   "def test():"
   ["print \"Jobim can do Python too!\""])
  (jobim/pseudo-clj 40
   (jobim/code*
    "python"
    "def test():"
    ["print \"Jobim can do Python too!\""])))
