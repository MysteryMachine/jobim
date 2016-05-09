(ns intro.core-test
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [intro.core :as show]
            [jobim.figwheel.helper])
  (:require-macros [jobim.core :as jobim]))

(deftest code-slide-test
  (let [{:keys [a b c %4]} (jobim/env show/code-slide)]
    (is (= a 3))
    (is (= b 6))
    (is (= (c 1) 10))
    (is (= %4 19))))

(run-tests)
