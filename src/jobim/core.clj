(ns jobim.core)

(defn def-form? [[a & _]] (= a 'def))
(defn defn-form? [[a & _]] (= a 'defn))

(defn def-form [[_ name & code]]
  `[~(keyword name) ~@code])

(defn defn-form [[_ name args & code]]
  `[~(keyword name) (fn ~name ~args ~@code)])

(defn apply-env [env expr]
  (cond
    (symbol? expr) (let [key (keyword expr)]
                     (if (contains? env key)
                       (key env)
                       expr))
    (seq? expr) (map #(apply-env env %) expr)
    :else expr))

(defn transform-code [code-map snippet]
  (let [id (:length code-map)
        env-snippet (apply-env code-map snippet)
        [key val] (cond
                    (not (seq snippet)) [id env-snippet]
                    (< (count snippet) 3) [id env-snippet]
                    (def-form?  env-snippet) (def-form  env-snippet)
                    (< (count snippet) 4) [id env-snippet]
                    (defn-form? env-snippet) (defn-form env-snippet)
                    :else [id env-snippet])]
    (-> code-map
        (assoc :length (inc id))
        (assoc key val))))

(defmacro clojure-code [& code]
  `(->ClojureCode '~code ~(reduce transform-code {:length 0} code)))

(defmacro defshow [name style & slides]
  `(def ~name (slide-show ~style ~@slides)))
