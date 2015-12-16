(ns examples.intro
  (:require [jobim.core :as jobim
             :refer [slide-show default-style ->Title
                     ->CaptionedPic ->ClojureCode ->Picture]
             :refer-macros [defshow defclj]]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(def jobim-image "https://i.ytimg.com/vi/84J0UXxxBXQ/maxresdefault.jpg")
(def meme-image "http://s.quickmeme.com/img/cc/cc0110d0bd1ee9336c872203f81181344211f9165cf4257fad12dd27a4b7efaf.jpg")

(defclj code-slide 20
  (def a (+ 1 2))
  (def b (+ a 3))
  (defn c [d] (+ a b d))
  (c 10)
  (defn e [b]
    (> 10 (+ 1 2 3))
    (+ 1 2 3 4 5)
    (map inc [ 1 2 34])))

(defshow intro-to-clojure
  default-style
  (->Title
   "Unit Testing In Clojure"
   "Built for the Recurse Center by Sal Becker as a demonstration of Jobim")
  (->CaptionedPic
   jobim-image
   "I started naming all my libraries after Brazilian things.")
  code-slide
  (->Picture meme-image))

