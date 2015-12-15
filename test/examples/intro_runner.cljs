(ns examples.intro-runner
  (:require  [cljs.test :include-macros true]
             [tests.runner :refer-macros [defrunner]]
             [examples.intro-test]))

(defrunner runner "intro" 'examples.intro-test)
