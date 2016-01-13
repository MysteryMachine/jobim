(ns intro.runner
  (:require  [cljs.test :include-macros true]
             [jobim.figwheel.runner :refer-macros [defrunner]]
             [intro.core-test]))

(defrunner runner "intro" 'intro.core-test)