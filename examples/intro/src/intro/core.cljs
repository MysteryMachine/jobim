(ns intro.core
  (:require [jobim.core :as jobim
             :refer [slide-show default-style ->Title
                     ->CaptionedPic ->ClojureCode ->Picture
                     ->Code ->Text code new-show ->CustomSlide]
             :refer-macros [defshow defclj pseudo-clj]]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(defonce show-state (new-show))

(defclj code-slide 40
  (def a (+ 1 2))
  (def b (+ a 3))
  (defn c [d] (+ d b a))
  (defn d [e]
    (if (= e :do-it)
      (c 10)
      (c 15)))
  (d :do-it))

(defshow intro-to-clojure
  show-state
  default-style
  (->CustomSlide
   (fn [state]
     [:div {:style {:color (:color state)}}
      (:page state)]))
  (->Title
   "Jobim: Testable and Extensible CLJS Presentations"
   "Made by Sal Becker for the Fall 2 batch of the Recurse Center")
  (->Text "Have you ever...")
  (->Text "Included code snippets in slide shows?")
  (->Text "Wrote tests for code in your slides?")
  (->Text "Written incorrect code into a slideshow?")
  (->Picture "https://vierbergenlars.files.wordpress.com/2013/08/40434677.jpg?w=594")
  (->CaptionedPic
   "https://i.ytimg.com/vi/84J0UXxxBXQ/maxresdefault.jpg"
   "Brazilian Bossa Nova musician, Tom Jobim, has nothing to do with unit tests.")
  (->Text "...but he was really good on stage, so maybe the name fits.")
  (->Picture "http://cdn.meme.am/instances/500x/55480046.jpg")
  (->Text "The Basics")
  (pseudo-clj 80
    (defshow show-name
      slide-style
      (->Title "title" "subtitle")
      (->Text "text")
      (->Picture "url")
      (->CaptionedPic "url" "text")))
  (->Text "Most of our technical slide shows are just plain strings or files!")
  (->Text "What if we could write techincal presentations in actual testable code!")
  code-slide
  (pseudo-clj 80
   (defclj code-slide 40
     (def a (+ 1 2))
     (def b (+ a 3))
     (defn c [d] (+ d b a))
     (defn d [e]
       (if (= e :do-it)
         (c 10)
         (c 15)))
     (d :do-it)))
  (pseudo-clj 80
   (deftest code-slide-test
     (is (= (get (:env show/code-slide) :a) 3))
     (is (= (get (:env show/code-slide) :b) 6))
     (is (= ((get (:env show/code-slide) :c) 1) 10))
     (is (= (get (:env show/code-slide) 4) 19))))
  (->Text "Tests run live in a figwheel environment while you develop.")
  (->Text "The Slide Abstraction: How to Extend Jobim")
  (pseudo-clj 80
   (defprotocol Slide
     (render-slide [this])
     (next-slide   [this state])
     (prev-slide   [this state])))
  (->Text "What about other languages?")
  (code
   "javascript"
   "function test(){"
   ["console.log(\"This is a JS function\");"]
   "};")
  (pseudo-clj 40
   (code
    "javascript"
    "function test(){"
    ["console.log(\"This is a JS function\");"]
    "};"))
  (code
   "python"
   "def test():"
   ["print \"Jobim can do Python too!\""])
  (pseudo-clj 40
   (code
    "python"
    "def test():"
    ["print \"Jobim can do Python too!\""])))
