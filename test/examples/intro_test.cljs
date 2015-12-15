(ns examples.intro-test
  (:require [cljs.test :refer-macros [deftest is]]
            [examples.intro :as show]))

(deftest code-slide-test
  (is (= (get (:env show/code-slide) 3) 19)))
