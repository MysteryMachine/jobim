(ns jobim.figwheel.runner)

(defmacro defrunner [name build & syms]
  `(do
     (defn ~name [] (cljs.test/run-tests ~@syms))
     (jobim.figwheel.runner/run ~name ~build)))
