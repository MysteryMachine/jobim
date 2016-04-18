(ns intro.core-test
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [jobim.figwheel.helper]
            [intro.core :as show]))

#_(deftest code-slide-test
  (is (= (get (:env show/code-slide) :a) 3))
  (is (= (get (:env show/code-slide) :b) 6))
  (is (= ((get (:env show/code-slide) :c) 1) 10))
  (is (= (get (:env show/code-slide) :%4) 19)))

(run-tests)
