(ns tests.runner)

(defmacro defrunner [name build & syms]
  `(do
     (defn ~name [] (cljs.test/run-tests ~@syms))
     (tests.runner/run ~name ~build)))
