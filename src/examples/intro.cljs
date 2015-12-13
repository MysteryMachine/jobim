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
  "https://i.ytimg.com/vi/84J0UXxxBXQ/maxresdefault.jpg"
  "I started naming all my libraries after Brazilian things.")
 (->ClojureCode
  ['(def a (+ 1 2))
   '(def b (+ 2 3))])
 (->Picture 
  "http://s.quickmeme.com/img/cc/cc0110d0bd1ee9336c872203f81181344211f9165cf4257fad12dd27a4b7efaf.jpg"))
