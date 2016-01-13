(ns jobim.runner ^:figwheel-always
  (:require [cljs.test :include-macros true]
            [jobim.core-test]
            [jobim.figwheel.runner :refer-macros [defrunner]]))

(defrunner runner "test" 'jobim.core-test)
